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
package org.alfresco.repo.avm.actions;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reverts a node and everything underneath it to a specified version.
 * @author britt
 * 
 * @deprecated see org.alfresco.wcm.actions.WCMSandboxRevertSnapshotAction or org.alfresco.wcm.SandboxService.revertSnapshot
 */
public class AVMRevertStoreAction extends ActionExecuterAbstractBase 
{
    @SuppressWarnings("unused")
    private static Log    fgLogger = LogFactory.getLog(AVMRevertStoreAction.class);
    
    public static final String NAME = "avm-revert-store";
    public static final String PARAM_VERSION = "version";
    
    /**
     * The AVM Synchronization Service.
     */
    private AVMSyncService fSyncService;
 
    /**
     * Set the Sync Service.
     */
    public void setAvmSyncService(AVMSyncService service)
    {
        fSyncService = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) 
    {
        // All this does is an override submit from the older version
        // to head of the store implied in the path.
        Pair<Integer, String> pathVersion = 
            AVMNodeConverter.ToAVMVersionPath(actionedUponNodeRef);
        int revertVersion = (Integer)action.getParameterValue(PARAM_VERSION);
        List<AVMDifference> diffs =
            fSyncService.compare(revertVersion, pathVersion.getSecond(), 
                                 -1, pathVersion.getSecond(), null);
        String message = "Reverted to Version " + revertVersion + ".";
        fSyncService.update(diffs, null, false, false, true, true, message, message);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(
            new ParameterDefinitionImpl(PARAM_VERSION,                              
                                        DataTypeDefinition.INT,                       
                                        true,                                           
                                        getParamDisplayLabel(PARAM_VERSION)));      
    }
}
