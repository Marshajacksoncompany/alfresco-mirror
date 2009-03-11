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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Action service interface
 * 
 * @author Roy Wetherall
 */
@PublicService
public interface ActionService
{
    /**
     * Get a named action definition
     * 
     * @param name    the name of the action definition
     * @return        the action definition
     */
    @Auditable(parameters = {"name"})
    ActionDefinition getActionDefinition(String name);
    
    /**
     * Get all the action definitions
     * 
     * @return    the list action definitions
     */
    @Auditable()
    List<ActionDefinition> getActionDefinitions();
    
    /**
     * Get all the action definitions that are applicable for the given node, based on
     * its type and aspects.
     * 
     * @param nodeRef   the node reference
     * @return          a list of applicable action definitions
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    List<ActionDefinition> getActionDefinitions(NodeRef nodeRef);
    
    /**
     * Get a named action condition definition
     * 
     * @param name    the name of the action condition definition
     * @return        the action condition definition
     */
    @Auditable(parameters = {"name"})
    ActionConditionDefinition getActionConditionDefinition(String name);
    
    /**
     * Get all the action condition definitions
     * 
     * @return    the list of action condition definitions
     */
    @Auditable(parameters = {})
    List<ActionConditionDefinition> getActionConditionDefinitions();
    
    /**
     * Create a new action
     * 
     * @param name    the action definition name
     * @return        the action
     */
    @Auditable(parameters = {"name"})
    Action createAction(String name);
    
    /**
     * Create a new action specifying the initial set of parameter values
     * 
     * @param name        the action definition name
     * @param params    the parameter values
     * @return            the action
     */
    @Auditable(parameters = {"name", "params"})
    Action createAction(String name, Map<String, Serializable> params);
    
    /**
     * Create a composite action 
     * 
     * @return    the composite action
     */
    @Auditable()
    CompositeAction createCompositeAction();
    
    /**
     * Create an action condition
     * 
     * @param name    the action condition definition name
     * @return        the action condition
     */
    @Auditable(parameters = {"name"})
    ActionCondition createActionCondition(String name);
    
    /**
     * Create an action condition specifying the initial set of parameter values
     * 
     * @param name        the action condition definition name
     * @param params    the parameter values
     * @return            the action condition
     */
    @Auditable(parameters = {"name", "params"})
    ActionCondition createActionCondition(String name, Map<String, Serializable> params);
    
   /**
    * Create a composite actionCondition
    * @return  the composite actionCondition
    */
    @Auditable()
   CompositeActionCondition createCompositeActionCondition();
    
    /**
     * The actions conditions are always checked.
     * 
     * @see ActionService#executeAction(Action, NodeRef, boolean)
     *  
     * @param action                the action
     * @param actionedUponNodeRef    the actioned upon node reference
     */
    @Auditable(key = Auditable.Key.ARG_1, parameters = {"action", "actionedUponNodeRef" })
    void executeAction(Action action, NodeRef actionedUponNodeRef);
    
    /**
     * The action is executed based on the asynchronous attribute of the action.
     * 
     * @see ActionService#executeAction(Action, NodeRef, boolean, boolean)
     * 
     * @param action                the action
     * @param actionedUponNodeRef    the actioned upon node reference
     * @param checkConditions        indicates whether the conditions should be checked
     */
    @Auditable(key = Auditable.Key.ARG_1, parameters = {"action", "actionedUponNodeRef", "checkConditions" })
    void executeAction(Action action, NodeRef actionedUponNodeRef, boolean checkConditions);
    
    /**
     * Executes the specified action upon the node reference provided.
     * <p>
     * If specified that the conditions should be checked then any conditions
     * set on the action are evaluated.
     * <p>
     * If the conditions fail then the action is not executed.
     * <p>
     * If an action has no conditions then the action will always be executed.
     * <p>
     * If the conditions are not checked then the action will always be executed.
     * 
     * @param action                the action
     * @param actionedUponNodeRef    the actioned upon node reference
     * @param checkConditions        indicates whether the conditions should be checked before
     *                                 executing the action
     * @param executeAsynchronously    indicates whether the action should be executed asychronously or not, this value overrides
     *                                 the value set on the action its self
     */
    @Auditable(key = Auditable.Key.ARG_1, parameters = {"action", "actionedUponNodeRef", "checkConditions", "executeAsynchronously" })
    void executeAction(Action action, NodeRef actionedUponNodeRef, boolean checkConditions, boolean executeAsynchronously);
    
    /**
     * Evaluted the conditions set on an action.
     * <p>
     * Returns true if the action has no conditions.
     * <p>
     * If the action has more than one condition their results are combined using the 'AND' 
     * logical operator.
     * 
     * @param action                the action
     * @param actionedUponNodeRef    the actioned upon node reference
     * @return                        true if the condition succeeds, false otherwise
     */
    @Auditable(key = Auditable.Key.ARG_1, parameters = {"action", "actionedUponNodeRef" })
    boolean evaluateAction(Action action, NodeRef actionedUponNodeRef);
    
    /**
     * Evaluate an action condition.
     * 
     * @param condition                the action condition
     * @param actionedUponNodeRef    the actioned upon node reference
     * @return                        true if the condition succeeds, false otherwise
     */
    @Auditable(key = Auditable.Key.ARG_1, parameters = {"condition", "actionedUponNodeRef" })
    boolean evaluateActionCondition(ActionCondition condition, NodeRef actionedUponNodeRef);
    
    /**
     * Save an action against a node reference.
     * <p>
     * The node will be made configurable if it is not already.
     * <p>
     * If the action already exists then its details will be updated.
     * 
     * @param nodeRef    the node reference
     * @param action    the action
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "action" })
    void saveAction(NodeRef nodeRef, Action action);
    
    /**
     * Gets all the actions currently saved on the given node reference.
     * 
     * @param nodeRef    the node reference
     * @return            the list of actions
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    List<Action> getActions(NodeRef nodeRef);
    
    /**
     * Gets an action stored against a given node reference.
     * <p>
     * Returns null if the action can not be found.
     * 
     * @param nodeRef    the node reference
     * @param actionId    the action id
     * @return            the action
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "actionId"})
    Action getAction(NodeRef nodeRef, String actionId);
    
    /**
     * Removes an action associated with a node reference.
     * 
     * @param nodeRef        the node reference
     * @param action        the action
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "action" })
    void removeAction(NodeRef nodeRef, Action action);
    
    /**
     * Removes all actions associated with a node reference
     * 
     * @param nodeRef    the node reference
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    void removeAllActions(NodeRef nodeRef);
    
}
