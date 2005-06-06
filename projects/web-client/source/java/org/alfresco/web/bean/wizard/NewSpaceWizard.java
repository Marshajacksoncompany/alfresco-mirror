package org.alfresco.web.bean.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.dictionary.NamespaceService;
import org.alfresco.repo.dictionary.impl.DictionaryBootstrap;
import org.alfresco.repo.ref.ChildAssocRef;
import org.alfresco.repo.ref.NodeRef;
import org.alfresco.repo.ref.QName;
import org.alfresco.repo.search.ResultSet;
import org.alfresco.repo.search.ResultSetRow;
import org.alfresco.web.bean.RepoUtils;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.log4j.Logger;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Handler class used by the New Space Wizard 
 * 
 * @author gavinc
 */
public class NewSpaceWizard extends AbstractWizardBean
{
   private static Logger logger = Logger.getLogger(NewSpaceWizard.class);
   
   // TODO: retrieve these from the config service
   private static final String WIZARD_TITLE = "New Space Wizard";
   private static final String WIZARD_DESC = "Use this wizard to create a new space.";
   private static final String STEP1_TITLE = "Step One - Starting Space";
   private static final String STEP1_DESCRIPTION = "Choose how you want to create your space.";
   private static final String STEP2_TITLE = "Step Two - Space Options";
   private static final String STEP2_DESCRIPTION = "Select space options.";
   private static final String STEP3_TITLE = "Step Three - Space Details";
   private static final String STEP3_DESCRIPTION = "Enter information about the space.";
   private static final String FINISH_INSTRUCTION = "To close this wizard and create your space click Finish.";
   
   // new space wizard specific properties
   private String createFrom;
   private String spaceType;
   private String existingSpaceId;
   private String templateSpaceId;
   private String copyPolicy;
   private String name;
   private String description;
   private String icon;
   private String templateName;
   private boolean saveAsTemplate;
   private List<SelectItem> templates;
   
   /**
    * Deals with the finish button being pressed
    * 
    * @return outcome
    */
   public String finish()
   {
      String outcome = FINISH_OUTCOME;
      
      // *******************************************************************************
      // TODO: The user may have selected to create the space from an existing space
      //       or a template space, if so we need to copy rather than create, but there
      //       are no repository services available yet to do this!
      //       We also need to be aware of copying structure and/or content.
      //       For now we always create the space from scratch.
      // *******************************************************************************
      
      if (this.name == null || this.name.length() == 0)
      {
         // create error and send wizard back to details page
         Utils.addErrorMessage("You must supply a name for the space.");
         outcome = determineOutcomeForStep(3);
         this.currentStep = 3;
      }
      else
      {
         UserTransaction tx = null;
      
         try
         {
            tx = RepoUtils.getUserTransaction(FacesContext.getCurrentInstance());
            tx.begin();
            
            if (this.editMode)
            {
               // update the existing node in the repository
               Node currentSpace = this.browseBean.getActionSpace();
               NodeRef nodeRef = currentSpace.getNodeRef();
               Date now = new Date( Calendar.getInstance().getTimeInMillis() );
               
               // update the modified timestamp
               this.nodeService.setProperty(nodeRef, DictionaryBootstrap.PROP_QNAME_MODIFIED, now);
               this.nodeService.setProperty(nodeRef, DictionaryBootstrap.PROP_QNAME_NAME, this.name);
               this.nodeService.setProperty(nodeRef, DictionaryBootstrap.PROP_QNAME_ICON, this.icon);
               this.nodeService.setProperty(nodeRef, DictionaryBootstrap.PROP_QNAME_DESCRIPTION, this.description);
            }
            else
            {
               // create the space (just create a folder for now)
               NodeRef parentNodeRef;
               String nodeId = getNavigator().getCurrentNodeId();
               if (nodeId == null)
               {
                  parentNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
               }
               else
               {
                  parentNodeRef = new NodeRef(Repository.getStoreRef(), nodeId);
               }
               
               String qname = RepoUtils.createValidQName(this.name);
               ChildAssocRef assocRef = this.nodeService.createNode(parentNodeRef,
                         null, QName.createQName(NamespaceService.ALFRESCO_URI, qname),
                         DictionaryBootstrap.TYPE_QNAME_FOLDER);
               
               NodeRef nodeRef = assocRef.getChildRef();
               
               // set the name property on the node
               this.nodeService.setProperty(nodeRef, DictionaryBootstrap.PROP_QNAME_NAME, this.name);
               
               if (logger.isDebugEnabled())
                  logger.debug("Created folder node with name: " + this.name);

               // apply the uifacets aspect - icon, title and description props
               Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(5);
               uiFacetsProps.put(DictionaryBootstrap.PROP_QNAME_ICON, this.icon);
               uiFacetsProps.put(DictionaryBootstrap.PROP_QNAME_TITLE, this.name);
               uiFacetsProps.put(DictionaryBootstrap.PROP_QNAME_DESCRIPTION, this.description);
               this.nodeService.addAspect(nodeRef, DictionaryBootstrap.ASPECT_QNAME_UIFACETS, uiFacetsProps);
               
               if (logger.isDebugEnabled())
                  logger.debug("Added uifacets aspect with properties: " + uiFacetsProps);
               
               // apply the auditable aspect - created and modified date
               Map<QName, Serializable> auditProps = new HashMap<QName, Serializable>(5);
               Date now = new Date( Calendar.getInstance().getTimeInMillis() );
               auditProps.put(DictionaryBootstrap.PROP_QNAME_CREATED, now);
               auditProps.put(DictionaryBootstrap.PROP_QNAME_MODIFIED, now);
               this.nodeService.addAspect(nodeRef, DictionaryBootstrap.ASPECT_QNAME_AUDITABLE, auditProps);
   
               if (logger.isDebugEnabled())
                  logger.debug("Added auditable aspect with properties: " + auditProps);
            }
            
            // commit the transaction
            tx.commit();
            
            // now we know the new details are in the repository, reset the
            // client side node representation so the new details are retrieved
            if (this.editMode)
            {
               this.browseBean.getActionSpace().reset();
            }
         }
         catch (Exception e)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            throw new AlfrescoRuntimeException("Failed to create new space", e);
         }
      }
      
      return outcome;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getWizardDescription()
    */
   public String getWizardDescription()
   {
      return WIZARD_DESC;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getWizardTitle()
    */
   public String getWizardTitle()
   {
      return WIZARD_TITLE;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepDescription()
    */
   public String getStepDescription()
   {
      String stepDesc = null;
      
      switch (this.currentStep)
      {
         case 1:
         {
            stepDesc = STEP1_DESCRIPTION;
            break;
         }
         case 2:
         {
            stepDesc = STEP2_DESCRIPTION;
            break;
         }
         case 3:
         {
            stepDesc = STEP3_DESCRIPTION;
            break;
         }
         case 4:
         {
            stepDesc = SUMMARY_DESCRIPTION;
            break;
         }
         default:
         {
            stepDesc = "";
         }
      }
      
      return stepDesc;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepTitle()
    */
   public String getStepTitle()
   {
      String stepTitle = null;
      
      switch (this.currentStep)
      {
         case 1:
         {
            stepTitle = STEP1_TITLE;
            break;
         }
         case 2:
         {
            stepTitle = STEP2_TITLE;
            break;
         }
         case 3:
         {
            stepTitle = STEP3_TITLE;
            break;
         }
         case 4:
         {
            stepTitle = SUMMARY_TITLE;
            break;
         }
         default:
         {
            stepTitle = "";
         }
      }
      
      return stepTitle;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepInstructions()
    */
   public String getStepInstructions()
   {
      String stepInstruction = null;
      
      switch (this.currentStep)
      {
         case 4:
         {
            stepInstruction = FINISH_INSTRUCTION;
            break;
         }
         default:
         {
            stepInstruction = DEFAULT_INSTRUCTION;
         }
      }
      
      return stepInstruction;
   }
   
   /**
    * Initialises the wizard
    */
   public void init()
   {
      super.init();
      
      // clear the cached query results
      if (this.templates != null)
      {
         this.templates.clear();
         this.templates = null;
      }
      
      // reset all variables
      this.createFrom = "scratch";
      this.spaceType = "container";
      this.icon = "space-icon-default";
      this.copyPolicy = "structure";
      this.existingSpaceId = null;
      this.templateSpaceId = null;
      this.name = null;
      this.description = "";
      this.templateName = null;
      this.saveAsTemplate = false;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#populate()
    */
   public void populate()
   {
      // get hold of the current node and populate the appropriate values
      Node currentSpace = browseBean.getActionSpace();
      Map<String, Object> props = currentSpace.getProperties();
      
      this.name = (String)props.get("name");
      this.description = (String)props.get("description");
      this.icon = (String)props.get("icon");
   }

   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("Name: ").append(this.name).append("<br/>");
      builder.append("Description: ").append(this.description).append("<br/>");
      builder.append("Create Type: ").append(this.createFrom).append("<br/>");
      builder.append("Space Type: ").append(this.spaceType).append("<br/>");
      builder.append("icon: ").append(this.icon).append("<br/>");
      builder.append("Save As Template: ").append(this.saveAsTemplate).append("<br/>");
      builder.append("Template Name: ").append(this.templateName).append("<br/>");
      
      return builder.toString();
   }
   
   /**
    * @return Returns a list of template spaces currently in the system
    */
   public List getTemplateSpaces()
   {
      if (this.templates == null)
      {
         NodeRef rootNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
         this.templates = new ArrayList<SelectItem>();
         
         String actNs = NamespaceService.ALFRESCO_PREFIX;
         String s = "PATH:\"/" + actNs + ":Glossary/" + actNs + ":Templates/" + actNs + ":*\"";
         ResultSet results = this.searchService.query(rootNodeRef.getStoreRef(), "lucene", s, null, null);
         if (results.length() > 0)
         {
            for (ResultSetRow row : results)
            {
               NodeRef node = row.getNodeRef();
               if (this.nodeService.getType(node).equals(DictionaryBootstrap.TYPE_QNAME_FOLDER))
               {
                  String name = row.getQName().getLocalName();
                  String id = node.getId();
                  this.templates.add(new SelectItem(id, name));
               }
            }
         }
      }
      
      return this.templates;
   }

   /**
    * @return Returns the copyPolicy.
    */
   public String getCopyPolicy()
   {
      return copyPolicy;
   }

   /**
    * @param copyPolicy The copyPolicy to set.
    */
   public void setCopyPolicy(String copyPolicy)
   {
      this.copyPolicy = copyPolicy;
   }
   
   /**
    * @return Returns the createFrom.
    */
   public String getCreateFrom()
   {
      return createFrom;
   }

   /**
    * @param createFrom The createFrom to set.
    */
   public void setCreateFrom(String createFrom)
   {
      this.createFrom = createFrom;
   }

   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   } 

   /**
    * @return Returns the existingSpaceId.
    */
   public String getExistingSpaceId()
   {
      return existingSpaceId;
   }
   
   /**
    * @param existingSpaceId The existingSpaceId to set.
    */
   public void setExistingSpaceId(String existingSpaceId)
   {
      this.existingSpaceId = existingSpaceId;
   }
   
   /**
    * @return Returns the icon.
    */
   public String getIcon()
   {
      return icon;
   }
   
   /**
    * @param icon The icon to set.
    */
   public void setIcon(String icon)
   {
      this.icon = icon;
   }
   
   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * @return Returns the saveAsTemplate.
    */
   public boolean isSaveAsTemplate()
   {
      return saveAsTemplate;
   }
   
   /**
    * @param saveAsTemplate The saveAsTemplate to set.
    */
   public void setSaveAsTemplate(boolean saveAsTemplate)
   {
      this.saveAsTemplate = saveAsTemplate;
   }

   /**
    * @return Returns the spaceType.
    */
   public String getSpaceType()
   {
      return spaceType;
   }
   
   /**
    * @param spaceType The spaceType to set.
    */
   public void setSpaceType(String spaceType)
   {
      this.spaceType = spaceType;
   }
   
   /**
    * @return Returns the templateName.
    */
   public String getTemplateName()
   {
      return templateName;
   }
   
   /**
    * @param templateName The templateName to set.
    */
   public void setTemplateName(String templateName)
   {
      this.templateName = templateName;
   }
   
   /**
    * @return Returns the templateSpaceId.
    */
   public String getTemplateSpaceId()
   {
      return templateSpaceId;
   }
   
   /**
    * @param templateSpaceId The templateSpaceId to set.
    */
   public void setTemplateSpaceId(String templateSpaceId)
   {
      this.templateSpaceId = templateSpaceId;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#determineOutcomeForStep(int)
    */
   protected String determineOutcomeForStep(int step)
   {
      String outcome = null;
      
      switch(step)
      {
         case 1:
         {
            outcome = "create-from";
            break;
         }
         case 2:
         {
            if (createFrom.equalsIgnoreCase("scratch"))
            {
               outcome = "from-scratch";
            }
            else if (createFrom.equalsIgnoreCase("existing"))
            {
               outcome = "from-existing";
            }
            else if (createFrom.equalsIgnoreCase("template"))
            {
               outcome = "from-template";
            }
            
            break;
         }
         case 3:
         {
            outcome = "details";
            break;
         }
         case 4:
         {
            outcome = "summary";
            break;
         }
         default:
         {
            outcome = CANCEL_OUTCOME;
         }
      }
      
      return outcome;
   }
}
