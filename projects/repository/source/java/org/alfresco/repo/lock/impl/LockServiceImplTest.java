/**
 * Created on Apr 19, 2005
 */
package org.alfresco.repo.lock.impl;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.repo.dictionary.impl.DictionaryBootstrap;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToAquireLockException;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;

/**
 * Simple lock service test
 * 
 * @author Roy Wetherall
 */
public class LockServiceImplTest extends BaseSpringTest
{
    private static final String USER_REF1 = "userRef1";
    private static final String USER_REF2 = "userRef2";
    
    /**
     * Services used in tests
     */
    private NodeService nodeService;
    private LockService lockService;
    
    /**
     * Data used in tests
     */
    private NodeRef parentNode;
    private NodeRef childNode1;
    private NodeRef childNode2;    
    private NodeRef noAspectNode;

    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        this.nodeService = (NodeService)applicationContext.getBean("dbNodeService");
        this.lockService = (LockService)applicationContext.getBean("lockService");
        
        // Create the node properties
        HashMap<QName, Serializable> nodeProperties = new HashMap<QName, Serializable>();
        nodeProperties.put(QName.createQName("{test}property1"), "value1");
        
        // Create a workspace that contains the 'live' nodes
        StoreRef storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        
        // Get a reference to the root node
        NodeRef rootNodeRef = this.nodeService.getRootNode(storeRef);
        
        // Create node 
        this.parentNode = this.nodeService.createNode(
                rootNodeRef, 
				DictionaryBootstrap.CHILD_ASSOC_QNAME_CONTAINS, 
                QName.createQName("{}ParentNode"),
                DictionaryBootstrap.TYPE_QNAME_CONTAINER,
                nodeProperties).getChildRef();
        this.nodeService.addAspect(this.parentNode, DictionaryBootstrap.ASPECT_QNAME_LOCKABLE, new HashMap<QName, Serializable>());
        assertNotNull(this.parentNode);
        
        // Add some children to the node
        this.childNode1 = this.nodeService.createNode(
                this.parentNode,
				DictionaryBootstrap.CHILD_ASSOC_QNAME_CONTAINS,
                QName.createQName("{}ChildNode1"),
                DictionaryBootstrap.TYPE_QNAME_CONTAINER,
                nodeProperties).getChildRef();
        this.nodeService.addAspect(this.childNode1, DictionaryBootstrap.ASPECT_QNAME_LOCKABLE, new HashMap<QName, Serializable>());
        assertNotNull(this.childNode1);
        this.childNode2 = this.nodeService.createNode(
                this.parentNode,
				DictionaryBootstrap.CHILD_ASSOC_QNAME_CONTAINS,
                QName.createQName("{}ChildNode2"),
                DictionaryBootstrap.TYPE_QNAME_CONTAINER,
                nodeProperties).getChildRef();
        this.nodeService.addAspect(this.childNode2, DictionaryBootstrap.ASPECT_QNAME_LOCKABLE, new HashMap<QName, Serializable>());
        assertNotNull(this.childNode2);
        
        // Create a node with no lockAspect
        this.noAspectNode = this.nodeService.createNode(
                rootNodeRef, 
				DictionaryBootstrap.CHILD_ASSOC_QNAME_CONTAINS, 
                QName.createQName("{}noAspectNode"),
                DictionaryBootstrap.TYPE_QNAME_CONTAINER,
                nodeProperties).getChildRef();
        assertNotNull(this.noAspectNode);
    }
    
    /**
     * Test lock
     */
    public void testLock()
    {
        // Check that the node is not currently locked
        assertEquals(
                LockStatus.NO_LOCK, 
                this.lockService.getLockStatus(this.parentNode, USER_REF1));
        
        // Test valid lock
        this.lockService.lock(this.parentNode, USER_REF1, LockType.WRITE_LOCK);
        assertEquals(
                LockStatus.LOCK_OWNER, 
                this.lockService.getLockStatus(this.parentNode, USER_REF1));
        assertEquals(
                LockStatus.LOCKED,
                this.lockService.getLockStatus(this.parentNode, USER_REF2));
        
        // Test lock when already locked
        try
        {
            this.lockService.lock(this.parentNode, USER_REF2, LockType.WRITE_LOCK);
            fail("The user should not be able to lock the node since it is already locked by another user.");
        }
        catch (UnableToAquireLockException exception)
        {
        }
        
        // Test already locked by this user
        try
        {
            this.lockService.lock(this.parentNode, USER_REF1, LockType.WRITE_LOCK);
        }
        catch (Exception exception)
        {
            fail("No error should be thrown when a node is re-locked by the current lock owner.");
        }
        
        // Test with no apect node
        try
        {
            this.lockService.lock(this.noAspectNode, USER_REF1, LockType.WRITE_LOCK);
            fail("This node has no lock aspect.");
        }
        catch (AspectMissingException exception)
        {
        }
    }

    /**
     * Test lock with lockChildren == true
     */
    // TODO
    public void testLockChildren()
    {
    }
    
    /**
     * Test lock with collection
     */
    // TODO
    public void testLockMany()
    {
    }
    
    /**
     * Test unlock node
     */
    public void testUnlock()
    {
        // Lock the parent node
        testLock();
        
        // Try and unlock a locked node
        try
        {
            this.lockService.unlock(this.parentNode, USER_REF2);
            fail("A user cannot unlock a node that is currently lock by another user.");
        }
        catch (UnableToReleaseLockException exception)
        {
        }
        
        // Unlock the node
        this.lockService.unlock(this.parentNode, USER_REF1);
        assertEquals(
                LockStatus.NO_LOCK,
                this.lockService.getLockStatus(this.parentNode, USER_REF1));
        assertEquals(
                LockStatus.NO_LOCK,
                this.lockService.getLockStatus(this.parentNode, USER_REF2));
        
        // Try and unlock node with no lock
        try
        {
            this.lockService.unlock(this.parentNode, USER_REF1);
        }
        catch (Exception exception)
        {
            fail("Unlocking an unlocked node should not result in an exception being raised.");
        }
        
        // Test with no apect node
        try
        {
            this.lockService.unlock(this.noAspectNode, USER_REF1);
            fail("This node has no lock aspect.");
        }
        catch (AspectMissingException exception)
        {
        }
    }
    
    // TODO
    public void testUnlockChildren()
    {
    }
    
    // TODO
    public void testUnlockMany()
    {
    }
    
    /**
     * Test getLockStatus
     */
    public void testGetLockStatus()
    {
        // Check an unlocked node
        LockStatus lockStatus1 = this.lockService.getLockStatus(this.parentNode, USER_REF1);
        assertEquals(LockStatus.NO_LOCK, lockStatus1);
        
        // Simulate the node being locked by user1
        this.nodeService.setProperty(this.parentNode, DictionaryBootstrap.PROP_QNAME_LOCK_OWNER, USER_REF1);
        
        // Check for locked status
        LockStatus lockStatus2 = this.lockService.getLockStatus(this.parentNode, USER_REF2);
        assertEquals(LockStatus.LOCKED, lockStatus2);
        
        // Check for lock owner status
        LockStatus lockStatus3 = this.lockService.getLockStatus(this.parentNode, USER_REF1);
        assertEquals(LockStatus.LOCK_OWNER, lockStatus3);
                
        // Test with no apect node
        try
        {
            this.lockService.getLockStatus(this.noAspectNode, USER_REF1);
            fail("This node has no lock aspect.");
        }
        catch (AspectMissingException exception)
        {
        }
    }
    
    /**
     * Test getLockType
     */
    public void testGetLockType()
    {
        // Get the lock type (should be null since the object is not locked)
        LockType lockType1 = this.lockService.getLockType(this.parentNode);
        assertNull(lockType1);
        
        // Lock the object for writing
        this.lockService.lock(this.parentNode, USER_REF1, LockType.WRITE_LOCK);
        LockType lockType2 = this.lockService.getLockType(this.parentNode);
        assertNotNull(lockType2);               
        assertEquals(LockType.WRITE_LOCK, lockType2);
        
        // Unlock the node
        this.lockService.unlock(this.parentNode, USER_REF1);
        LockType lockType3 = this.lockService.getLockType(this.parentNode);
        assertNull(lockType3);
        
        // Lock the object for read only
        this.lockService.lock(this.parentNode, USER_REF1, LockType.READ_ONLY_LOCK);
        LockType lockType4 = this.lockService.getLockType(this.parentNode);
        assertNotNull(lockType4);
        assertEquals(LockType.READ_ONLY_LOCK, lockType4);
        
        // Test with no apect node
        try
        {
            this.lockService.getLockType(this.noAspectNode);
            fail("This node has no lock aspect.");
        }
        catch (AspectMissingException exception)
        {
        }
    }
}
