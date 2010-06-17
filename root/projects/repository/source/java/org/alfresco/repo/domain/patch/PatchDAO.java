/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.patch;

import java.util.List;

import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Additional DAO services for patches
 *
 * @author janv
 * @author Derek Hulley
 * @since 3.2
 */
public interface PatchDAO
{
    /**
     * Does the underlying connection support isolation level 1 (dirty read)
     * 
     * @return true if we can do a dirty db read and so track changes (Oracle can not)
     */
    public boolean supportsProgressTracking();
    
    // AVM-related
    
    public Long getAVMNodesCountWhereNewInStore();
    
    public List<AVMNodeEntity> getEmptyGUIDS(int count);
    
    public List<AVMNodeEntity> getNullVersionLayeredDirectories(int count);
    
    public List<AVMNodeEntity> getNullVersionLayeredFiles(int count);
    
    public Long getMaxAvmNodeID();
    
    public List<Long> getAvmNodesWithOldContentProperties(Long minNodeId, Long maxNodeId);
    
    public int updateAVMNodesNullifyAcl(List<Long> nodeIds);
    
    public int updateAVMNodesSetAcl(long aclId, List<Long> nodeIds);
    
    // DM-related
    
    public Long getMaxAdmNodeID();
    
    /**
     * Migrates DM content properties from the old V3.1 format (String-based {@link ContentData#toString()})
     * to the new V3.2 format (ID based storage using {@link ContentDataDAO}).
     * 
     * @param minNodeId         the inclusive node ID to limit the updates to
     * @param maxNodeId         the exclusive node ID to limit the updates to
     */
    public void updateAdmV31ContentProperties(Long minNodeId, Long maxNodeId);
    
    /**
     * Update all <b>alf_content_data</b> mimetype references.
     * 
     * @param oldMimetypeId     the ID to search for
     * @param newMimetypeId     the ID to change to
     * @return                  the number of rows affected
     */
    public int updateContentMimetypeIds(Long oldMimetypeId, Long newMimetypeId);
    
    /**
     * A callback handler for iterating over the string results
     */
    public interface StringHandler
    {
        void handle(String string);
    }
    
    /**
     * Iterate over all person nodes with missing usage property (for one-off patch)
     * 
     * @param storeRef                          the store to search in
     * @param handler                           the callback to use while iterating over the people
     * @return Returns the values for person node uuid
     */
    public void getUsersWithoutUsageProp(StoreRef storeRef, StringHandler handler);
    
    // ACL-related
    
    /**
     * Get the max acl id
     * 
     * @return - max acl id
     */
    public Long getMaxAclId();
    
    /**
     * How many DM nodes are there?
     * 
     * @return - the count
     */
    public long getDmNodeCount();
    
    /**
     * How many DM nodes are three with new ACls (to track patch progress)
     * 
     * @param above
     * @return - the count
     */
    public long getDmNodeCountWithNewACLs(Long above);
    
    public List<Long> selectAllAclIds();
    
    public List<Long> selectNonDanglingAclIds();
    
    public int deleteDanglingAces();
    
    public int deleteAcls(List<Long> aclIds);
    
    public int deleteAclMembersForAcls(List<Long> aclIds);
    
    /**
     * @return      Returns the names of authorities with incorrect CRC values
     */
    public List<String> getAuthoritiesWithNonUtf8Crcs();
}
