/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.scripts.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.config.ConfigService;
import org.alfresco.connector.Connector;
import org.alfresco.connector.ConnectorContext;
import org.alfresco.connector.ConnectorService;
import org.alfresco.connector.Credentials;
import org.alfresco.connector.HttpMethod;
import org.alfresco.connector.Response;
import org.alfresco.connector.CredentialsImpl;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.Base64;
import org.alfresco.web.config.RemoteConfigElement;
import org.alfresco.web.config.RemoteConfigElement.EndpointDescriptor;
import org.alfresco.web.config.RemoteConfigElement.IdentityType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * EndPoint HTTP Proxy Servlet.
 * 
 * Provides the ability to submit a URL request via a configured end point such as a
 * remote Alfresco Server. Makes use of the Connector framework so that appropriate
 * authentication is automatically applied to the proxied request as applicable.
 * 
 * This servlet accepts URIs of the following format:
 * 
 * /proxy/<endpointid>[/uri]*[?[<argName>=<argValue>]*]
 * 
 * Where:
 * 
 * - endpointid is the ID of a configured EndPoint model object to make a request against
 * - url is the uri to call on the EndPoint URL e.g. /api/sites
 * - argName is the name of a URL argument to append to the request
 * - argValue is the value of URL argument
 * 
 * E.g.
 * 
 * /proxy/alfresco/api/sites?name=mysite&desc=description
 * 
 * The proxy supports all valid HTTP methods.
 * 
 * @author kevinr
 */
public class EndPointProxyServlet extends HttpServlet
{
    private static final String USER_ID = "USER_ID";
    private static final String PARAM_ALF_TICKET = "alf_ticket";
    
    private static Log logger = LogFactory.getLog(EndPointProxyServlet.class);
    
    private static final long serialVersionUID = -176412355613122789L;
    
    protected RemoteConfigElement config;
    protected ConnectorService connectorService;
    
    @Override
    public void init() throws ServletException
    {
        super.init();
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        ConfigService configService = (ConfigService)context.getBean("web.config");
        
        // retrieve the remote configuration
        this.config = (RemoteConfigElement)configService.getConfig("Remote").getConfigElement("remote");
        
        // retrieve the connector service
        this.connectorService = (ConnectorService) context.getBean("connector.service");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        String uri = req.getRequestURI().substring(req.getContextPath().length());
        
        // validate and return the endpoint id from the URI path - stripping the servlet context
        StringTokenizer t = new StringTokenizer(uri, "/");
        String servletName = t.nextToken();
        if (!t.hasMoreTokens())
        {
            throw new IllegalArgumentException("Proxy URL did not specify endpoint id.");
        }
        String endpointId = t.nextToken();
        
        // rebuild rest of the URL for the proxy request
        StringBuilder buf = new StringBuilder(64);
        if (t.hasMoreTokens())
        {
            do
            {
                buf.append('/');
                buf.append(t.nextToken());
            } while (t.hasMoreTokens());
        }
        else
        {
            // allow for an empty uri to be passed in
            // this could therefore refer to the root of a service i.e. /webapp/axis
            buf.append('/');
        }
        
        try
        {
            // retrieve the endpoint descriptor - do not allow proxy access to unsecure endpoints
            EndpointDescriptor descriptor = this.config.getEndpointDescriptor(endpointId);
            if (descriptor == null || descriptor.getUnsecure())
            {
                // throw an exception if endpoint ID is does not exist or invalid
                throw new AlfrescoRuntimeException("Invalid EndPoint Id: " + endpointId);
            }
            
            // special case for some Flash based apps - they might pass in the alf_ticket directly
            // as a parameter as POST requests do not correctly pickup the browser cookies and
            // therefore do not share the same session so we must apply the ticket directly
            String ticket = req.getParameter(PARAM_ALF_TICKET);
            
            // user id from session NOTE: @see org.alfresco.web.site.UserFactory
            Connector connector;
            String userId = (String)req.getSession().getAttribute(USER_ID);
            if (userId != null)
            {
                // build an authenticated connector - as we have a userId
                connector = this.connectorService.getConnector(endpointId, userId, req.getSession());
            }
            else if (ticket != null ||
                     descriptor.getIdentity() == IdentityType.NONE ||
                     descriptor.getIdentity() == IdentityType.DECLARED ||
                     descriptor.getExternalAuth())
            {
                // the authentication for this endpoint is either not required, declared in config or
                // managed "externally" (i.e. by a servlet filter such as NTLM) - this means we should
                // proceed on the assumption it will be dealt with later
                connector = this.connectorService.getConnector(endpointId, req.getSession());
            }
            else if (descriptor.getBasicAuth())
            {
                // check for HTTP authorisation request (i.e. RSS feeds etc.)
                String authorization = req.getHeader("Authorization");
                if (authorization == null || authorization.length() == 0)
                {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED,
                            "No USER_ID found in session and requested endpoint requires authentication.");
                    res.setHeader("WWW-Authenticate", "Basic realm=\"Alfresco\"");
                    
                    // no further processing as authentication is required but not provided
                    // the browser will now prompt the user for appropriate credentials
                    return;
                }
                else
                {
                    // user has provided authentication details with the request
                    String[] authParts = authorization.split(" ");
                    if (!authParts[0].equalsIgnoreCase("basic"))
                    {
                        throw new AlfrescoRuntimeException("Authorization '" + authParts[0] + "' not supported.");
                    }
                    
                    String[] values = new String(Base64.decode(authParts[1])).split(":");
                    if (values.length == 2)
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Authenticating (BASIC HTTP) user " + values[0]);
                        
                        // assume username and password passed as the parts and
                        // build an unauthenticated authentication connector then
                        // apply the supplied credentials to it
                        connector = this.connectorService.getConnector(endpointId, values[0], req.getSession());
                        Credentials credentials = new CredentialsImpl(endpointId);
                        credentials.setProperty(Credentials.CREDENTIAL_USERNAME, values[0]);
                        credentials.setProperty(Credentials.CREDENTIAL_PASSWORD, values[1]);
                        connector.setCredentials(credentials);
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException("Authorization request did not provide user/pass.");
                    }
                }
            }
            else
            {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED,
                        "No USER_ID found in session and requested endpoint requires authentication.");
                return;
            }
            
            // build a connector context, stores information about how we will drive the remote client
            ConnectorContext context;
            if (ticket == null)
            {
                context = new ConnectorContext();
            }
            else
            {
                // special case for some Flash apps - see above
                Map<String, String> params = new HashMap<String, String>(1, 1.0f);
                params.put(PARAM_ALF_TICKET, ticket);
                context = new ConnectorContext(params, null);
            }
            context.setContentType(req.getContentType());
            context.setMethod(HttpMethod.valueOf(req.getMethod().toUpperCase()));
            
            // build proxy URL referencing the endpoint
            String q = req.getQueryString();
            String url = buf.toString() + (q != null && q.length() != 0 ? "?" + q : "");
            
            if (logger.isDebugEnabled())
            {
                logger.debug("EndPointProxyServlet preparing to proxy:");
                logger.debug(" - endpointId: " + endpointId);
                logger.debug(" - userId: " + userId);
                logger.debug(" - connector: " + connector);
                logger.debug(" - method: " + context.getMethod());
                logger.debug(" - url: " + url);
            }
            
            // call through using our connector to proxy
            Response response = connector.call(url, context, req, res);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Return code: " + response.getStatus().getCode());
            }
        }
        catch (Throwable err)
        {
            // TODO: trap and handle errors!
            throw new AlfrescoRuntimeException("Error during endpoint proxy processing: " + err.getMessage(), err);
        }
    }
}
