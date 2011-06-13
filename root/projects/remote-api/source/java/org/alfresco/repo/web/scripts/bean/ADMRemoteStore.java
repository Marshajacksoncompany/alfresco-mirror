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
package org.alfresco.repo.web.scripts.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * ADM Remote Store service.
 * <p>
 * This implementation of the RemoteStore is tied to the current SiteService implementation.
 * <p>
 * It remaps incoming generic document path requests to the appropriate folder structure
 * in the Sites folder. Dashboard pages and component bindings are remapped to take advantage
 * of inherited permissions in the appropriate root site folder, ensuring that only valid
 * users can write to the appropriate configuration objects.
 * 
 * @see BaseRemoteStore for the available API methods.
 * 
 * @author Kevin Roast
 */
public class ADMRemoteStore extends BaseRemoteStore
{
    private static final Log logger = LogFactory.getLog(ADMRemoteStore.class);
    
    private static final String SURF_CONFIG = "surf-config";
    
    // patterns used to match site and user specific configuration locations
    private static final Pattern USER_PATTERN_1 = Pattern.compile(".*/components/.*\\.user~(.*)~.*");
    private static final Pattern USER_PATTERN_2 = Pattern.compile(".*/pages/user/(.*?)(/.*)?$");
    private static final Pattern SITE_PATTERN_1 = Pattern.compile(".*/components/.*\\.site~(.*)~.*");
    private static final Pattern SITE_PATTERN_2 = Pattern.compile(".*/pages/site/(.*?)(/.*)?$");
    
    private NodeService nodeService;
    private NodeService unprotNodeService;
    private FileFolderService fileFolderService;
    private NamespaceService namespaceService;
    private SiteService siteService;
    private ContentService contentService;
    
    
    /**
     * @param nodeService       the NodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param nodeService       the NodeService to set
     */
    public void setUnprotectedNodeService(NodeService nodeService)
    {
        this.unprotNodeService = nodeService;
    }

    /**
     * @param fileFolderService the FileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @param namespaceService  the NamespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param siteService       the SiteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * @param contentService    the ContentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService; 
    }

    /**
     * Gets the last modified timestamp for the document.
     * 
     * The output will be the last modified date as a long toString().
     * 
     * @param store the store id
     * @param path  document path to an existing document
     */
    @Override
    protected void lastModified(final WebScriptResponse res, final String store, final String path)
        throws IOException
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                final FileInfo fileInfo = resolveFilePath(path);
                if (fileInfo == null)
                {
                    throw new WebScriptException("Unable to locate file: " + path);
                }
                
                Writer out = res.getWriter();
                out.write(Long.toString(fileInfo.getModifiedDate().getTime()));
                out.close();
                if (logger.isDebugEnabled())
                    logger.debug("lastModified: " + Long.toString(fileInfo.getModifiedDate().getTime()));
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Gets a document.
     * 
     * The output will be the document content stream.
     * 
     * @param store the store id
     * @param path  document path
     */
    @Override
    protected void getDocument(final WebScriptResponse res, final String store, final String path)
    {
        // TODO: could only allow appropriate users to read config docs i.e. site specific...
        //       but currently GET requests need to run unauthenticated before user login occurs.
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                final FileInfo fileInfo = resolveFilePath(path);
                if (fileInfo == null || fileInfo.isFolder())
                {
                    res.setStatus(Status.STATUS_NOT_FOUND);
                    return null;
                }
                
                final ContentReader reader;
                try
                {
                    reader = contentService.getReader(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT);
                    if (reader == null || !reader.exists())
                    {
                        throw new WebScriptException("No content found for file: " + path);
                    }
                    
                    // establish mimetype
                    String mimetype = reader.getMimetype();
                    if (mimetype == null || mimetype.length() == 0)
                    {
                        mimetype = MimetypeMap.MIMETYPE_BINARY;
                        int extIndex = path.lastIndexOf('.');
                        if (extIndex != -1)
                        {
                            String ext = path.substring(extIndex + 1);
                            String mt = mimetypeService.getMimetypesByExtension().get(ext);
                            if (mt != null)
                            {
                                mimetype = mt;
                            }
                        }
                    }
                    
                    // set mimetype for the content and the character encoding + length for the stream
                    res.setContentType(mimetype);
                    res.setContentEncoding(reader.getEncoding());
                    res.setHeader("Last-Modified", Long.toString(fileInfo.getModifiedDate().getTime()));
                    res.setHeader("Content-Length", Long.toString(reader.getSize()));
                    
                    if (logger.isDebugEnabled())
                        logger.debug("getDocument: " + fileInfo.toString());
                    
                    // get the content and stream directly to the response output stream
                    // assuming the repository is capable of streaming in chunks, this should allow large files
                    // to be streamed directly to the browser response stream.
                    try
                    {
                        reader.getContent(res.getOutputStream());
                    }
                    catch (SocketException e1)
                    {
                        // the client cut the connection - our mission was accomplished apart from a little error message
                        if (logger.isDebugEnabled())
                            logger.debug("Client aborted stream read:\n\tnode: " + path + "\n\tcontent: " + reader);
                    }
                    catch (ContentIOException e2)
                    {
                        if (logger.isInfoEnabled())
                            logger.info("Client aborted stream read:\n\tnode: " + path + "\n\tcontent: " + reader);
                    }
                    catch (Throwable err)
                    {
                       if (err.getCause() instanceof SocketException)
                       {
                          if (logger.isDebugEnabled())
                              logger.debug("Client aborted stream read:\n\tnode: " + path + "\n\tcontent: " + reader);
                       }
                       else
                       {
                           if (logger.isInfoEnabled())
                               logger.info(err.getMessage());
                           res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
                       }
                    }
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Determines if the document exists.
     * 
     * The output will be either the string "true" or the string "false".
     * 
     * @param store the store id
     * @param path  document path
     */
    @Override
    protected void hasDocument(final WebScriptResponse res, final String store, final String path) throws IOException
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                final FileInfo fileInfo = resolveFilePath(path);
                
                Writer out = res.getWriter();
                out.write(Boolean.toString(fileInfo != null && !fileInfo.isFolder()));
                out.close();
                if (logger.isDebugEnabled())
                    logger.debug("hasDocument: " + Boolean.toString(fileInfo != null && !fileInfo.isFolder()));
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Creates a document.
     * <p>
     * Create methods are user authenticated, so the creation of site config must be
     * allowed for the current user.
     * 
     * @param store         the store id
     * @param path          document path
     * @param content       content of the document to write
     */
    @Override
    protected void createDocument(final WebScriptResponse res, final String store, final String path, final InputStream content)
    {
        try
        {
            // TODO: don't need to support filenames at the root?
            final int off = path.lastIndexOf('/');
            if (off != -1)
            {
                FileInfo parentFolder = resolveNodePath(path, true, false);
                FileInfo fileInfo = this.fileFolderService.create(
                        parentFolder.getNodeRef(), encodePath(path.substring(off + 1)), ContentModel.TYPE_CONTENT);
                this.contentService.getWriter(
                        fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true).putContent(content);
                if (logger.isDebugEnabled())
                    logger.debug("createDocument: " + fileInfo.toString());
            }
        }
        catch (FileExistsException feeErr)
        {
            res.setStatus(Status.STATUS_CONFLICT);
        }
    }
    
    /**
     * Creates multiple XML documents encapsulated in a single one. 
     * 
     * @param store         the store id
     * @param path          document path
     * @param content       content of the document to write
     */
    @Override
    protected void createDocuments(WebScriptResponse res, String store, InputStream content)
    {
        // no implementation currently
    }

    /**
     * Updates an existing document.
     * <p>
     * Update methods are user authenticated, so the modification of site config must be
     * allowed for the current user.
     * 
     * @param store the store id
     * @param path  document path
     * @param content       content to update the document with
     */
    @Override
    protected void updateDocument(final WebScriptResponse res, String store, final String path, final InputStream content)
    {
        final FileInfo fileInfo = resolveFilePath(path);
        if (fileInfo == null || fileInfo.isFolder())
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }
        
        ContentWriter writer = contentService.getWriter(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.putContent(content);
        if (logger.isDebugEnabled())
            logger.debug("updateDocument: " + fileInfo.toString());
    }
    
    /**
     * Deletes an existing document.
     * <p>
     * Delete methods are user authenticated, so the deletion of site config must be
     * allowed for the current user.
     * 
     * @param store the store id
     * @param path  document path
     */
    @Override
    protected void deleteDocument(final WebScriptResponse res, final String store, final String path)
    {
        final FileInfo fileInfo = resolveFilePath(path);
        if (fileInfo == null || fileInfo.isFolder())
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }
        
        this.nodeService.deleteNode(fileInfo.getNodeRef());
        if (logger.isDebugEnabled())
            logger.debug("deleteDocument: " + fileInfo.toString());
    }

    /**
     * Lists the document paths under a given path.
     * <p>
     * The output will be the list of relative document paths found under the path.
     * Separated by newline characters.
     * 
     * @param store     the store id
     * @param path      document path
     * @param recurse   true to peform a recursive list, false for direct children only.
     * 
     * @throws IOException if an error occurs listing the documents
     */
    @Override
    protected void listDocuments(final WebScriptResponse res, final String store, final String path, final boolean recurse)
        throws IOException
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                res.setContentType("text/plain;charset=UTF-8");
                
                final FileInfo fileInfo = resolveNodePath(path, false, true);
                if (fileInfo == null || !fileInfo.isFolder())
                {
                    res.setStatus(Status.STATUS_NOT_FOUND);
                    return null;
                }
                
                try
                {
                    outputFileNodes(res.getWriter(), fileInfo, aquireSurfConfigRef(path, false), "*", recurse);
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                finally
                {
                    res.getWriter().close();
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Lists the document paths matching a file pattern under a given path.
     * 
     * The output will be the list of relative document paths found under the path that
     * match the given file pattern. Separated by newline characters.
     * 
     * @param store     the store id
     * @param path      document path
     * @param pattern   file pattern to match - allows wildcards e.g. *.xml or site*.xml
     * 
     * @throws IOException if an error occurs listing the documents
     */
    @Override
    protected void listDocuments(final WebScriptResponse res, final String store, final String path, final String pattern)
        throws IOException
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                res.setContentType("text/plain;charset=UTF-8");
                
                final FileInfo fileInfo = resolveNodePath(path, false, true);
                if (fileInfo == null || !fileInfo.isFolder())
                {
                    res.setStatus(Status.STATUS_NOT_FOUND);
                    return null;
                }
                
                String filePattern = pattern;
                if (filePattern == null || filePattern.length() == 0)
                {
                    filePattern = "*";
                }
                
                if (logger.isDebugEnabled())
                    logger.debug("listDocuments() pattern: " + pattern);
                
                try
                {
                    outputFileNodes(res.getWriter(), fileInfo, aquireSurfConfigRef(path, false), pattern, false);
                }
                catch (AccessDeniedException ae)
                {
                    res.setStatus(Status.STATUS_UNAUTHORIZED);
                }
                finally
                {
                    res.getWriter().close();
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @param path      cm:name based root relative path
     *                  example: /alfresco/site-data/pages/customise-user-dashboard.xml
     * 
     * @return FileInfo representing the file/folder at the specified path location
     *         or null if the supplied path does not exist in the store
     */
    private FileInfo resolveFilePath(final String path)
    {
        return resolveNodePath(path, false, false);
    }
    
    /**
     * @param path      cm:name based root relative path
     *                  example: /alfresco/site-data/pages/customise-user-dashboard.xml
     *                           /alfresco/site-data/components
     * @param create    if true create the config and folder dirs for the given path returning
     *                  the FileInfo for the last parent in the path, if false only attempt to
     *                  resolve the folder path if it exists returning the last element.
     * @param isFolder  True if the path is for a folder, false if it ends in a filename
     * 
     * @return FileInfo representing the file/folder at the specified path location (see create
     *         parameter above) or null if the supplied path does not exist in the store.
     */
    private FileInfo resolveNodePath(final String path, final boolean create, final boolean isFolder)
    {
        if (logger.isDebugEnabled())
            logger.debug("Resolving path: " + path);
        FileInfo result = null;
        if (path != null)
        {
            // break down the path into its component elements
            List<String> pathElements = new ArrayList<String>(4);
            final StringTokenizer t = new StringTokenizer(encodePath(path), "/");
            // the store requires paths of the form /alfresco/site-data/<objecttype>[/<folder>]/<file>.xml
            if (t.countTokens() >= 3)
            {
                t.nextToken();  // skip /alfresco
                t.nextToken();  // skip /site-data
                // collect remaining folder path (and file)
                while (t.hasMoreTokens())
                {
                    pathElements.add(t.nextToken());
                }
                
                NodeRef surfConfigRef = aquireSurfConfigRef(path, create);
                try
                {
                    if (create)
                    {
                        // ensure folders exist down to the specified parent
                        result = FileFolderUtil.makeFolders(
                                this.fileFolderService,
                                surfConfigRef,
                                isFolder ? pathElements : pathElements.subList(0, pathElements.size() - 1),
                                ContentModel.TYPE_FOLDER);
                    }
                    else
                    {
                        // perform the cm:name path lookup against our config root node
                        if (surfConfigRef != null)
                        {
                            result = this.fileFolderService.resolveNamePath(surfConfigRef, pathElements);
                        }
                    }
                }
                catch (FileNotFoundException fnfErr)
                {
                    // this is a valid condition - we return null to indicate failed lookup
                }
            }
        }
        return result;
    }

    /**
     * Aquire (optionally create) the NodeRef to the "surf-config" folder as appropriate
     * for the given path.
     * <p>
     * Disassmbles the path to correct match either user, site or generic folder path.
     * 
     * @param path
     * @param create
     * 
     * @return NodeRef to the "surf-config" folder, or null if it does not exist yet.
     */
    private NodeRef aquireSurfConfigRef(final String path, final boolean create)
    {
        // remap the path into the appropriate Sites or site relative folder location
        // by first matching the path to appropriate user or site regex
        String userId = null;
        String siteName = null;
        Matcher matcher;
        if ((matcher = USER_PATTERN_1.matcher(path)).matches())
        {
            userId = matcher.group(1);
        }
        else if ((matcher = USER_PATTERN_2.matcher(path)).matches())
        {
            userId = matcher.group(1);
        }
        else if ((matcher = SITE_PATTERN_1.matcher(path)).matches())
        {
            siteName = matcher.group(1);
        }
        else if ((matcher = SITE_PATTERN_2.matcher(path)).matches())
        {
            siteName = matcher.group(1);
        }
        
        NodeRef surfConfigRef = null;
        if (userId != null)
        {
            if (logger.isDebugEnabled())
                logger.debug("...resolved user path id: " + userId);
            surfConfigRef = getSurfConfigNodeRef(getRootNodeRef(), create);
        }
        else if (siteName != null)
        {
            if (logger.isDebugEnabled())
                logger.debug("...resolved site path id: " + siteName);
            NodeRef siteRef = getSiteNodeRef(siteName);
            if (siteRef != null)
            {
                surfConfigRef = getSurfConfigNodeRef(siteRef, create);
            }
        }
        else
        {
            if (logger.isDebugEnabled())
                logger.debug("...resolved to generic path.");
            surfConfigRef = getSurfConfigNodeRef(getRootNodeRef(), create);
        }
        return surfConfigRef;
    }
    
    /**
     * Return the "surf-config" noderef under the given root. No attempt will be made
     * to create the node if it does not exist yet.
     * 
     * @param rootRef   Root node reference where the "surf-config" folder should live
     * 
     * @return surf-config folder ref if found, null otherwise
     */
    private NodeRef getSurfConfigNodeRef(final NodeRef rootRef)
    {
        return getSurfConfigNodeRef(rootRef, false);
    }
    
    /**
     * Return the "surf-config" noderef under the given root. Optionally create the
     * folder if it does not exist yet. NOTE: must only be set to create if within a
     * WRITE transaction context.
     * 
     * @param rootRef   Root node reference where the "surf-config" folder should live
     * @param create    True to create the folder if missing, false otherwise
     * 
     * @return surf-config folder ref if found, null otherwise if not creating
     */
    private NodeRef getSurfConfigNodeRef(final NodeRef rootRef, final boolean create)
    {
        NodeRef surfConfigRef = this.unprotNodeService.getChildByName(
                rootRef, ContentModel.ASSOC_CONTAINS, SURF_CONFIG);
        //
        // TODO: Does this need protecting with RRW - keyed from rootRef?
        //       As given the use cases, only the surf-config folder in Sites has the potential
        //       for multi-user WRITE access e.g. 2 users creating their dashboards at same time
        //       - which could be solved by creating the surf-config folder as part of the patch code
        //       and on bootstrap for new repo also.
        //       As the site level surf-config folders will be created by which ever user creates the
        //       site - no READ access will be possible until then anyway...
        //
        if (create && surfConfigRef == null)
        {
            if (logger.isDebugEnabled())
                logger.debug("'surf-config' folder not found under path, creating...");
            QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, SURF_CONFIG);
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1, 1.0f);
            properties.put(ContentModel.PROP_NAME, (Serializable) SURF_CONFIG);
            ChildAssociationRef ref = this.unprotNodeService.createNode(
                    rootRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_FOLDER, properties);
            surfConfigRef = ref.getChildRef();
        }
        return surfConfigRef;
    }

    /**
     * @return the Sites folder root node reference
     */
    private NodeRef getRootNodeRef()
    {
        return this.siteService.getSiteRoot();
    }
    
    /**
     * @param shortName     Site shortname
     * 
     * @return the given Site folder node reference
     */
    private NodeRef getSiteNodeRef(String shortName)
    {
        SiteInfo siteInfo = this.siteService.getSite(shortName); 
        return siteInfo != null ? siteInfo.getNodeRef() : null;
    }
    
    /**
     * Output the matching file paths a node contains based on a pattern search.
     * 
     * @param out       Writer for output - relative paths separated by newline characters
     * @param surfConfigRef Surf-Config folder
     * @param fileInfo  The FileInfo node to use as the parent
     * @param pattern   Optional pattern to match filenames against
     * @param recurse   True to recurse sub-directories
     * 
     * @throws IOException
     */
    private void outputFileNodes(Writer out, FileInfo fileInfo, NodeRef surfConfigRef, String pattern, boolean recurse)
        throws IOException
    {
        final boolean debug = logger.isDebugEnabled();
        final Map<NodeRef, String> nameCache = new HashMap<NodeRef, String>();
        final List<FileInfo> files = fileFolderService.search(fileInfo.getNodeRef(), pattern, true, false, recurse);
        for (final FileInfo file : files)
        {
            // walking up the parent tree manually until the "surf-config" parent is hit
            // and manually appending the rest of the cm:name path down to the node.
            StringBuilder displayPath = new StringBuilder(64);
            NodeRef ref = unprotNodeService.getPrimaryParent(file.getNodeRef()).getParentRef();
            while (!ref.equals(surfConfigRef))
            {
                String name = nameCache.get(ref);
                if (name == null)
                {
                    name = (String)unprotNodeService.getProperty(ref, ContentModel.PROP_NAME);
                    nameCache.put(ref, name);
                }
                displayPath.insert(0, '/');
                displayPath.insert(0, name);
                ref = unprotNodeService.getPrimaryParent(ref).getParentRef();
            }
            
            out.write("/alfresco/site-data/");
            out.write(displayPath.toString());
            out.write(file.getName());
            out.write('\n');
            if (debug) logger.debug("   /alfresco/site-data/" + displayPath.toString() + file.getName());
        }
    }
}
