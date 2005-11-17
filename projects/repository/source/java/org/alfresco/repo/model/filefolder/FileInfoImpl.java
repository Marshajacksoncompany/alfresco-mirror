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
package org.alfresco.repo.model.filefolder;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Common file information implementation.
 * 
 * @author Derek Hulley
 */
public class FileInfoImpl implements FileInfo
{
    private NodeRef nodeRef;
    private boolean isFolder;
    private String name;

    /**
     * Package-level constructor
     */
    /* package */ FileInfoImpl(NodeRef nodeRef, boolean isFolder, String name)
    {
        this.nodeRef = nodeRef;
        this.isFolder = isFolder;
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("FileInfo")
          .append("[name=").append(name)
          .append(", isFolder=").append(isFolder)
          .append(", nodeRef=").append(nodeRef)
          .append("]");
        return sb.toString();
    }
    
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public boolean isFolder()
    {
        return isFolder;
    }

    public String getName()
    {
        return name;
    }
}
