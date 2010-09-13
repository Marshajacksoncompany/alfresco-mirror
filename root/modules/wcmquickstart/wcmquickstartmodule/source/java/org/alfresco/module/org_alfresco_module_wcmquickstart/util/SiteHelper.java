package org.alfresco.module.org_alfresco_module_wcmquickstart.util;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_wcmquickstart.model.WebSiteModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Site helper class. 
 * 
 * @author Brian
 */
public class SiteHelper implements WebSiteModel
{
    private final static Log log = LogFactory.getLog(SiteHelper.class);
    
    /** Template for web asset URL */
    private static final String URL_WEBASSET = "http://{0}:{1}/{2}/asset/{3}/{4}";
    
    /** Site service */
    private SiteService siteService;
    
    /** Node service */
    private NodeService nodeService;
    
    private DictionaryService dictionaryService;
    
    /**
     * Given a webasset, return the full URL calculated from the containing web site.
     * @param nodeRef	node reference
     * @return String	URL
     */
    public String getWebAssetURL(NodeRef nodeRef)
    {
    	String result = null;
    	if (nodeService.hasAspect(nodeRef, ASPECT_WEBASSET) == true)
    	{
    		NodeRef webSite = getRelevantWebSite(nodeRef);
    		if (webSite != null)
    		{
    			// Get the parts of the URL
    			Map<QName, Serializable> webSiteProps = nodeService.getProperties(webSite);
    			String hostName = (String)webSiteProps.get(PROP_HOST_NAME);
    			String hostPort = ((Integer)webSiteProps.get(PROP_HOST_PORT)).toString();
    			String webappName = (String)webSiteProps.get(PROP_WEB_APP_CONTEXT);
    			String id = nodeRef.getId();
    			String fileName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    			
    			// Construct the URL
    			result = MessageFormat.format(URL_WEBASSET, hostName, hostPort, webappName, id, fileName);
    		}
    	}
    	
    	return result;
    }
    
    /**
     * For the specified node, find the Share site that it is located in (if any)
     * @param noderef
     * @return The {@link org.alfresco.service.cmr.site.SiteInfo} object representing the relevant Share site or null if the specified
     * node is not within a Share site
     */
    public SiteInfo getRelevantShareSite(NodeRef noderef)
    {
        SiteInfo siteInfo = null;
        
        NodeRef parentNode = findNearestParent(noderef, SiteModel.TYPE_SITE);
        if (parentNode != null && nodeService.exists(parentNode) == true)
        {
            // If we get here then parentNode identifies a Share site.
            siteInfo = siteService.getSite(parentNode);
            if (log.isDebugEnabled())
            {
                log.debug("Found the corresponding Share site for the specified node: " + siteInfo.getShortName());
            }
        }
        else
        {
            if (log.isDebugEnabled())
            {
                log.debug("Specified node is not within a Share site: " + noderef);
            }
        }
        return siteInfo;
    }

    /**
     * Gets the web site that a node resides within
     * @param noderef	node reference
     * @return NodeRef	web site node reference, null otherwise
     */
    public NodeRef getRelevantWebSite(NodeRef noderef)
    {
        return findNearestParent(noderef, WebSiteModel.TYPE_WEB_SITE);
    }
    
    /**
     * Gets the web root that the node resides within
     * @param nodeRef	node reference
     * @return NodeRef	web root node reference, null otherwise
     */
    public NodeRef getRelevantWebRoot(NodeRef nodeRef)
    {
    	return findNearestParent(nodeRef, WebSiteModel.TYPE_WEB_ROOT);
    }
    
    /**
     * Gets the section that the node resides within
     * @param nodeRef   node reference
     * @return NodeRef  section node reference, null otherwise
     */
    public NodeRef getRelevantSection(NodeRef nodeRef)
    {
        return getRelevantSection(nodeRef, true);
    }
    
    /**
     * Gets the section that the node resides within
     * @param nodeRef   node reference
     * @param allowSelf true if the supplied noderef is included in the "relevant section" test. That is to say that
     * if this flag is true and the supplied node ref identifies a Section then this method will return the 
     * supplied node ref.
     * @return NodeRef  section node reference, null otherwise
     */
    public NodeRef getRelevantSection(NodeRef nodeRef, boolean allowSelf)
    {
        return findNearestParent(nodeRef, WebSiteModel.TYPE_SECTION, allowSelf);
    }
    
    /**
     * Gets the named site container that a given node reference resides within
     * @param noderef			node reference
     * @param containerName		container name
     * @return NodeRef			container node reference, null otherwise
     */
    public NodeRef getWebSiteContainer(NodeRef noderef, String containerName)
    {
        NodeRef container = null;
        SiteInfo siteInfo = getRelevantShareSite(noderef);
        NodeRef websiteId = getRelevantWebSite(noderef);
        if (siteInfo != null && 
            nodeService.exists(siteInfo.getNodeRef()) == true)
        {
            if (websiteId == null)
            {
                websiteId = siteInfo.getNodeRef();
            }
 
            if (siteService.getSite(siteInfo.getShortName()) != null)
            {            
                NodeRef containerParent = siteService.getContainer(siteInfo.getShortName(), websiteId.getId());
                if (containerParent == null)
                {
                    containerParent = siteService.createContainer(siteInfo.getShortName(), websiteId.getId(), null, null);
                }
                container = nodeService.getChildByName(containerParent, ContentModel.ASSOC_CONTAINS, 
                        containerName);
                if (container == null)
                {
                    HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(ContentModel.PROP_NAME, containerName);
                    container = nodeService.createNode(containerParent, ContentModel.ASSOC_CONTAINS, 
                            QName.createQName(WebSiteModel.NAMESPACE, containerName), 
                            ContentModel.TYPE_FOLDER, props).getChildRef();
                }
            }
        }
        return container;
    }
    
    /**
     * Set the site service
     * @param siteService	site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /** 
     * Set the node service
     * @param nodeService	node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Find the nearest parent in the primary child association hierarchy that
     * is of the specified content type (or a sub-type of that type).
     * @param noderef       node reference
     * @param parentType    parent node type
     * @return NodeRef      nearest parent node reference, null otherwise
     */
    private NodeRef findNearestParent(NodeRef noderef, QName parentType)
    {
        return findNearestParent(noderef, parentType, true);
    }

    /**
     * Find the nearest parent in the primary child association hierarchy that
     * is of the specified content type (or a sub-type of that type).
     * @param noderef		node reference
     * @param parentType	parent node type
     * @return NodeRef		nearest parent node reference, null otherwise
     */
    private NodeRef findNearestParent(NodeRef noderef, QName parentType, boolean allowSelf)
    {
        NodeRef parentNode;
        parentNode = allowSelf ? noderef : nodeService.getPrimaryParent(noderef).getParentRef();
        while (parentNode != null && 
               nodeService.exists(parentNode) == true &&
               dictionaryService.isSubClass(nodeService.getType(parentNode), parentType) == false)
        {
            parentNode = nodeService.getPrimaryParent(parentNode).getParentRef();
        }
        return parentNode;
    }
}
