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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.module.org_alfresco_module_dod5015.audit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Records Management Audit Service Implementation.
 * 
 * @author Gavin Cornwell
 * @since 3.2
 */
public class RecordsManagementAuditServiceImpl implements RecordsManagementAuditService
{
	/** Logger */
    private static Log logger = LogFactory.getLog(RecordsManagementAuditServiceImpl.class);

    protected static final String AUDIT_TRAIL_FILE_PREFIX = "audit_";
    protected static final String AUDIT_TRAIL_FILE_SUFFIX = ".json";
        
    private AuditService auditService;

    // temporary field to hold imaginary enabled flag
    private boolean enabled = false;
    
    /**
     * Sets the AuditService instance
     */
	public void setAuditService(AuditService auditService)
	{
		this.auditService = auditService;
	}
	
    /**
     * {@inheritDoc}
     */
	public boolean isEnabled()
    {
        return this.enabled;
    }
	
    /**
     * {@inheritDoc}
     */
    public void start()
    {
        // TODO: Start RM auditing properly!
        this.enabled = true;
        
        if (logger.isInfoEnabled())
            logger.info("Started Records Management auditing");
    }

    /**
     * {@inheritDoc}
     */
    public void stop()
    {
        // TODO: Stop RM auditing properly!
        this.enabled = false;
        
        if (logger.isInfoEnabled())
            logger.info("Stopped Records Management auditing");
    }
    
    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        // TODO: Clear the RM audit trail
        
        if (logger.isInfoEnabled())
            logger.debug("Records Management audit log has been cleared");
    }
    
    /**
     * {@inheritDoc}
     */
    public Date getDateLastStarted()
    {
        // TODO: return proper date, for now it's today's date
        return new Date();
    }
    
    /**
     * {@inheritDoc}
     */
    public Date getDateLastStopped()
    {
        // TODO: return proper date, for now it's today's date
        return new Date();
    }
    
    /**
     * {@inheritDoc}
     */
    public File getAuditTrailFile(RecordsManagementAuditQueryParameters params)
    {
        ParameterCheck.mandatory("params", params);
        
        FileWriter fileWriter = null;
        try
        {
            File auditTrailFile = TempFileProvider.createTempFile(AUDIT_TRAIL_FILE_PREFIX, AUDIT_TRAIL_FILE_SUFFIX);
            fileWriter = new FileWriter(auditTrailFile);
            // Get the results, dumping to file
            getAuditTrailImpl(params, null, fileWriter);
            // Done
            return auditTrailFile;
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("Failed to generate audit trail file", e);
        }
        finally
        {
            // close the writer
            try { fileWriter.close(); } catch (IOException closeEx) {}
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List<RecordsManagementAuditEntry> getAuditTrail(RecordsManagementAuditQueryParameters params)
    {
        ParameterCheck.mandatory("params", params);
        
        List<RecordsManagementAuditEntry> entries = new ArrayList<RecordsManagementAuditEntry>(50);
        try
        {
            getAuditTrailImpl(params, entries, null);
            // Done
            return entries;
        }
        catch (Throwable e)
        {
            // Should be
            throw new AlfrescoRuntimeException("Failed to generate audit trail", e);
        }
    }
    
    /**
     * Get the audit trail, optionally dumping the results the the given writer dumping to a list.
     * 
     * @param params                the search parameters
     * @param results               the list to which individual results will be dumped
     */
    private void getAuditTrailImpl(
            RecordsManagementAuditQueryParameters params,
            final List<RecordsManagementAuditEntry> results,
            final Writer writer)
            throws IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("Retrieving audit trail using parameters: " + params);
        
        // define the callback
        AuditQueryCallback callback = new AuditQueryCallback()
        {
            private boolean firstEntry = true;
            
            public boolean handleAuditEntry(
                    Long entryId,
                    String applicationName,
                    String user,
                    long time,
                    Map<String, Serializable> values)
            {
                Date timestamp = new Date(time);
                String fullName = (String) values.get(RecordsManagementAuditService.RM_AUDIT_DATA_PERSON_FULLNAME);
                if (fullName == null)
                {
                    logger.warn(
                            "RM Audit: No value for '" +
                            RecordsManagementAuditService.RM_AUDIT_DATA_PERSON_FULLNAME + "': " + entryId);
                }
                String userRole = (String) values.get(RecordsManagementAuditService.RM_AUDIT_DATA_PERSON_ROLE);
                if (userRole == null)
                {
                    logger.warn(
                            "RM Audit: No value for '" +
                            RecordsManagementAuditService.RM_AUDIT_DATA_PERSON_ROLE + "': " + entryId);
                }
                NodeRef nodeRef = (NodeRef) values.get(RecordsManagementAuditService.RM_AUDIT_DATA_NODE_NODEREF);
                if (nodeRef == null)
                {
                    logger.warn(
                            "RM Audit: No value for '" +
                            RecordsManagementAuditService.RM_AUDIT_DATA_NODE_NODEREF + "': " + entryId);
                }
                String nodeName = (String) values.get(RecordsManagementAuditService.RM_AUDIT_DATA_NODE_NAME);
                if (nodeName == null)
                {
                    logger.warn(
                            "RM Audit: No value for '" +
                            RecordsManagementAuditService.RM_AUDIT_DATA_NODE_NAME + "': " + entryId);
                }
                String description = (String) values.get(RecordsManagementAuditService.RM_AUDIT_DATA_ACTIONDESCRIPTION_VALUE);
                if (description == null)
                {
                    logger.warn(
                            "RM Audit: No value for '" +
                            RecordsManagementAuditService.RM_AUDIT_DATA_ACTIONDESCRIPTION_VALUE + "': " + entryId);
                }
                
                RecordsManagementAuditEntry entry = new RecordsManagementAuditEntry(
                        timestamp,
                        user,
                        fullName,
                        userRole,
                        nodeRef,
                        nodeName,
                        description);
                
                // write out the entry to the file in JSON format
                writeEntryToFile(entry);
                
                if (results != null)
                {
                    results.add(entry);
                }
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("   " + entry);
                }
                
                // Keep going
                return true;
            }
            
            private void writeEntryToFile(RecordsManagementAuditEntry entry)
            {
                if (writer == null)
                {
                    return;
                }
                try
                {
                    if (!firstEntry)
                    {
                        writer.write(",");
                    }
                    else
                    {
                        firstEntry = false;
                    }
                    
                    // write the entry to the file
                    writer.write("\n\t\t");
                    writer.write(entry.toJSONString());
                }
                catch (IOException ioe)
                {
                    throw new AlfrescoRuntimeException("Failed to generate audit trail file", ioe);
                }
            }
        };
        
        String user = params.getUser();
        Long fromTime = (params.getDateFrom() == null ? null : new Long(params.getDateFrom().getTime()));
        Long toTime = (params.getDateTo() == null ? null : new Long(params.getDateTo().getTime()));
        int maxEntries = params.getMaxEntries();
        
        // start the audit trail JSON
        writeAuditTrailHeader(writer);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("RM Audit: Issuing query: " + params);
        }
        
        NodeRef nodeRef = params.getNodeRef();
        if (nodeRef != null)
        {
            auditService.auditQuery(
                    callback,
                    RecordsManagementAuditService.RM_AUDIT_APPLICATION_NAME,
                    user,
                    fromTime,
                    toTime,
                    RecordsManagementAuditService.RM_AUDIT_DATA_NODE_NODEREF, nodeRef.toString(),
                    maxEntries);
        }
        else
        {
            auditService.auditQuery(
                    callback,
                    RecordsManagementAuditService.RM_AUDIT_APPLICATION_NAME,
                    user,
                    fromTime,
                    toTime,
                    null, null,
                    maxEntries);
        }
        
        // finish off the audit trail JSON
        writeAuditTrailFooter(writer);
    }
    
    /**
     * {@inheritDoc}
     */
    public NodeRef fileAuditTrailAsRecord(RecordsManagementAuditQueryParameters params, NodeRef destination)
    {
        // NOTE: the underlying RM services checks the pre-conditions
        
        // get the audit trail for the provided parameters
        
        // file the audit log as an undeclared record
        
        return null;
    }
    
    /**
     * Writes the start of the audit JSON stream to the given writer
     * 
     * @param writer The writer to write to
     * @throws IOException
     */
    private void writeAuditTrailHeader(Writer writer) throws IOException
    {
        if (writer == null)
        {
            return;
        }
        writer.write("{\n\t\"data\":\n\t{");
        writer.write("\n\t\t\"started\": \"");
        writer.write(ISO8601DateFormat.format(getDateLastStarted()));
        writer.write("\",\n\t\t\"stopped\": \"");
        writer.write(ISO8601DateFormat.format(getDateLastStopped()));
        writer.write("\",\n\t\t\"enabled\": ");
        writer.write(Boolean.toString(isEnabled()));
        writer.write(",\n\t\t\"entries\":[");
    }
    
    /**
     * Writes the end of the audit JSON stream to the given writer
     * 
     * @param writer The writer to write to
     * @throws IOException
     */
    private void writeAuditTrailFooter(Writer writer) throws IOException
    {
        if (writer == null)
        {
            return;
        }
        writer.write("\n\t\t]\n\t}\n}");
    }
}