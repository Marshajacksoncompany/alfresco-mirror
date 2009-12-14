/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.contentdata;

import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.util.EqualsHelper;
import org.springframework.extensions.surf.util.Pair;

/**
 * Entity bean for <b>alf_content_url</b> table.
 * <p>
 * These are unique (see {@link #equals(Object) equals} and {@link #hashCode() hashCode}) based
 * on the {@link #getContentUrl() content URL} value.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class ContentUrlEntity
{
    public static final Long CONST_LONG_ZERO = new Long(0L);
    public static final String EMPTY_URL = "empty";
    
    private Long id;
    private Long version;
    private String contentUrl;
    private String contentUrlShort;
    private long contentUrlCrc;
    private long size;
    
    public ContentUrlEntity()
    {
        this.size = 0L;
    }
    
    @Override
    public int hashCode()
    {
        return (contentUrl == null ? 0 : contentUrl.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof ContentUrlEntity)
        {
            ContentUrlEntity that = (ContentUrlEntity) obj;
            return EqualsHelper.nullSafeEquals(this.contentUrl, that.contentUrl);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("ContentUrlEntity")
          .append("[ ID=").append(id)
          .append(", contentUrl=").append(contentUrl)
          .append(", size=").append(size)
          .append("]");
        return sb.toString();
    }
    
    /**
     * @param 
     * @return              Returns a pair of the short (12 chars lowercase) URL and the CRC value
     */
    private static Pair<String, Long> getContentUrlCrcPair(String internalContentUrl)
    {
        return CrcHelper.getStringCrcPair(internalContentUrl, 12, false, true);
    }
    
    /**
     * @return              Returns the originally-set content URL
     */
    private static String getExternalUrl(String contentUrl)
    {
        if (contentUrl == null)
        {
            return null;
        }
        // Decode Oracle's NULL-EMPTY confusion
        if (contentUrl.equals(EMPTY_URL))
        {
            return "";
        }
        else
        {
            return contentUrl;
        }
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public String getContentUrl()
    {
        // Convert the persisted content URL to an external value
        return ContentUrlEntity.getExternalUrl(contentUrl);
    }

    public void setContentUrl(String contentUrl)
    {
        this.contentUrl = contentUrl;
        // Convert the URL to a persistable value
        Pair<String, Long> contentUrlPair = ContentUrlEntity.getContentUrlCrcPair(contentUrl);
        this.contentUrlShort = contentUrlPair.getFirst();
        this.contentUrlCrc = contentUrlPair.getSecond();
    }
    
    /**
     * For persistence use
     */
    public String getContentUrlShort()
    {
        return contentUrlShort;
    }

    /**
     * For persistence use
     */
    public void setContentUrlShort(String contentUrlShort)
    {
        this.contentUrlShort = contentUrlShort;
    }

    /**
     * For persistence use
     */
    public long getContentUrlCrc()
    {
        return contentUrlCrc;
    }

    /**
     * For persistence use
     */
    public void setContentUrlCrc(long contentUrlCrc)
    {
        this.contentUrlCrc = contentUrlCrc;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }
}
