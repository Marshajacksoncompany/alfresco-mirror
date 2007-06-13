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
package org.alfresco.repo.template;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.processor.BaseProcessor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ProcessorExtension;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateProcessor;
import org.alfresco.service.cmr.repository.TemplateProcessorExtension;
import org.alfresco.service.cmr.repository.TemplateService;
import org.apache.log4j.Logger;

import freemarker.cache.MruCacheStorage;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * FreeMarker implementation of the template processor interface.
 * <p>
 * Service to process FreeMarker template files loaded from various sources including
 * the classpath, repository and directly from a String.
 * <p>
 * The template is processed against a data model generally consisting of a map of
 * named objects. FreeMarker can natively handle any POJO objects using standard bean
 * notation syntax. It has support for walking List objects. A 'standard' data model
 * helper is provided to help generate an object model containing well known objects
 * such as the Company Home, User Home and current User nodes. It also provides helpful
 * util classes to process Date objects and repository specific custom methods. 
 * 
 * @author Kevin Roast
 */
public class FreeMarkerProcessor extends BaseProcessor implements TemplateProcessor
{
    private final static String MSG_ERROR_NO_TEMPLATE   = "error_no_template";
    private final static String MSG_ERROR_TEMPLATE_FAIL = "error_template_fail";
    private final static String MSG_ERROR_TEMPLATE_IO   = "error_template_io";
    
    private static final Logger logger = Logger.getLogger(FreeMarkerProcessor.class);
    
    /** Pseudo path to String based template */
    private static final String PATH = "string://fixed";
    
    /** Template encoding */
    private String defaultEncoding;
    
    /**
     * Set the default template encoding
     * 
     * @param defaultEncoding  the default encoding 
     */
    public void setDefaultEncoding(String defaultEncoding)
    {
        this.defaultEncoding = defaultEncoding;
    }
    
    /**
     * Get the FreeMarker configuration for this instance
     * 
     * @return FreeMarker configuration
     */
    protected Configuration getConfig()
    {
        Configuration config = new Configuration();
        
        // setup template cache
        config.setCacheStorage(new MruCacheStorage(2, 0));
        
        // use our custom loader to find templates on the ClassPath
        config.setTemplateLoader(new ClassPathRepoTemplateLoader(this.services.getNodeService(), this.services.getContentService()));
        
        // use our custom object wrapper that can deal with QNameMap objects directly
        config.setObjectWrapper(new QNameAwareObjectWrapper());
        
        // rethrow any exception so we can deal with them
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        // localized template lookups off by default - as they create strange noderef lookups
        // such as workspace://SpacesStore/01234_en_GB - causes problems for ns.exists() on DB2
        config.setLocalizedLookup(false);
        
        // set default template encoding
        if (defaultEncoding != null)
        {
            config.setDefaultEncoding(defaultEncoding);
        }
        
        return config;
    }
    
    /**
     * FreeMarker configuration for loading the specified template directly from a String
     * 
     * @param path      Pseudo Path to the template
     * @param template  Template content
     * 
     * @return FreeMarker configuration
     */
    protected Configuration getStringConfig(String path, String template)
    {
        Configuration config = new Configuration();
        
        // setup template cache
        config.setCacheStorage(new MruCacheStorage(2, 0));
        
        // use our custom loader to load a template directly from a String
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate(path, template);
        config.setTemplateLoader(stringTemplateLoader);
        
        // use our custom object wrapper that can deal with QNameMap objects directly
        config.setObjectWrapper(new QNameAwareObjectWrapper());
        
        // rethrow any exception so we can deal with them
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        // set default template encoding
        if (defaultEncoding != null)
        {
            config.setDefaultEncoding(defaultEncoding);
        }
        
        return config;
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.TemplateProcessor#process(java.lang.String, java.lang.Object, java.io.Writer)
     */
    public void process(String template, Object model, Writer out)
    {
        if (template == null || template.length() == 0)
        {
            throw new IllegalArgumentException("Template name is mandatory.");
        }
        if (model == null)
        {
            throw new IllegalArgumentException("Model is mandatory.");
        }
        if (out == null)
        {
            throw new IllegalArgumentException("Output Writer is mandatory.");
        }
        
        try
        {
            long startTime = 0;
            if (logger.isDebugEnabled())
            {
                logger.debug("Executing template: " + template);// + " on model: " + model);
                startTime = System.currentTimeMillis();
            }
            
            Template t = getConfig().getTemplate(template);
            if (t != null)
            {
                try
                {
                    // perform the template processing against supplied data model
                    Object freeMarkerModel = convertToFreeMarkerModel(model);
                    t.process(freeMarkerModel, out);
                }
                catch (Throwable err)
                {
                    throw new TemplateException(MSG_ERROR_TEMPLATE_FAIL, new Object[] {err.getMessage()}, err);
                }
            }
            else
            {
                throw new TemplateException(MSG_ERROR_NO_TEMPLATE, new Object[] {template});
            }
            
            if (logger.isDebugEnabled())
            {
                long endTime = System.currentTimeMillis();
                logger.debug("Time to execute template: " + (endTime - startTime) + "ms");
            }
        }
        catch (IOException ioerr)
        {
            throw new TemplateException(MSG_ERROR_TEMPLATE_IO, new Object[] {template}, ioerr);
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.TemplateProcessor#processString(java.lang.String, java.lang.Object, java.io.Writer)
     */
    public void processString(String template, Object model, Writer out)
    {
        if (template == null || template.length() == 0)
        {
            throw new IllegalArgumentException("Template is mandatory.");
        }
        if (model == null)
        {
            throw new IllegalArgumentException("Model is mandatory.");
        }
        if (out == null)
        {
            throw new IllegalArgumentException("Output Writer is mandatory.");
        }
        
        try
        {
            long startTime = 0;
            if (logger.isDebugEnabled())
            {
                logger.debug("Executing template: " + template);// + " on model: " + model);
                startTime = System.currentTimeMillis();
            }
            
            Template t = getStringConfig(PATH, template).getTemplate(PATH);
            if (t != null)
            {
                try
                {
                    // perform the template processing against supplied data model
                    Object freeMarkerModel = convertToFreeMarkerModel(model);
                    t.process(freeMarkerModel, out);
                    
                    if (logger.isDebugEnabled())
                    {
                        long endTime = System.currentTimeMillis();
                        logger.debug("Time to execute template: " + (endTime - startTime) + "ms");
                    }
                }
                catch (Throwable err)
                {
                    throw new TemplateException(MSG_ERROR_TEMPLATE_FAIL, new Object[] {err.getMessage()}, err);
                }
            }
            else
            {
                throw new TemplateException(MSG_ERROR_NO_TEMPLATE, new Object[] {template});
            }
        }
        catch (IOException ioerr)
        {
            throw new TemplateException(MSG_ERROR_TEMPLATE_IO, new Object[] {template}, ioerr);
        }
    }
    
    /**
     * Converts the passed model into a FreeMarker model
     * 
     * @param model     the model
     * 
     * @return Object the converted model
     */
    private Object convertToFreeMarkerModel(Object model)
    {
        // If we dont have a map in our hand we just return the passes model
        if (model instanceof Map)
        {
            Map<String, Object> freeMarkerModel = new HashMap<String, Object>(((Map)model).size());

            // Look for the image resolver in the model
            TemplateImageResolver imageResolver = (TemplateImageResolver)((Map)model).get(TemplateService.KEY_IMAGE_RESOLVER);

            // add the template extensions to the model
            // the extensions include custom root helper objects and custom template method objects
            for (ProcessorExtension ext : this.processorExtensions.values())
            {
                if (ext instanceof TemplateProcessorExtension)
                {
                    ((TemplateProcessorExtension)ext).setTemplateImageResolver(imageResolver);
                }
                freeMarkerModel.put(ext.getExtensionName(), ext);
            }

            Map<String, Object> value = (Map<String, Object>)convertValue(model, imageResolver);
            freeMarkerModel.putAll(value);
            return freeMarkerModel;
        }
        else
        {
            return convertValue(model, null);
        }
    }

    /**
     * Converts a value to a freemarker value
     * 
     * @param value
     * @param imageResolver
     * @return
     */
    private Object convertValue(Object value, TemplateImageResolver imageResolver)
    {
        if (value instanceof NodeRef)
        {
            return new TemplateNode((NodeRef)value, this.services, imageResolver);
        }
        
        else if (value instanceof Map)
        {
            Map<String, Object> map = (Map<String, Object>)value;
            Map<String, Object> convertedMap = new HashMap<String, Object>(map.size());
            for (String key : map.keySet())
            {
                if (key.equals(TemplateService.KEY_IMAGE_RESOLVER) == false)
                {
                    Object mapValue = map.get(key);
                    convertedMap.put(key, convertValue(mapValue, imageResolver));
                }
            }
            return convertedMap;
        }
        
        else if (value instanceof List)
        {
            List<Object> list = (List<Object>)value;
            List<Object> convertedList = new ArrayList<Object>(list.size());
            for (Object listVal : list)
            {
                convertedList.add(convertValue(listVal, imageResolver));
            }
            return convertedList;
        }
        
        else if (value instanceof Object[])
        {
            Object[] array = (Object[])value;
            Object[] convertedArray = new Object[array.length];
            int i = 0;
            for (Object item : array)
            {
                convertedArray[i++] = convertValue(item, imageResolver);
            }
            return convertedArray;
        }
        
        return value;
    }
    
}
