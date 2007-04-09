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

package org.alfresco.repo.attributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.alfresco.repo.avm.AVMDAOs;

/**
 * @author britt
 *
 */
public class MapAttributeImpl extends AttributeImpl implements MapAttribute
{
    public MapAttributeImpl()
    {
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getType()
     */
    public Type getType()
    {
        return Type.MAP;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#clear()
     */
    @Override
    public void clear()
    {
        AVMDAOs.Instance().fMapEntryDAO.delete(this);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#entrySet()
     */
    @Override
    public Set<Entry<String, Attribute>> entrySet()
    {
        List<MapEntry> entries = AVMDAOs.Instance().fMapEntryDAO.get(this);
        Map<String, Attribute> map = new HashMap<String, Attribute>();
        for (MapEntry entry : entries)
        {
            map.put(entry.getKey(), entry.getAttribute());
        }
        return map.entrySet();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#get(java.lang.String)
     */
    @Override
    public Attribute get(String key)
    {
        return AVMDAOs.Instance().fMapEntryDAO.get(this, key).getAttribute();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#keySet()
     */
    @Override
    public Set<String> keySet()
    {
        List<MapEntry> entries = AVMDAOs.Instance().fMapEntryDAO.get(this);
        Set<String> keys = new HashSet<String>();
        for (MapEntry entry : entries)
        {
            keys.add(entry.getKey());
        }
        return keys;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#put(java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    @Override
    public void put(String key, Attribute value)
    {
        MapEntry entry = new MapEntryImpl(this, key, value);
        AVMDAOs.Instance().fMapEntryDAO.save(entry);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#remove(java.lang.String)
     */
    @Override
    public void remove(String key)
    {
        MapEntry entry = AVMDAOs.Instance().fMapEntryDAO.get(this, key);
        if (entry != null)
        {
            AVMDAOs.Instance().fMapEntryDAO.delete(entry);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#values()
     */
    @Override
    public Collection<Attribute> values()
    {
        List<MapEntry> entries = AVMDAOs.Instance().fMapEntryDAO.get(this);
        List<Attribute> attrs = new ArrayList<Attribute>();
        for (MapEntry entry : entries)
        {
            attrs.add(entry.getAttribute());
        }
        return attrs;
    }
}
