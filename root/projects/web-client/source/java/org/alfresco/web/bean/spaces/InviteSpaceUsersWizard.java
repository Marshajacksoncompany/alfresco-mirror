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
package org.alfresco.web.bean.spaces;

import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wizard.BaseInviteUsersWizard;

/**
 * Concrete implementation providing the ability to invite users to a space.
 * 
 * @author gavinc
 */
public class InviteSpaceUsersWizard extends BaseInviteUsersWizard
{
   private static final long serialVersionUID = -1584891656721183347L;
   
   /** Cache of available folder permissions */
   Set<String> folderPermissions = null;
   
   @Override
   protected Set<String> getPermissionsForType()
   {
      if (this.folderPermissions == null)
      {
         this.folderPermissions = this.permissionService.getSettablePermissions(getNode().getType());
      }
       
      return this.folderPermissions;
   }

   @Override
   protected Node getNode()
   {
      return this.browseBean.getActionSpace();
   }

    @Override
    protected String getEmailTemplateXPath()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        String xpath = Application.getRootPath(fc) + "/" + 
              Application.getGlossaryFolderName(fc) + "/" +
              Application.getEmailTemplatesFolderName(fc) + "/" + 
              Application.getInviteEmailTemplatesFolderName(fc) + "//*";
        return xpath;
    }
}
