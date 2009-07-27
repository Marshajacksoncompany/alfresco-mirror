package org.alfresco.module.org_alfresco_module_dod5015.test;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_dod5015.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

public class TestAction2 extends RMActionExecuterAbstractBase
{
    public static final String NAME = "testAction2";
    
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // Do nothing
    }      
    
    @Override
    public boolean isDispositionAction()
    {
        return false;
    }

    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
    {
       return true;
    }
    
    
}
