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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.filesys.avm;

import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.util.WildCard;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;

/**
 * AVM Filesystem Search Context Class
 * 
 * <p>Contains the details of a wildcard folder search.
 *
 * @author GKSpencer
 */
public class AVMSearchContext extends SearchContext {

	// File list and current index
	
	private AVMNodeDescriptor[] m_fileList;
	private int m_fileIdx;
	
	// File attributes
	
	private int m_attrib;
	
	// Optional wildcard filter
	
	private WildCard m_filter;
	
	// Relative path to the parent folder being searched
	
	private String m_parentPath;
	
	// Mark all files/folders as read-only
	
	private boolean m_readOnly;
	
	/**
	 * Class constructor
	 * 
	 * @param fileList SortedMap<String, AVMNodeDescriptor>
	 * @param attrib int
	 * @param filter WildCard
	 * @param parentPath String
	 * @param readOnly boolean
	 */
	public AVMSearchContext( AVMNodeDescriptor[]  fileList, int attrib, WildCard filter, String parentPath, boolean readOnly)
	{
		m_attrib   = attrib;
		m_filter   = filter;
		m_fileList = fileList;
		
		m_parentPath = parentPath;
		if ( m_parentPath != null && m_parentPath.endsWith( FileName.DOS_SEPERATOR_STR) == false)
			m_parentPath = m_parentPath + FileName.DOS_SEPERATOR_STR;
		
		m_readOnly = readOnly;
	}
	
    /**
     * Determine if there are more files for the active search.
     * 
     * @return boolean
     */
    public boolean hasMoreFiles()
    {
    	return m_fileIdx < m_fileList.length ? true : false;
    }

    /**
     * Return file information for the next file in the active search. Returns false if the search
     * is complete.
     * 
     * @param info FileInfo to return the file information.
     * @return true if the file information is valid, else false
     */
    public boolean nextFileInfo(FileInfo info)
    {
    	// Check if there is another file record to return
    	
    	if ( m_fileIdx >= m_fileList.length)
    		return false;

    	// Search for the next valid file
    	
        boolean foundMatch = false;
    	AVMNodeDescriptor curFile = null;
    	
        while (foundMatch == false && m_fileIdx < m_fileList.length)
        {
        	// 	Get the next file from the list
        	
        	curFile = m_fileList[ m_fileIdx++];
        	
        	//	Check if the file name matches the search pattern
  				
  			if ( m_filter.matchesPattern(curFile.getName()) == true)
  			{
  					
  				//  Check if the file matches the search attributes
  	
  				if (FileAttribute.hasAttribute(m_attrib, FileAttribute.Directory) &&
  					curFile.isDirectory())
  				{
  	
  					//  Found a match
  	
  					foundMatch = true;
  				}
  				else if ( curFile.isFile())
  				{
  	
  					//  Found a match
  	
  					foundMatch = true;
  				}

  				//	Check if we found a match
  				
  				if ( foundMatch == false)
  				{
  					
  					//  Get the next file from the list

  					if ( ++m_fileIdx < m_fileList.length)
  			        	curFile = m_fileList[ m_fileIdx];
  				}
  			}
        }

        // If we found a match then fill in the file information

        if ( foundMatch)
        {
        	// Fill in the file information
        	
        	info.setFileName( curFile.getName());
        	
        	if ( curFile.isFile())
        	{
        		info.setFileSize( curFile.getLength());
        		info.setAllocationSize((curFile.getLength() + 512L) & 0xFFFFFFFFFFFFFE00L);
        	}
        	else
        		info.setFileSize( 0L);

        	info.setAccessDateTime( curFile.getAccessDate());
        	info.setCreationDateTime( curFile.getCreateDate());
        	info.setModifyDateTime( curFile.getModDate());

        	// Build the file attributes
        	
        	int attr = 0;
        	
        	if ( curFile.isDirectory())
        		attr += FileAttribute.Directory;
        	
        	if ( curFile.getName().startsWith( ".") ||
        			curFile.getName().equalsIgnoreCase( "Desktop.ini") ||
        			curFile.getName().equalsIgnoreCase( "Thumbs.db"))
        		attr += FileAttribute.Hidden;

        	if ( isReadOnly())
        		attr += FileAttribute.ReadOnly;
        	
        	if ( attr == 0)
        		attr = FileAttribute.NTNormal;
        	
        	info.setFileAttributes( attr);
        	
        	// Generate a file id for the current file
        	
        	StringBuilder pathStr = new StringBuilder( m_parentPath);
        	pathStr.append ( curFile.getName());
        	
        	info.setFileId( pathStr.toString().hashCode());
        }
        
        // Indicate if the file information is valid
        
    	return foundMatch;
    }

    /**
     * Return the file name of the next file in the active search. Returns null is the search is
     * complete.
     * 
     * @return String
     */
    public String nextFileName()
    {
    	// Check if there is another file record to return
    	
    	//	Find the next matching file name
    	
    	while ( m_fileIdx < m_fileList.length) {
    		
    		//	Check if the current file name matches the search pattern
    		
    		String fname = m_fileList[m_fileIdx++].getName();
    		
    		if ( m_filter.matchesPattern(fname))
    			return fname;
    	}
    	
    	// No more matching file names
    	
    	return null;
    }

    /**
     * Return the total number of file entries for this search if known, else return -1
     * 
     * @return int
     */
    public int numberOfEntries()
    {
        return m_fileList.length;
    }

    /**
     * Return the resume id for the current file/directory in the search.
     * 
     * @return int
     */
    public int getResumeId()
    {
    	return m_fileIdx;
    }
    
    /**
     * Restart a search at the specified resume point.
     * 
     * @param resumeId Resume point id.
     * @return true if the search can be restarted, else false.
     */
    public boolean restartAt(int resumeId)
    {
    	// Range check the resume id
    	
    	int resId = resumeId - 1;
    	
    	if ( resId < 0 || resId >= m_fileList.length)
    		return false;
    	
    	// Reset the current file index
    	
    	m_fileIdx = resId;
    	return true;
    }

    /**
     * Restart the current search at the specified file.
     * 
     * @param info File to restart the search at.
     * @return true if the search can be restarted, else false.
     */
    public boolean restartAt(FileInfo info)
    {
    	// Search backwards from the current file
    	
    	int curFileIdx = m_fileIdx;

        if (m_fileIdx >= m_fileList.length)
        {
            m_fileIdx = m_fileList.length - 1;
        }
        
    	while ( m_fileIdx > 0) {
    		
    		// Check if the current file is the required search restart point
    		
    		if ( m_fileList[ m_fileIdx].getName().equals( info.getFileName()))
    			return true;
    		else
    			m_fileIdx--;
    	}
    	
    	// Failed to find the restart file
    	
    	m_fileIdx = curFileIdx;
    	return false;
    }
    
    /**
     * Check if all files/folders returned by the search should be marked as read-only
     * 
     * @return boolean
     */
    public final boolean isReadOnly()
    {
    	return m_readOnly;
    }
    
    /**
     * Set all files/folders returned by the search as read-only
     * 
     * @param readOnly boolean
     */
    public final void setReadOnly( boolean readOnly)
    {
    	m_readOnly = readOnly;
    }
}
