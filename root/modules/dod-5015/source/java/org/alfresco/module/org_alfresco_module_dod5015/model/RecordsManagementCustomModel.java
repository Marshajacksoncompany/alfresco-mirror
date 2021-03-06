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
package org.alfresco.module.org_alfresco_module_dod5015.model;

import org.alfresco.service.namespace.QName;

/**
 * Helper class containing records management custom model qualified names
 * 
 * @author Gavin Cornwell
 */
public interface RecordsManagementCustomModel
{
    // Namespace details
    public static String RM_CUSTOM_URI = "http://www.alfresco.org/model/rmcustom/1.0";
    public static String RM_CUSTOM_PREFIX = "rmc";
    
    // Model
    public static QName RM_CUSTOM_MODEL = QName.createQName(RM_CUSTOM_URI, "rmcustom");
    
    // Custom constraint for Supplemental Marking List
    public static QName CONSTRAINT_CUSTOM_SMLIST = QName.createQName(RM_CUSTOM_URI, "smList");
    
    // Custom property for for Supplemental Marking List
    public static QName PROP_SUPPLEMENTAL_MARKING_LIST = QName.createQName(RM_CUSTOM_URI, "supplementalMarkingList");
    
    // Supplemental Marking List aspect
    public static QName ASPECT_SUPPLEMENTAL_MARKING_LIST = QName.createQName(RM_CUSTOM_URI, "customSupplementalMarkingList");
    
    // Custom associations aspect
    public static QName ASPECT_CUSTOM_ASSOCIATIONS = QName.createQName(RM_CUSTOM_URI, "customAssocs");
    
    // Custom record series properties aspect
    public static QName ASPECT_CUSTOM_RECORD_SERIES_PROPERTIES = QName.createQName(RM_CUSTOM_URI, "customRecordSeriesProperties");
    
    // Custom record category properties aspect
    public static QName ASPECT_CUSTOM_RECORD_CATEGORY_PROPERTIES = QName.createQName(RM_CUSTOM_URI, "customRecordCategoryProperties");
    
    // Custom record folder properties aspect
    public static QName ASPECT_CUSTOM_RECORD_FOLDER_PROPERTIES = QName.createQName(RM_CUSTOM_URI, "customRecordFolderProperties");
    
    // Custom record properties aspect
    public static QName ASPECT_CUSTOM_RECORD_PROPERTIES = QName.createQName(RM_CUSTOM_URI, "customRecordProperties");
    
    // Some Custom references which are present on system startup.
    public static QName CUSTOM_REF_VERSIONS = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "versions");
    public static QName CUSTOM_REF_SUPERSEDES = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "supersedes");
}
