/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.web.site;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.alfresco.connector.AlfrescoAuthenticator;
import org.alfresco.connector.ConnectorSession;
import org.springframework.extensions.surf.util.URLDecoder;
import org.alfresco.web.framework.exception.ContentLoaderException;
import org.alfresco.web.framework.model.Page;
import org.alfresco.web.framework.model.Theme;
import org.alfresco.web.framework.resource.ResourceContent;
import org.alfresco.web.scripts.ProcessorModelHelper;
import org.alfresco.web.scripts.URLHelper;
import org.alfresco.web.scripts.WebScriptProcessor;
import org.alfresco.web.site.exception.PageMapperException;

/**
 * This is a Page Mapper class which serves to interpret URLs at dispatch
 * time for the Mobile project.
 * 
 * Requests are received in the style defined for Slingshot.
 * 
 * Requests arrive in the form:
 * 
 * 		/p
 * 		/p/<pageId>
 * 		/p/<pageId>?<objArgument>=<objectId>
 * 		/p/<pageId>?<objArgument>=<objectId>&other arguments
 * 
 * The <pageId> identifier could be the id of the page object.
 * It could also be a relative path to the page object:
 * 
 * 		For example:  /p/user/profile
 * 
 * Everything from the original request is available downstream to
 * all rendering components and templates.
 * 
 * @author kevinr
 */
public class MobilePageMapper extends AbstractPageMapper
{
    private static final String URI_PAGEID = "pageid";
    
    
    /**
     * Empty constructor - for instantiation via reflection 
     */
    public MobilePageMapper()
    {
        super();
    }
    
    /**
     * Process a page request.
     */
    public void executeMapper(RequestContext context,
        ServletRequest request) throws PageMapperException
    {
    	if (request instanceof HttpServletRequest == false)
    	{
    		throw new PageMapperException("The Mobile page mapper must be given an HttpServletRequest to execute.");
    	}
    	
    	// The request URI string.  This comes in as something like:
    	//    /mobile/p/user/profile
        // Strip off the webapp name (if any - may be ROOT i.e. "/")
        HttpServletRequest req = ((HttpServletRequest)request);
    	String requestURI = req.getRequestURI().substring(req.getContextPath().length());
        requestURI = URLDecoder.decode(requestURI);
        
    	// Extract page Id from the rest of the URI
    	String pageId = null;
        
	    // strip servlet name and set remaining path as currently executing URI
        Map<String, String> uriTokens = null;
        int pathIndex = requestURI.indexOf('/', 1);
        
        // Test to see if any elements are provided beyond the servlet name.
        // Optimization to ignore webscript /service requests when processing page URIs -
        // this is because a RequestContext is created manually for service requests
        // that pass through the web-framework webscript runtime - we know those requests
        // are not related to pages or page types so we can skip some processing.
        if (pathIndex != -1 && requestURI.length() > (pathIndex + 1) &&
            !WebScriptProcessor.WEBSCRIPT_SERVICE_SERVLET.equals(requestURI.substring(0, pathIndex)))
        {
            pageId = requestURI.substring(pathIndex + 1);
            context.setUri(pageId);
        }
        
        // build a URLHelper object one time here - it is an immutable object
        URLHelper urlHelper = new URLHelper(req, uriTokens);
        context.setValue(ProcessorModelHelper.MODEL_URL, urlHelper);
    	
        /**
         * Extract the page type id and determine which page to bind
         * into the request context.
         * 
         * This checks to see if a theme is defined and whether said theme
         * describes an override for this page type.
         * 
         * Otherwise, a check is made to see if a system default page has
         * been specified for this page type.
         * 
         * Finally, if nothing can be determined, a generic page is
         * bound into the request context.
         */
        String pageTypeId = (String) request.getParameter("pt");
        if (pageTypeId != null && pageTypeId.length() != 0)
        {
            // page type overrides anything else on the uri
            pageId = null;
            
            // Consider the theme
            String themeId = (String) context.getThemeId();
            Theme theme = context.getModel().getTheme(themeId);
            if (theme != null)
            {
                pageId = theme.getPageId(pageTypeId);
            }
            
            // Consider whether a system default has been set up
            if (pageId == null)
            {
                pageId = getPageId(context, pageTypeId);
            }
            
            // Worst case, pick a generic page
            if (pageId == null)
            {
                pageId = getPageId(context, WebFrameworkConstants.GENERIC_PAGE_TYPE_DEFAULT_PAGE_ID);
            }
        }
        
    	// Place everything into the request context
    	if (pageId != null)
    	{
    		// We have a page Id.  We must resolve it to a page.
            Page page = context.getModel().getPage(pageId);
            if (page != null)
            {
                context.setPage(page);
            }
        }
        
        /**
         * At present, the Mobile project doesn't do much with formats.
         * 
         * Note that if we didn't set it, it would still automatically
         * pick up the default format.
         */
        context.setFormatId(FrameworkHelper.getConfig().getDefaultFormatId());
    }
}
