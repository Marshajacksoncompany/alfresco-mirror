/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of the Alfresco Web Quick Start module.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.module.org_alfresco_module_wcmquickstart.webscript;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_wcmquickstart.model.SectionHierarchyProcessor;
import org.alfresco.module.org_alfresco_module_wcmquickstart.model.WebSiteModel;
import org.alfresco.module.org_alfresco_module_wcmquickstart.util.AssetSerializer;
import org.alfresco.module.org_alfresco_module_wcmquickstart.util.AssetSerializerFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Asset GET implementation
 */
public class AssetSearch extends AbstractWebScript
{
    private static Log log = LogFactory.getLog(AssetSearch.class);
    
    private static Set<Character> CHARS_TO_ESCAPE = new HashSet<Character>(89);
    static {
        CHARS_TO_ESCAPE.add('!');
        CHARS_TO_ESCAPE.add('*');
        CHARS_TO_ESCAPE.add('?');
        CHARS_TO_ESCAPE.add('(');
        CHARS_TO_ESCAPE.add(')');
        CHARS_TO_ESCAPE.add('[');
        CHARS_TO_ESCAPE.add(']');
        CHARS_TO_ESCAPE.add('^');
        CHARS_TO_ESCAPE.add('"');
        CHARS_TO_ESCAPE.add('~');
        CHARS_TO_ESCAPE.add(':');
        CHARS_TO_ESCAPE.add('+');
        CHARS_TO_ESCAPE.add('-');
        CHARS_TO_ESCAPE.add('\\');
        CHARS_TO_ESCAPE.add('{');
        CHARS_TO_ESCAPE.add('}');
    }

    private static final String PARAM_SECTION_ID = "sectionid";
    private static final String PARAM_PHRASE = "phrase";
    private static final String PARAM_TAG = "tag";
    private static final String PARAM_SKIP = "skip";
    private static final String PARAM_MAX = "max";

    private NodeService nodeService;
    private SearchService searchService;
    private AssetSerializerFactory assetSerializerFactory;
    private SectionHierarchyProcessor sectionHierarchyProcessor;

    public void setAssetSerializerFactory(AssetSerializerFactory assetSerializerFactory)
    {
        this.assetSerializerFactory = assetSerializerFactory;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setSectionHierarchyProcessor(SectionHierarchyProcessor sectionHierarchyProcessor)
    {
        this.sectionHierarchyProcessor = sectionHierarchyProcessor;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        try
        {
            sectionHierarchyProcessor.init();
            
            StringBuilder queryBuilder = new StringBuilder();
            
            String sectionId = req.getParameter(PARAM_SECTION_ID);
            String phrase = req.getParameter(PARAM_PHRASE);
            String tag = req.getParameter(PARAM_TAG);
            String resultsToSkip = req.getParameter(PARAM_SKIP);
            String maxResults = req.getParameter(PARAM_MAX);
            
            int skip = resultsToSkip == null ? 0 : Integer.parseInt(resultsToSkip);
            int max = maxResults == null ? 100 : Integer.parseInt(maxResults);
            
            if (sectionId == null || sectionId.length() == 0)
            {
                throw new WebScriptException("\"sectionid\" is a required parameter");
            }
            
            queryBuilder.append("+@ws\\:ancestorSections:\"");
            queryBuilder.append(sectionId);
            queryBuilder.append("\" ");
            
            queryBuilder.append("+ASPECT:\"");
            queryBuilder.append(WebSiteModel.ASPECT_WEBASSET.toString());
            queryBuilder.append("\" ");
            
            if (phrase != null && phrase.length() != 0)
            {
                String[] tokens = phrase.split(" ");
                queryBuilder.append("+(");
                for (String token : tokens)
                {
                    queryBuilder.append("+(");
                    queryBuilder.append("@cm\\:title:\"");
                    queryBuilder.append(token);
                    queryBuilder.append("\"^10 ");
                    queryBuilder.append("@cm\\:description:\"");
                    queryBuilder.append(token);
                    queryBuilder.append("\"^5 ");
                    queryBuilder.append("TEXT:\"");
                    queryBuilder.append(token);
                    queryBuilder.append("\") ");
                }
                queryBuilder.append(") ");
            }
            
            if (tag != null && tag.length() != 0)
            {
                queryBuilder.append("+@ws\\:tags:\"");
                queryBuilder.append(tag);
                queryBuilder.append("\" ");
            }
            
            long start = 0L;
            String query = queryBuilder.toString();
            if (log.isDebugEnabled())
            {
                log.debug("About to run query: " + query);
                start = System.currentTimeMillis();
            }
            ResultSet rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, 
                    SearchService.LANGUAGE_LUCENE, query);
            if (log.isDebugEnabled())
            {
                long timeTaken = System.currentTimeMillis() - start;
                log.debug("Found " + rs.length() + " results in " + timeTaken + "ms");
            }
            
            long totalResults = rs.length();
            int index = skip;
            int count = 0;
            
            res.setContentEncoding("UTF-8");
            res.setContentType("text/xml");
            Writer writer = res.getWriter();
            AssetSerializer assetSerializer = assetSerializerFactory.getAssetSerializer();
            assetSerializer.start(writer);
            Map<QName, Serializable> header = new HashMap<QName, Serializable>(3);
            header.put(QName.createQName("totalResults"), new Long(totalResults));
            assetSerializer.writeHeader(header);
            while (index < totalResults && count < max)
            {
                ResultSetRow row = rs.getRow(index);
                NodeRef nodeRef = row.getNodeRef();
                QName typeName = nodeService.getType(nodeRef);
                Map<QName,Serializable> props = nodeService.getProperties(nodeRef);
                props.put(QName.createQName("searchScore"), new Integer((int)(row.getScore() * 100)));
                assetSerializer.writeNode(nodeRef, typeName, props);
                ++index;
                ++count;
            }
            assetSerializer.end();
        }
        catch (Throwable e)
        {
            throw createStatusException(e, req, res);
        }
    }
}
