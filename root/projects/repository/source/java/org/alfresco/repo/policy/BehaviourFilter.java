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
package org.alfresco.repo.policy;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Contract disabling and enabling policy behaviours.
 * 
 * @See org.alfresco.repo.policy.PolicyComponent
 * 
 * @author David Caruana
 */
public interface BehaviourFilter
{
    /**
     * Disable behaviour for a type or aspect for all nodes.
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param className  the type/aspect behaviour to disable
     * @return  true => already disabled
     */
    public boolean disableBehaviour(QName className);

    /**
     * Disable behaviour for specific node
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param nodeRef  the node to disable for
     * @param className  the type/aspect behaviour to disable
     * @return  true => already disabled
     */
    public boolean disableBehaviour(NodeRef nodeRef, QName className);

    /**
     * Enable behaviour for all nodes
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param className  the type/aspect behaviour to enable
     */
    public void enableBehaviour(QName className);
    
    /**
     * Enable behaviour for specific node
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param nodeRef  the node to enable for
     * @param className  the type/aspect behaviour to enable
     */
    public void enableBehaviour(NodeRef nodeRef, QName className);

    /**
     * Enable all behaviours for specific node
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param nodeRef  the node to enable for
     */
    public void enableBehaviours(NodeRef nodeRef);
    
    /**
     * Disable all behaviours.   Once this method is called the node and class level filters, enableBehaviours and disableBehaviours 
     * methods have no effect, every behaviour is disabled.   
     * EnableAllBehaviours reverses the result of calling this method.  
     * <p>
     * Calling this method may result in nodes existing in your repository that do not conform to your policies.
     *  
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * @see #enableAllBehaviours
     */
    public void disableAllBehaviours();
        
    /**
     * Enable all behaviours i.e. undo all disable calls - at the global,
     * node and class level.
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     */
    public void enableAllBehaviours();
    
    /**
     * Determine if behaviour is enabled across all nodes.
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param className  the behaviour to test for
     * @return  true => behaviour is enabled
     */
    public boolean isEnabled(QName className);
    
    /**
     * Determine if behaviour is enabled for specific node.
     * <p> 
     * Note: A node behaviour is enabled only when:
     *       a) the behaviour is not disabled across all nodes
     *       b) the behaviour is not disabled specifically for the provided node
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @param nodeRef  the node to test for
     * @param className  the behaviour to test for
     * @return  true => behaviour is enabled
     */
    public boolean isEnabled(NodeRef nodeRef, QName className);
    
    /**
     * Determine if any behaviours have been disabled?
     * <p>
     * The change applies <b>ONLY</b> to the current transaction.
     * 
     * @return  true => behaviours have been filtered
     */
    public boolean isActivated();
}
