/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.permissions;

import java.util.List;

import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;

/**
 * Provides data access support for persistence in <b>alf_access_control_list</b>.
 * 
 * @since 3.4
 * @author Andy Hind, janv
 */
public interface AclDAO
{
    /**
     * Get an ACL (including entries)
     */
    public AccessControlList getAccessControlList(Long id);
    
    /**
     * Get an ACL
     */
    public Acl getAcl(Long id);
    
    /**
     * Get an ACL
     */
    public DbAccessControlList getDbAccessControlList(Long id);
    
    /**
     * Delete an ACL
     * 
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteAccessControlList(Long id);
    
    /**
     * Delete the ACEs in position 0 (those set directly on the ACL and not inherited) Cleans up existing acls
     * 
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteLocalAccessControlEntries(Long id);
    
    /**
     * Delete the ACEs in position > 0 (those not set directly on the ACL but inherited) No affect on any other acl
     * 
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteInheritedAccessControlEntries(Long id);
    
    /**
     * Delete all ACEs that reference this authority as no longer valid. THIS DOES NOT CAUSE ANY ACL TO VERSION
     * Used when deleting a user. No ACL is updated - the user has gone the aces and all related info is deleted.
     * 
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteAccessControlEntries(String authority);
    
    /**
     * Delete some locally set ACLs according to the pattern
     * 
     * @param pattern -
     *            non null elements are used for the match
     * @return - the id of all ACLs affected
     */
    public List<AclChange> deleteAccessControlEntries(Long id, AccessControlEntry pattern);
    
    /**
     * Add an access control entry
     */
    public List<AclChange> setAccessControlEntry(Long id, AccessControlEntry ace);
    
    /**
     * Enable inheritance
     */
    public List<AclChange> enableInheritance(Long id, Long parent);
    
    /**
     * Disable inheritance
     */
    public List<AclChange> disableInheritance(Long id, boolean setInheritedOnAcl);
    
    /**
     * Get the ACL properties
     * 
     * @return - the id of all ACLs affected
     */
    public AccessControlListProperties getAccessControlListProperties(Long id);
    
    /**
     * Create a new ACL with default properties
     * 
     * @see #getDefaultProperties()
     * @see #createAccessControlList(AccessControlListProperties)
     */
    public Long createAccessControlList();
    
    /**
     * Get the default ACL properties
     * 
     * @return the default properties
     */
    public AccessControlListProperties getDefaultProperties();
    
    /**
     * Create a new ACL with the given properties. Unset properties are assigned defaults.
     * 
     * @return ID of AccessControlList
     */
    public Long createAccessControlList(AccessControlListProperties properties);
    
    public DbAccessControlList createDbAccessControlList(AccessControlListProperties properties);
    
    /**
     * @see #createAccessControlList(AccessControlListProperties)
     * @return Acl
     */
    public Acl createAcl(AccessControlListProperties properties);
    
    /**
     * @see #createAccessControlList(AccessControlListProperties)
     * @return Acl
     */
    public Acl createAcl(AccessControlListProperties properties, List<AccessControlEntry> aces, Long inherited);
    
    /**
     * Get the id of the ACL inherited from the one given
     * May return null if there is nothing to inherit -> OLD world where nodes have their own ACL and we walk the parent chain
     */
    public Long getInheritedAccessControlList(Long id);
    
    /**
     * Merge inherited ACEs in to target - the merged ACEs will go in at their current position +1
     */
    public List<AclChange> mergeInheritedAccessControlList(Long inherited, Long target);
    
    public DbAccessControlList getDbAccessControlListCopy(Long toCopy, Long toInheritFrom, ACLCopyMode mode);
    
    public List<Long> getAVMNodesByAcl(long aclEntityId, int maxResults);
    
    public List<Long> getADMNodesByAcl(long aclEntityId, int maxResults);
    
    public DbAccessControlList createLayeredAcl(Long indirectedAcl);
    
    public void renameAuthority(String before, String after);
    
    public void deleteAclForNode(long aclId, boolean isAVMNode);
}
