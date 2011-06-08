/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.solr.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

public class NodeMetaData
{
    private long id;
    private NodeRef nodeRef;
    private QName type;
    private long aclId;
    private Map<QName, PropertyValue> properties;
    private Set<QName> aspects;
    private List<Pair<String, QName>> paths;
    public long getId()
    {
        return id;
    }
    public void setId(long id)
    {
        this.id = id;
    }
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    public QName getType()
    {
        return type;
    }
    public void setType(QName type)
    {
        this.type = type;
    }
    public long getAclId()
    {
        return aclId;
    }
    public void setAclId(long aclId)
    {
        this.aclId = aclId;
    }
    
    /**
     * A property value is either null or a subclass of PropertyValue
     */
    public Map<QName, PropertyValue> getProperties()
    {
        return properties;
    }
    public void setProperties(Map<QName, PropertyValue> properties)
    {
        this.properties = properties;
    }
    public Set<QName> getAspects()
    {
        return aspects;
    }
    public void setAspects(Set<QName> aspects)
    {
        this.aspects = aspects;
    }
    public List<Pair<String, QName>> getPaths()
    {
        return paths;
    }
    public void setPaths(List<Pair<String, QName>> paths)
    {
        this.paths = paths;
    }
    @Override
    public String toString()
    {
        return "NodeMetaData [id=" + id + ", nodeRef=" + nodeRef + ", type=" + type + ", aclId=" + aclId
                + ", properties=" + properties + ", aspects=" + aspects + ", paths=" + paths + "]";
    }
    
    
}
