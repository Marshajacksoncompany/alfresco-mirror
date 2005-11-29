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
package org.alfresco.util;


import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;

/**
 * Base Alfresco test.
 * 
 * Creates a store and root node that can be used in the tests.
 * 
 * @author Roy Wetherall
 */
public abstract class BaseAlfrescoTestCase extends TestCase
{
    /** the context to keep between tests */
    public static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    /** The node service */
    protected NodeService nodeService;
    
    /** The content service */
    protected ContentService contentService;
    
    /** The authentication service */
    protected AuthenticationService authenticationService;
    
    /** The store reference */
    protected StoreRef storeRef;
    
    /** The root node reference */
    protected NodeRef rootNodeRef;
    
    
    protected ActionService actionService;
    protected TransactionService transactionService;
    
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        //Get a reference to the node service
        this.nodeService = (NodeService)ctx.getBean("NodeService");
        this.contentService = (ContentService)ctx.getBean("ContentService");
        this.authenticationService = (AuthenticationService)ctx.getBean("authenticationService");
        this.actionService = (ActionService)ctx.getBean("actionService");
        this.transactionService = (TransactionService)ctx.getBean("transactionComponent");
        
        // Authenticate as the system user - this must be done before we create the store
        AuthenticationComponent authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // Create the store and get the root node
        this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.storeRef);
        
       
    }
    
    
    @Override
    protected void tearDown() throws Exception
    {
        authenticationService.clearCurrentSecurityContext();
        super.tearDown();
    }
    
}