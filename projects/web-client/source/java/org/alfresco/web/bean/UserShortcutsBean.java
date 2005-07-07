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
package org.alfresco.web.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.RepositoryAuthenticationDao;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.ValueConverter;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.shelf.UIShortcutsShelfItem;
import org.apache.log4j.Logger;

/**
 * This bean manages the user defined list of Recent Spaces in the Shelf component.
 * 
 * @author Kevin Roast
 */
public class UserShortcutsBean
{
   private static Logger logger = Logger.getLogger(UserShortcutsBean.class);
   
   /** The NodeService to be used by the bean */
   private NodeService nodeService;
   
   /** The BrowseBean reference */
   private BrowseBean browseBean;
   
   /** NamespaceService bean reference */
   private NamespaceService namespaceService;
   
   /** RuleService bean reference */
   private RuleService ruleService;
   
   /** List of shortcut nodes */
   private List<Node> shortcuts = null;
   
   private QName QNAME_SHORTCUTS = QName.createQName(NamespaceService.ALFRESCO_URI, "shortcuts");
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters
   
   /**
    * @param nodeService The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param browseBean The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @param namespaceService The Namespace Service to set.
    */
   public void setNamespaceService(NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
   }
   
   /**
    * @param ruleService Sets the rule service to use
    */
   public void setRuleService(RuleService ruleService)
   {
      this.ruleService = ruleService;
   }
   
   /**
    * @return the List of shortcut Nodes
    */
   public List<Node> getShortcuts()
   {
      if (this.shortcuts == null)
      {
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // get the shortcuts from the preferences for this user
            NodeRef prefRef = getCreateShortcutsNodeRef();
            List<String> shortcuts = (List<String>)this.nodeService.getProperty(prefRef, QNAME_SHORTCUTS);
            if (shortcuts != null)
            {
               // each shortcut node ID is persisted as a list item in a well known property
               this.shortcuts = new ArrayList<Node>(shortcuts.size());
               for (int i=0; i<shortcuts.size(); i++)
               {
                  Node node = new Node(new NodeRef(Repository.getStoreRef(), shortcuts.get(i)), this.nodeService);
                  try
                  {
                     // quick init properties while in the usertransaction
                     node.getProperties();
                     
                     // save ref to the Node for rendering
                     this.shortcuts.add(node);
                  }
                  catch (InvalidNodeRefException nodeErr)
                  {
                     // ignore this shortcut node - no longer exists in the system!
                     // TODO: correct this error in the node list and save back
                  }
               }
            }
            else
            {
               this.shortcuts = new ArrayList<Node>(5);
            }
            
            tx.commit();
         }
         catch (Exception err)
         {
            Utils.addErrorMessage( MessageFormat.format(Repository.ERROR_GENERIC, err.getMessage()), err );
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
      
      return this.shortcuts;
   }
   
   /**
    * @param spaces     List of shortcuts Nodes
    */
   public void setShortcuts(List<Node> nodes)
   {
      this.shortcuts = nodes;
   }
   
   
   // ------------------------------------------------------------------------------
   // Action method handlers
   
   /**
    * Action handler called when a new shortcut is to be added to the list
    */
   public void createShortcut(ActionEvent event)
   {
      // TODO: add this action to the Details screen for Space and Document
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String id = params.get("id");
      if (id != null && id.length() != 0)
      {
         try
         {
            NodeRef ref = new NodeRef(Repository.getStoreRef(), id);
            Node node = new Node(ref, this.nodeService);
            
            boolean foundShortcut = false;
            for (int i=0; i<getShortcuts().size(); i++)
            {
               if (node.getId().equals(getShortcuts().get(i).getId()))
               {
                  // found same node already in the list - so we don't need to add it again
                  foundShortcut = true;
                  break;
               }
            }
            
            if (foundShortcut == false)
            {
               // add to persistent store
               UserTransaction tx = null;
               try
               {
                  FacesContext context = FacesContext.getCurrentInstance();
                  tx = Repository.getUserTransaction(context);
                  tx.begin();
                  
                  NodeRef prefRef = getCreateShortcutsNodeRef();
                  List<String> shortcuts = (List<String>)this.nodeService.getProperty(prefRef, QNAME_SHORTCUTS);
                  if (shortcuts == null)
                  {
                     shortcuts = new ArrayList<String>(1);
                  }
                  shortcuts.add(node.getNodeRef().getId());
                  this.nodeService.setProperty(prefRef, QNAME_SHORTCUTS, (Serializable)shortcuts);
                  
                  // commit the transaction
                  tx.commit();
                  
                  // add our new shortcut Node to the in-memory list
                  getShortcuts().add(node);
                  
                  if (logger.isDebugEnabled())
                     logger.debug("Added node: " + node.getName() + " to the user shortcuts list.");
               }
               catch (Exception err)
               {
                  Utils.addErrorMessage( MessageFormat.format(Repository.ERROR_GENERIC, err.getMessage()), err );
                  try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
               }
            }
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage( MessageFormat.format(Repository.ERROR_NODEREF, new Object[] {id}) );
         }
      }
   }
   
   /**
    * Get or create the node we need to store our user preferences
    */
   private synchronized NodeRef getCreateShortcutsNodeRef()
   {
      if (this.shortcutFolderRef == null)
      {
         // TODO: move this to Application/Repository?
         NodeRef person = Application.getCurrentUser(FacesContext.getCurrentInstance()).getPerson();
         if (this.nodeService.hasAspect(person, ContentModel.ASPECT_CONFIGURABLE) == false)
         {
            // create the configuration folder for this Person node
            this.ruleService.makeConfigurable(person);
         }
         
         List<AssociationRef> assocs = this.nodeService.getTargetAssocs(person, ContentModel.ASSOC_CONFIGURATIONS);
         if (assocs.size() != 1)
         {
            throw new IllegalStateException("Unable to find associated 'configurations' folder for node: " + person);
         }
         
         // target of the assoc is the configurations folder ref
         NodeRef configRef = assocs.get(0).getTargetRef();
         
         String xpath = NamespaceService.ALFRESCO_PREFIX + ":" + "preferences";
         List<NodeRef> nodes = this.nodeService.selectNodes(
               configRef,
               xpath,
               null,
               this.namespaceService,
               false);
         
         NodeRef prefRef;
         if (nodes.size() == 1)
         {
            prefRef = nodes.get(0);
         }
         else
         {
            // create the preferences Node for this user
            ChildAssociationRef childRef = this.nodeService.createNode(
                  configRef,
                  ContentModel.ASSOC_CONTAINS,
                  QName.createQName(NamespaceService.ALFRESCO_URI, "preferences"),
                  ContentModel.TYPE_CMOBJECT);
            
            prefRef = childRef.getChildRef();
         }
         
         this.shortcutFolderRef = prefRef;
      }
      
      return this.shortcutFolderRef;
   }
   
   private NodeRef shortcutFolderRef = null;
   
   /**
    * Action handler bound to the user shortcuts Shelf component called when a node is removed
    */
   public void removeShortcut(ActionEvent event)
   {
      UIShortcutsShelfItem.ShortcutEvent shortcutEvent = (UIShortcutsShelfItem.ShortcutEvent)event;
      
      // remove from persistent store
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         NodeRef prefRef = getCreateShortcutsNodeRef();
         List<String> shortcuts = (List<String>)this.nodeService.getProperty(prefRef, QNAME_SHORTCUTS);
         if (shortcuts != null && shortcuts.size() > shortcutEvent.Index)
         {
            // remove the shortcut from the saved list and persist back
            shortcuts.remove(shortcutEvent.Index);
            this.nodeService.setProperty(prefRef, QNAME_SHORTCUTS, (Serializable)shortcuts);
            
            // commit the transaction
            tx.commit();
            
            // remove shortcut Node from the in-memory list
            Node node = getShortcuts().remove(shortcutEvent.Index);
            
            if (logger.isDebugEnabled())
               logger.debug("Removed node: " + node.getName() + " from the user shortcuts list.");
         }
      }
      catch (Exception err)
      {
         Utils.addErrorMessage( MessageFormat.format(Repository.ERROR_GENERIC, err.getMessage()), err );
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
   }
   
   /**
    * Action handler bound to the user shortcuts Shelf component called when a node is clicked
    */
   public void click(ActionEvent event)
   {
      // work out which node was clicked from the event data
      UIShortcutsShelfItem.ShortcutEvent shortcutEvent = (UIShortcutsShelfItem.ShortcutEvent)event;
      Node selectedNode = getShortcuts().get(shortcutEvent.Index);
      
      if (selectedNode.getType().equals(ContentModel.TYPE_FOLDER))
      {
         // then navigate to the appropriate node in UI
         // use browse bean functionality for this as it will update the breadcrumb for us
         this.browseBean.updateUILocation(selectedNode.getNodeRef());
      }
      else if (selectedNode.getType().equals(ContentModel.TYPE_CONTENT))
      {
         // view details for document
         this.browseBean.setupContentAction(selectedNode.getId());
         FacesContext fc = FacesContext.getCurrentInstance();
         fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "showDocDetails");
      }
   }
}
