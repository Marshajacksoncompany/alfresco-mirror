/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.ui.repo.component.property;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.UISelectBoolean;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.PropertyTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.converter.XMLDateConverter;
import org.alfresco.web.ui.repo.component.UICategorySelector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Component to represent an individual property
 * 
 * @author gavinc
 */
public class UIProperty extends UIPanel implements NamingContainer
{
   private static Log logger = LogFactory.getLog(UIProperty.class);
   
   private String name;
   private String displayLabel;
   private String converter;
   private Boolean readOnly;
   
   /**
    * Default constructor
    */
   public UIProperty()
   {
      // set the default renderer
      setRendererType("org.alfresco.faces.PropertyRenderer");
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Property";
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      // get the variable being used from the parent
      UIComponent parent = this.getParent();
      if ((parent instanceof UIPropertySheet) == false)
      {
         throw new IllegalStateException("The property component must be nested within a property sheet component");
      }
      
      // only build the components if there are currently no children
      int howManyKids = getChildren().size();
      if (howManyKids == 0)
      {
         Node node = ((UIPropertySheet)parent).getNode();
         String var = ((UIPropertySheet)parent).getVar();
         String propertyName = (String)getName();
   
         DataDictionary dd = (DataDictionary)FacesContextUtils.getRequiredWebApplicationContext(
               context).getBean(Application.BEAN_DATA_DICTIONARY);
         PropertyDefinition propDef = dd.getPropertyDefinition(node, propertyName);
         
         if (propDef == null)
         {
            logger.warn("Failed to find property definition for property '" + propertyName + "'");
            
            // there is no definition for the node, so it may have been added to
            // the node as an additional property, so look for it in the node itself
            if (node.hasProperty(propertyName))
            {
               String displayLabel = (String)getDisplayLabel();
               if (displayLabel == null)
               {
                  displayLabel = propertyName;
               }
               
               // generate the label and generic control
               generateLabel(context, displayLabel);
               generateControl(context, propertyName, var);
            }
            else
            {
               // add an error message as the property is not defined in the data dictionary and 
               // not in the node's set of properties
               String msg = "Property '"+ propertyName + "' is not available for this node";
               Utils.addErrorMessage(msg);
               
               if (logger.isDebugEnabled())
                  logger.debug("Added global error message: " + msg);
            }
         }
         else
         {
            String displayLabel = (String)getDisplayLabel();
            if (displayLabel == null)
            {
               // try and get the repository assigned label
               displayLabel = propDef.getTitle();
               
               // if the label is still null default to the local name of the property
               if (displayLabel == null)
               {
                  displayLabel = propDef.getName().getLocalName();
               }
            }
            
            // generate the label and type specific control
            generateLabel(context, displayLabel);
            generateControl(context, propDef, var);
         }
      }
      
      super.encodeBegin(context);
   }
   
   /**
    * @return Returns the display label
    */
   public String getDisplayLabel()
   {
      if (this.displayLabel == null)
      {
         ValueBinding vb = getValueBinding("displayLabel");
         if (vb != null)
         {
            this.displayLabel = (String)vb.getValue(getFacesContext());
         }
      }
      
      return this.displayLabel;
   }

   /**
    * @param displayLabel Sets the display label
    */
   public void setDisplayLabel(String displayLabel)
   {
      this.displayLabel = displayLabel;
   }

   /**
    * @return Returns the name
    */
   public String getName()
   {
      if (this.name == null)
      {
         ValueBinding vb = getValueBinding("name");
         if (vb != null)
         {
            this.name = (String)vb.getValue(getFacesContext());
         }
      }
      
      return this.name;
   }

   /**
    * @param name Sets the name
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * @return Returns the converter
    */
   public String getConverter()
   {
      if (this.converter == null)
      {
         ValueBinding vb = getValueBinding("converter");
         if (vb != null)
         {
            this.converter = (String)vb.getValue(getFacesContext());
         }
      }
      
      return this.converter;
   }

   /**
    * @param converter Sets the converter
    */
   public void setConverter(String converter)
   {
      this.converter = converter;
   }

   /**
    * @return Returns whether the property is read only
    */
   public boolean isReadOnly()
   {
      if (this.readOnly == null)
      {
         ValueBinding vb = getValueBinding("readOnly");
         if (vb != null)
         {
            this.readOnly = (Boolean)vb.getValue(getFacesContext());
         }
      }
      
      if (this.readOnly == null)
      {
         this.readOnly = Boolean.FALSE;
      }
      
      return this.readOnly;
   }

   /**
    * @param readOnly Sets the read only flag for the component
    */
   public void setReadOnly(boolean readOnly)
   {
      this.readOnly = readOnly;
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.name = (String)values[1];
      this.displayLabel = (String)values[2];
      this.readOnly = (Boolean)values[3];
      this.converter = (String)values[4];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[5];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.name;
      values[2] = this.displayLabel;
      values[3] = this.readOnly;
      values[4] = this.converter;
      return (values);
   }
   
   /**
    * Generates a JSF OutputText component/renderer
    * 
    * @param context JSF context
    * @param displayLabel The display label text
    * @param parent The parent component for the label
    */
   private void generateLabel(FacesContext context, String displayLabel)
   {
      UIOutput label = (UIOutput)context.getApplication().
                        createComponent("javax.faces.Output");
      label.setId(context.getViewRoot().createUniqueId());
      label.setRendererType("javax.faces.Text");
      label.setValue(displayLabel + ": ");
      this.getChildren().add(label);
      
      if (logger.isDebugEnabled())
         logger.debug("Created label " + label.getClientId(context) + 
                      " for '" + displayLabel + "' and added it to component " + this);
   }
   
   /**
    * Generates an appropriate control for the given property
    * 
    * @param context JSF context
    * @param propDef The definition of the property to create the control for
    * @param varName Name of the variable the node is stored in the session as 
    *                (used for value binding expression)
    * @param parent The parent component for the control
    */
   private void generateControl(FacesContext context, PropertyDefinition propDef, 
                                String varName)
   {
      UIOutput control = null;
      ValueBinding vb = context.getApplication().
                        createValueBinding("#{" + varName + ".properties[\"" + 
                        propDef.getName().toString() + "\"]}");
      
      UIPropertySheet propSheet = (UIPropertySheet)this.getParent();
      
      PropertyTypeDefinition propTypeDef = propDef.getPropertyType();
      QName typeName = propTypeDef.getName();
         
      if (propSheet.getMode().equalsIgnoreCase(UIPropertySheet.VIEW_MODE))
      {
         // if we are in view mode simply output the text to the screen
         control = (UIOutput)context.getApplication().createComponent("javax.faces.Output");
         control.setRendererType("javax.faces.Text");
         
         // if it is a date or datetime property add the converter
         if (typeName.equals(PropertyTypeDefinition.DATE) || 
                  typeName.equals(PropertyTypeDefinition.DATETIME))
         {
            XMLDateConverter conv = (XMLDateConverter)context.getApplication().
               createConverter("org.alfresco.faces.XMLDataConverter");
            conv.setType("both");
            conv.setDateStyle("long");
            conv.setTimeStyle("short");
            control.setConverter(conv);
         }
      }
      else
      {
         // generate the appropriate input field 
         if (typeName.equals(PropertyTypeDefinition.BOOLEAN))
         {
            control = (UISelectBoolean)context.getApplication().
                  createComponent("javax.faces.SelectBoolean");
            control.setRendererType("javax.faces.Checkbox");
         }
         else if (typeName.equals(PropertyTypeDefinition.CATEGORY))
         {
            control = (UICategorySelector)context.getApplication().
                  createComponent("org.alfresco.faces.CategorySelector");
         }
         else if (typeName.equals(PropertyTypeDefinition.DATE) || 
                  typeName.equals(PropertyTypeDefinition.DATETIME))
         {
            control = (UIInput)context.getApplication().
                  createComponent("javax.faces.Input");
            control.setRendererType("org.alfresco.faces.DatePickerRenderer");
            control.getAttributes().put("startYear", new Integer(1970));
            control.getAttributes().put("yearCount", new Integer(50));
            control.getAttributes().put("style", "margin-right: 7px;");
         }
         else
         {
            // any other type is represented as an input text field
            control = (UIInput)context.getApplication().
                  createComponent("javax.faces.Input");
            control.setRendererType("javax.faces.Text");
            control.getAttributes().put("size", "35");
            control.getAttributes().put("maxlength", "1024");
         }
      
         // set control to disabled state if set to read only or if the
         // property definition says it is protected
         if (isReadOnly() || propDef.isProtected())
         {
            control.getAttributes().put("disabled", Boolean.TRUE);
         }
         
         // add a validator if the field is required
//         if (propDef.isMandatory())
//         {
//            control.setRequired(true);
//            LengthValidator val = (LengthValidator)context.getApplication().
//                                   createValidator("javax.faces.Length");
//            val.setMinimum(1);
//            control.addValidator(val);
//         }   
      }
      
      // set up the common aspects of the control
      control.setId(context.getViewRoot().createUniqueId());
      control.setValueBinding("value", vb);
      
      // if a converter has been specified we need to instantiate it
      // and apply it to the control
      if (getConverter() != null)
      {
         // catch null pointer exception to workaround bug in myfaces
         try
         {
            Converter conv = context.getApplication().createConverter(getConverter());
            control.setConverter(conv);
         }
         catch (FacesException fe)
         {
            logger.warn("Converter " + getConverter() + " could not be applied");
         }
      }
      
      // add the control itself
      this.getChildren().add(control);
      
      if (logger.isDebugEnabled())
         logger.debug("Created control " + control + "(" + 
                      control.getClientId(context) + 
                      ") for '" + propDef.getName().getLocalName() + 
                      "' and added it to component " + this);
   }
   
   /**
    * Generates an appropriate control for the given property name
    * 
    * @param context JSF context
    * @param propName The name of the property to create a control for
    * @param varName Name of the variable the node is stored in the session as 
    *                (used for value binding expression)
    * @param parent The parent component for the control
    */
   private void generateControl(FacesContext context, String propName, 
                                String varName)
   {
      ValueBinding vb = context.getApplication().
                        createValueBinding("#{" + varName + ".properties[\"" + 
                        propName + "\"]}");
      
      UIOutput control = null;
      UIPropertySheet propSheet = (UIPropertySheet)this.getParent();
      if (propSheet.getMode().equalsIgnoreCase(UIPropertySheet.VIEW_MODE))
      {
         // if we are in view mode simply output the text to the screen
         control = (UIOutput)context.getApplication().createComponent("javax.faces.Output");
         control.setRendererType("javax.faces.Text");
      }
      else
      {
         // as we don't know the type of the property we can only output a text field 
         control = (UIInput)context.getApplication().createComponent("javax.faces.Input");
         control.setRendererType("javax.faces.Text");
         control.getAttributes().put("size", "35");
         control.getAttributes().put("maxlength", "1024");
      
         // set control to disabled state if set to read only
         if (isReadOnly())
         {
            control.getAttributes().put("disabled", Boolean.TRUE);
         }
      }
      
      // set the common attributes
      control.setId(context.getViewRoot().createUniqueId());
      control.setValueBinding("value", vb);
      
      // if a converter has been specified we need to instantiate it
      // and apply it to the control
      if (getConverter() != null)
      {
         try
         {
            Converter conv = context.getApplication().createConverter(getConverter());
            control.setConverter(conv);
         }
         catch (FacesException fe)
         {
            logger.warn("Converter " + getConverter() + " could not be applied");
         }
      }
      
      // add the control itself
      this.getChildren().add(control);
      
      if (logger.isDebugEnabled())
         logger.debug("Created control " + control + "(" + 
                      control.getClientId(context) + 
                      ") for '" + propName +  
                      "' and added it to component " + this);
   }
}
