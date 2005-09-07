/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.config;

import java.util.Iterator;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.dom4j.Element;

/**
 * Custom element reader to parse config for client config values
 * 
 * @author Kevin Roast
 */
public class ClientElementReader implements ConfigElementReader
{
   public static final String ELEMENT_CLIENT = "client";
   public static final String ELEMENT_PAGESIZE = "page-size";
   public static final String ELEMENT_LIST = "list";
   public static final String ELEMENT_DETAILS = "details";
   public static final String ELEMENT_ICONS = "icons";
   public static final String ELEMENT_DEFAULTVIEW = "default-view";
   public static final String ELEMENT_RECENTSPACESITEMS = "recent-spaces-items";
   public static final String ELEMENT_LANGUAGES = "languages";
   public static final String ELEMENT_LANGUAGE = "language";
   public static final String ATTRIBUTE_LOCALE = "locale";
   public static final String ELEMENT_TEMPLATES = "templates";
   public static final String ELEMENT_ENGINE = "engine";
   public static final String ATTRIBUTE_NAME = "name";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   public ConfigElement parse(Element element)
   {
      ClientConfigElement configElement = null;
      
      if (element != null)
      {
         String name = element.getName();
         if (name.equals(ELEMENT_CLIENT) == false)
         {
            throw new ConfigException("ClientElementReader can only parse " +
                  ELEMENT_CLIENT + "elements, the element passed was '" + name + "'");
         }
         
         configElement = new ClientConfigElement();
         
         // get the page size sub-element
         Element pageSize = element.element(ELEMENT_PAGESIZE);
         if (pageSize != null)
         {
            {
               Element viewPageSize = pageSize.element(ELEMENT_LIST);
               if (viewPageSize != null)
               {
                  configElement.setListPageSize(Integer.parseInt(viewPageSize.getTextTrim()));
               }
            }
            {
               Element viewPageSize = pageSize.element(ELEMENT_DETAILS);
               if (viewPageSize != null)
               {
                  configElement.setDetailsPageSize(Integer.parseInt(viewPageSize.getTextTrim()));
               }
            }
            {
               Element viewPageSize = pageSize.element(ELEMENT_ICONS);
               if (viewPageSize != null)
               {
                  configElement.setIconsPageSize(Integer.parseInt(viewPageSize.getTextTrim()));
               }
            }
         }
         
         // get the languages sub-element
         Element languages = element.element(ELEMENT_LANGUAGES);
         if (languages != null)
         {
            Iterator<Element> langsItr = languages.elementIterator(ELEMENT_LANGUAGE);
            while (langsItr.hasNext())
            {
               Element language = langsItr.next();
               String localeCode = language.attributeValue(ATTRIBUTE_LOCALE);
               String label = language.getTextTrim();
               
               if (localeCode != null && localeCode.length() != 0 &&
                   label != null && label.length() != 0)
               {
                  // store the language code against the display label
                  configElement.addLanguage(localeCode, label);
               }
            }
         }
         
         // get the template sub-element
         Element templates = element.element(ELEMENT_TEMPLATES);
         if (templates != null)
         {
            Iterator<Element> tempItr = templates.elementIterator(ELEMENT_ENGINE);
            while (tempItr.hasNext())
            {
               Element engine = tempItr.next();
               String templateName = engine.attributeValue(ATTRIBUTE_NAME);
               String templateEngine = engine.getTextTrim();
               
               if (templateName != null && templateName.length() != 0 &&
                   templateEngine != null && templateEngine.length() != 0)
               {
                  // store the template processor class name against the unique name
                  configElement.addTemplateProcessor(templateName, templateEngine);
               }
            }
         }
         
         // get the default view mode
         Element defaultView = element.element(ELEMENT_DEFAULTVIEW);
         if (defaultView != null)
         {
            configElement.setDefaultView(defaultView.getTextTrim());
         }
         
         // get the recent space max items
         Element recentSpaces = element.element(ELEMENT_RECENTSPACESITEMS);
         if (recentSpaces != null)
         {
            configElement.setRecentSpacesItems(Integer.parseInt(recentSpaces.getTextTrim()));
         }
      }
      
      return configElement;
   }
}
