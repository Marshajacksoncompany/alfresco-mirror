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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.page;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigService;
import org.alfresco.web.config.ServerProperties;
import org.alfresco.web.page.PageAuthenticationServlet.AuthenticationResult;
import org.alfresco.web.scripts.PresentationScriptProcessor;
import org.alfresco.web.scripts.PresentationTemplateProcessor;
import org.alfresco.web.scripts.Registry;
import org.alfresco.web.scripts.RuntimeContainer;
import org.alfresco.web.scripts.ScriptContent;
import org.alfresco.web.scripts.Store;
import org.alfresco.web.scripts.URLHelper;
import org.alfresco.web.scripts.servlet.WebScriptServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet for rendering pages based a behavior driven FreeMarker Template and scoped
 * WebScript UI components.
 * 
 * GET: /<context>/<servlet>/[resource]...
 *  resource - app url resource
 *  url args - passed to all webscript component urls for the page
 * 
 * @author Kevin Roast
 */
public class PageRendererServlet extends WebScriptServlet
{
   private static Log logger = LogFactory.getLog(PageRendererServlet.class);
   
   public static final String CONFIG_ELEMENT = "PageRenderer";
   
   static final String PARAM_COMPONENT_ID  = "_alfId";
   static final String PARAM_COMPONENT_URL = "_alfUrl";
   
   private static final String MIMETYPE_HTML = "text/html;charset=utf-8";
   
   /** bean references */
   private PresentationTemplateProcessor templateProcessor;
   private PresentationTemplateProcessor componentTemplateProcessor;
   private PresentationScriptProcessor scriptProcessor;
   private Registry webscriptsRegistry;
   
   /** stores */
   private Store pageStore;
   private Store templateStore;
   private Store templateConfigStore;
   private Store componentStore;
   
   /** thread-safe cache of page level components */
   private Map<String, CacheValue<PageComponent>> componentCache =
      Collections.synchronizedMap(new HashMap<String, CacheValue<PageComponent>>());
   
   
   @Override
   public void init() throws ServletException
   {
      super.init();
      
      // init required beans - webscript beans, stores, template and script processors
      ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
      
      webscriptsRegistry = (Registry)context.getBean("pagerenderer.registry");
      pageStore = (Store)context.getBean("pagerenderer.pagestore");
      pageStore.init();
      componentStore = (Store)context.getBean("pagerenderer.componentstore");
      componentStore.init();
      templateStore = (Store)context.getBean("pagerenderer.templatestore");
      templateStore.init();
      templateConfigStore = (Store)context.getBean("pagerenderer.templateconfigstore");
      templateConfigStore.init();
      templateProcessor = (PresentationTemplateProcessor)context.getBean("pagerenderer.templateprocessor");
      componentTemplateProcessor = (PresentationTemplateProcessor)context.getBean("pagerenderer.webscripts.templateprocessor");
      scriptProcessor = (PresentationScriptProcessor)context.getBean("pagerenderer.webscripts.scriptprocessor");
      
      // we use a specific config service instance
      configService = (ConfigService)context.getBean("pagerenderer.config");
      
      // we use a specific webscript container instance - override the one from the super
      container = (RuntimeContainer)context.getBean("pagerenderer.container");
   }

   @Override
   protected void service(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      String uri = req.getRequestURI();
      
      long startTime = 0;
      if (logger.isDebugEnabled())
      {
         String qs = req.getQueryString();
         logger.debug("Processing Page Renderer URL: ("  + req.getMethod() + ") " + uri + 
               ((qs != null && qs.length() != 0) ? ("?" + qs) : ""));
         startTime = System.nanoTime();
      }
      
      // skip server context path and build the path to the resource we are looking for
      uri = uri.substring(req.getContextPath().length());
      
      // validate and return the resource path - stripping the servlet context
      StringTokenizer t = new StringTokenizer(uri, "/");
      String servletName = t.nextToken();
      if (!t.hasMoreTokens())
      {
         throw new PageRendererException("Invalid URL: " + uri);
      }
      String resource = uri = uri.substring(servletName.length() + 1);
      
      // get URL arguments as a map ready for building the page context
      Map<String, String> args = new HashMap<String, String>(req.getParameterMap().size(), 1.0f);
      Enumeration names = req.getParameterNames();
      while (names.hasMoreElements())
      {
         String name = (String)names.nextElement();
         args.put(name, req.getParameter(name));
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Page Renderer resource URL: " + resource + " args: " + args);
      
      // retrieve the page instance as per resource url - will
      // return null if the page fails to retrieve from the given page store
      PageInstance page = findPageInstance(this.pageStore, resource);
      if (page == null)
      {
         logger.warn("Unable to locate page instance in page store for resource: " + resource);
         res.setStatus(HttpServletResponse.SC_NOT_FOUND, resource);
         return;
      }
      
      if (logger.isDebugEnabled())
         logger.debug("PageInstance: " + page.toString());
      
      // TODO: what caching here...?
      setNoCacheHeaders(res);
      
      // set response content type and charset
      res.setContentType(MIMETYPE_HTML);
      
      try
      {
         // TODO: reimplement using the new connector abstraction package
         // authenticate - or redirect to login page
         AuthenticationResult auth = PageAuthenticationServlet.authenticate(req, page.getAuthentication());
         if (auth.Success)
         {
            // apply the ticket to the WebScript runtime container for the current thread
            ((PageRendererRuntimeContainer)container).setTicket(auth.Ticket);
            
            // Setup the PageRenderer context for the webscript runtime to use when processing page components
            PageRendererContext context = createPageContext(req, uri, args, page);
            
            // handle a clicked UI component link - look for id+url
            // TODO: keep further state of page? i.e. multiple webscripts can be hosted and clicked
            String compId = req.getParameter(PARAM_COMPONENT_ID);
            if (compId != null)
            {
               String compUrl = req.getParameter(PARAM_COMPONENT_URL);
               if (logger.isDebugEnabled())
                  logger.debug("Clicked component found: " + compId + " URL: " + compUrl);
               context.ComponentId = compId;
               context.ComponentUrl = compUrl;
            }
            
            if (logger.isDebugEnabled())
               logger.debug("Page template resolved as: " + page.getTemplate());
            
            // execute the template to render the page - based on the current page definition
            try
            {
               processTemplatePage(context, page, req, res);
            }
            finally
            {
               // clean up
               res.getWriter().flush();
               res.getWriter().close();
            }
         }
         else
         {
            PageAuthenticationServlet.redirectToLoginPage(req, res, configService);
         }
         if (logger.isDebugEnabled())
         {
            long endTime = System.nanoTime();
            logger.debug("Page render completed in: " + (endTime - startTime)/1000000f + "ms");
         }
      }
      catch (Throwable err)
      {
         throw new PageRendererException("Error occurred during page rendering.\nResource: " +
               resource + "\nError: " + err.getMessage(), err);
      }
   }

   /**
    * Find a PageInstance representing the resource from the given store
    * 
    * @return the PageInstance if found or null otherwise
    */
   private static PageInstance findPageInstance(Store store, String resource)
   {
      String pagePath = resource + ".xml";
      if (store.hasDocument(pagePath))
      {
         return new PageInstance(store, pagePath);
      }
      else
      {
         return null;
      }
   }
   
   /**
    * @return a PageRendererContext instance for this page render request.
    */
   private PageRendererContext createPageContext(HttpServletRequest req, String uri, Map<String, String> args, PageInstance page)
   {
      PageRendererContext context = new PageRendererContext();
      context.ServletRequest = req;
      context.ServerProperties = serverProperties;
      context.RuntimeContainer = container;
      context.RequestURI = uri;
      context.RequestPath = req.getContextPath();
      context.PageInstance = page;
      context.Tokens = args;
      context.PageComponentModel = buildPageComponentModel(page, req);
      return context;
   }
   
   /**
    * Execute the template to render the main page - based on the specified page instance.
    * 
    * @throws IOException
    */
   private void processTemplatePage(
         PageRendererContext context, PageInstance page, HttpServletRequest req, HttpServletResponse res)
      throws IOException
   {
      // load the config for the specific template instance - then resolve the template and behaviour
      String templateConfig = page.getTemplate() + ".xml";
      if (this.templateConfigStore.hasDocument(templateConfig))
      {
         // load and parse the template instance config document
         TemplateInstanceConfig templateInstance = new TemplateInstanceConfig(
               this.templateConfigStore, templateConfig); 
         
         // the optional 'format' template support i.e ".format.ftl"
         String format = req.getParameter("format");
         
         // build template name by convention:
         // templatename[.format].ftl
         String template =
            templateInstance.getTemplateType() + 
            ((format != null && format.length() != 0) ? ("." + format + ".ftl") : ".ftl");
         
         // We need to preprocess the template to execute all @region directives
         // - component dependancies are resolved only when they have all executed.
         // Output to a dummy writer as we don't process the result - the custom
         // directive is aware of the active/passive rendering mode and will either lookup
         // the component or execute the component based on the mode.
         
         long startTime = 0;
         if (logger.isDebugEnabled())
         {
            logger.debug("Executing 1st template pass, looking up components...");
            startTime = System.nanoTime();
         }
         
         Map<String, Object> resultModel = new HashMap<String, Object>(8, 1.0f);
         Map<String, Object> templateModel = buildTemplateModel(context, page, req, false);
         
         // add the template config values directly to the template root model - this is useful for
         // templates that do not require additional processing in JavaScript, they just need the values
         templateModel.putAll(templateInstance.getPropetries());
         
         // execute any attached javascript behaviour for this template
         // the behaviour plus the config is responsible for specialising the template
         String scriptPath = templateInstance.getTemplateType() + ".js";
         ScriptContent script = templateStore.getScriptLoader().getScript(scriptPath);
         if (script != null)
         {
            Map<String, Object> scriptModel = new HashMap<String, Object>(8, 1.0f);
            // add the template config properties to the script model
            scriptModel.putAll(templateInstance.getPropetries());
            // results from the script should be placed into the root 'model' object
            scriptModel.put("model", resultModel);
            
            scriptProcessor.executeScript(script, scriptModel);
            
            // merge script results model into the template model
            for (Map.Entry<String, Object> entry : resultModel.entrySet())
            {
               // retrieve script model value and unwrap each java object from script object
               Object value = entry.getValue();
               Object templateValue = scriptProcessor.unwrapValue(value);
               templateModel.put(entry.getKey(), templateValue);
            }
         }
         
         // First pass is very fast as template pages themselves have very little implicit content and
         // any associated behaviour logic is executed only once, with the result stored for the 2nd pass.
         // The critical performance path is in executing the WebScript components - which is only
         // performed during the second pass of the template - once component references are all resolved.
         templateProcessor.process(template, templateModel,
            new Writer ()
            {
               public void write(char[] cbuf, int off, int len) throws IOException
               {
               }
            
               public void flush() throws IOException
               {
               }
            
               public void close() throws IOException
               {
               }
            });
         
         if (logger.isDebugEnabled())
         {
            long endTime = System.nanoTime();
            logger.debug("...1st pass processed in: " + (endTime - startTime)/1000000f + "ms");
            logger.debug("Executing 2nd template pass, rendering...");
            startTime = System.nanoTime();
         }
         
         // construct template model for 2nd pass
         templateModel = buildTemplateModel(context, page, req, true);
         templateModel.putAll(templateInstance.getPropetries());
         if (script != null)
         {
            // script already executed - so just merge script return model into the template model
            for (Map.Entry<String, Object> entry : resultModel.entrySet())
            {
               // retrieve script model value
               Object value = entry.getValue();
               Object templateValue = scriptProcessor.unwrapValue(value);
               templateModel.put(entry.getKey(), templateValue);
            }
         }
         templateProcessor.process(template, templateModel, res.getWriter());
         
         if (logger.isDebugEnabled())
         {
            long endTime = System.nanoTime();
            logger.debug("...2nd pass processed in: " + (endTime - startTime)/1000000f + "ms");
         }
      }
      else
      {
         throw new PageRendererException("Unable to find template config: " + templateConfig);
      }
   }
   
   /**
    * Build the model for page template execution. Responsible for added the special 'region' directive
    * to the model. The region directive executes in one of two modes, active and passive. See the docs
    * for the RegionDirective class for more information.
    * 
    * @param context    PageRendererContext for this thread
    * @param page       the page object
    * @param req        Request (for source url)
    * @param active     True for 'active' region directive processing mode, false for 'passive' mode.
    * 
    * @return model to use for UI Component template page execution
    */
   private Map<String, Object> buildTemplateModel(
         PageRendererContext context, PageInstance page, HttpServletRequest req, boolean active)
   {
      Map<String, Object> model = new HashMap<String, Object>(8);
      
      URLHelper urlHelper = new URLHelper(req);
      model.put("url", urlHelper);
      model.put("description", page.getDescription());
      model.put("title", page.getTitle());
      model.put("theme", page.getTheme());
      model.put("head", page.getHeaderRenderer(webscriptsRegistry, componentTemplateProcessor, urlHelper));
      
      // add the custom 'region' directive implementation - one instance per model as we pass in template/page 
      model.put("region", new RegionDirective(context, componentStore, componentCache, page, active));
      
      return model;
   }
   
   /**
    * Build the model available to each component to represent the current page meta. 
    * 
    * @return model to use for UI Component execution against a page.
    */
   private Map<String, Object> buildPageComponentModel(
         PageInstance page, HttpServletRequest req)
   {
      Map<String, Object> model = new HashMap<String, Object>(4);
      Map<String, Object> pageModel = new HashMap<String, Object>(4);
      URLHelper urlHelper = new URLHelper(req);
      pageModel.put("url", urlHelper);
      pageModel.put("theme", page.getTheme());
      // TODO: add page url arguments as Map model "page.args"
      model.put("page", pageModel);
      
      return model;
   }
   
   /**
    * @return the configuration object for the PageRenderer
    */
   private Config getConfig()
   {
      Config config = this.configService.getConfig(CONFIG_ELEMENT);
      if (config == null)
      {
         throw new PageRendererException("Cannot find required config element 'PageRenderer'.");
      }
      return config;
   }
   
   /**
    * Apply the headers required to disallow caching of the response in the browser
    */
   private static void setNoCacheHeaders(HttpServletResponse res)
   {
      res.setHeader("Cache-Control", "no-cache");
      res.setHeader("Pragma", "no-cache");
   }

   
   /**
    * Simple structure class representing the current thread request context for a page.
    */
   public static class PageRendererContext
   {
      HttpServletRequest ServletRequest;
      ServerProperties ServerProperties;
      RuntimeContainer RuntimeContainer;
      PageInstance PageInstance;
      String RequestURI;
      String RequestPath;
      String ComponentId;
      String ComponentUrl;
      Map<String, String> Tokens;
      Map<String, Object> PageComponentModel;
   }
}