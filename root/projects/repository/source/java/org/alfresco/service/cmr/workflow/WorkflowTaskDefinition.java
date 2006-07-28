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
package org.alfresco.service.cmr.workflow;

import org.alfresco.service.cmr.dictionary.TypeDefinition;


/**
 * Workflow Task Definition Data Object.
 * 
 * Represents meta-data for a Workflow Task.  The meta-data is described in terms
 * of the Alfresco Data Dictionary.
 * 
 * @author davidc
 */
public class WorkflowTaskDefinition
{
    /** Unique id of Workflow Task Definition */
    public String id;
    
    // TODO: Convert to TaskDefinition (derived from TypeDefinition)
    /** Task Metadata */
    public TypeDefinition metadata;

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "WorkflowTaskDefinition[id=" + id + ",metadata=" + metadata + "]";
    }
}
