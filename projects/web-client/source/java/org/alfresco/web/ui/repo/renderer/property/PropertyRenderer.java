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
package org.alfresco.web.ui.repo.renderer.property;

import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.renderer.BaseRenderer;
import org.alfresco.web.ui.repo.component.property.UIProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Renderer for the UIProperty component
 * 
 * @author gavinc
 */
public class PropertyRenderer extends BaseRenderer
{
   private static Log logger = LogFactory.getLog(PropertyRenderer.class);
   
   /**
    * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeBegin(FacesContext context, UIComponent component) throws IOException
   {
      if (component.isRendered() == false)
      {
         return;
      }

      // NOTE: we close off the first <td> generated by the property sheet's grid renderer
      context.getResponseWriter().write("</td>");
   }

   /**
    * @see javax.faces.render.Renderer#encodeChildren(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeChildren(FacesContext context, UIComponent component) throws IOException
   {
      if (component.isRendered() == false)
      {
         return;
      }
      
      UIProperty property = (UIProperty)component;
      ResponseWriter out = context.getResponseWriter();
      
      // make sure there are exactly 2 child components
      int count = property.getChildCount();
      
      //if (logger.isDebugEnabled())
      //   logger.debug("property has " + count + " children");
      
      if (count == 2)
      {
         // get the label and the control
         List<UIComponent> kids = property.getChildren();
         UIComponent label = kids.get(0);
         UIComponent control = kids.get(1);
         
         // place a style class on the label column if necessary
         String labelStylceClass = (String)property.getParent().getAttributes().get("labelStyleClass");
         out.write("</td><td");
         if (labelStylceClass != null)
         {
            outputAttribute(out, labelStylceClass, "class");
         }
         
         // close the <td> 
         out.write(">");
         // encode the label
         Utils.encodeRecursive(context, label);
         // encode the control
         context.getResponseWriter().write("</td><td>");
         Utils.encodeRecursive(context, control);
         
         // NOTE: we'll allow the property sheet's grid renderer close off the last <td>
      }
   }

   /**
    * @see javax.faces.render.Renderer#encodeEnd(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    */
   public void encodeEnd(FacesContext context, UIComponent component) throws IOException
   {
      // we don't need to do anything in here
   }

   /**
    * @see javax.faces.render.Renderer#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return true;
   }

}
