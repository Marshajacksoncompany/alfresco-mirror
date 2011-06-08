/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.domain.solr;

import java.util.List;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.solr.NodeParameters;

/**
 * DAO support for SOLR web scripts.
 * 
 * @since 4.0
 */
public interface SOLRDAO
{
    /**
     * Get the ACL changesets for given range parameters
     * 
     * @param minAclChangeSetId         minimum ACL changeset ID - (inclusive and optional)
     * @param fromCommitTime            minimum ACL commit time - (inclusive and optional)
     * @param maxResults                limit the results. 0 or Integer.MAX_VALUE does not limit the results
     * @return                          list of ACL changesets
     */
    public List<AclChangeSet> getAclChangeSets(Long minAclChangeSetId, Long fromCommitTime, int maxResults);
    
    /**
     * Get the transactions from either minTxnId or fromCommitTime, optionally limited to maxResults
     * 
     * @param minTxnId greater than or equal to minTxnId
     * @param fromCommitTime greater than or equal to transaction commit time
     * @param maxResults limit the results. 0 or Integer.MAX_VALUE does not limit the results
     * @return list of transactions
     */
	public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, int maxResults);
	
    /**
     * Get the nodes satisfying the constraints in nodeParameters
     * 
     * @param nodeParameters set of constraints for which nodes to return
     * @param maxResults limit the results. 0 or Integer.MAX_VALUE does not limit the results
     * @return list of matching nodes
     */
	public List<Node> getNodes(NodeParameters nodeParameters);
	
//    /**
//     * The interface that will be used to give query results to the calling code.
//     */
//    public static interface NodeQueryCallback
//    {
//        /**
//         * Handle a node.
//         * 
//         * @param node                      the node
//         * @return                          Return <tt>true</tt> to continue processing rows or <tt>false</tt> to stop
//         */
//        boolean handleNode(Node node);
//    }
//    
//    /**
//     * The interface that will be used to give query results to the calling code.
//     */
//    public static interface NodeMetaDataQueryCallback
//    {
//        /**
//         * Handle a node.
//         * 
//         * @param node                      the node meta data
//         * @return                          Return <tt>true</tt> to continue processing rows or <tt>false</tt> to stop
//         */
//        boolean handleNodeMetaData(NodeMetaData nodeMetaData);
//    }
}
