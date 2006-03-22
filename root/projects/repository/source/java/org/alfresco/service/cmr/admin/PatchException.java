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
package org.alfresco.service.cmr.admin;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when a patch fails to execute successfully.
 * 
 * @author Derek Hulley
 */
public class PatchException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 7022368915143884315L;

    /**
     * @param msgId the patch failure message ID
     */
    public PatchException(String msgId)
    {
        super(msgId);
    }

    /**
     * @param msgId the patch failure message ID
     * @param args variable number of message arguments
     */
    public PatchException(String msgId, Object ... args)
    {
        super(msgId, args);
    }
}
