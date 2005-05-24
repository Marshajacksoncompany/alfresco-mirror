/*
 * Created on Mar 24, 2005
 * 
 * TODO Comment this class
 * 
 * 
 */
package org.alfresco.repo.search;

import org.alfresco.repo.ref.Path;
import org.alfresco.repo.ref.QName;
import org.alfresco.repo.ref.StoreRef;

/**
 * Ths encapsultes the execution of search against different indexing
 * mechanisms.
 * 
 * Canned queries have been translated into the query string by this stage.
 * Handling of parameterisation is left to the implementation.
 * 
 * @author andyh
 * 
 */
public interface Searcher
{
    /**
     * Search against a store.
     * 
     * @param store -
     *            the store against which to search
     * @param language -
     *            the query language
     * @param query -
     *            the query string - which may include parameters
     * @param attributePaths -
     *            explicit list of attributes/properties to extract for the selected nodes in xpath style syntax
     * @param queryParameterDefinition - query parameter definitions - the default value is used for the value.
     * @return
     */
    public ResultSet query(StoreRef store, String language, String query, Path[] attributePaths,
            QueryParameterDefinition[] queryParameterDefinitions);
    
    /**
     * Search against a store.
     * Pulls back all attributes on each node.
     * Does not allow parameterisation.
     * 
     * @param store -
     *            the store against which to search
     * @param language -
     *            the query language
     * @param query -
     *            the query string - which may include parameters
     * @return
     */
    public ResultSet query(StoreRef store, String language, String query);
    
    /**
     * Search against a store.
     * 
     * @param store -
     *            the store against which to search
     * @param language -
     *            the query language
     * @param query -
     *            the query string - which may include parameters
     * @param queryParameterDefinition - query parameter definitions - the default value is used for the value.
     * @return
     */
    public ResultSet query(StoreRef store, String language, String query, QueryParameterDefinition[] queryParameterDefintions);
    
    
    /**
     * Search against a store.
     * 
     * @param store -
     *            the store against which to search
     * @param language -
     *            the query language
     * @param query -
     *            the query string - which may include parameters
     * @param attributePaths -
     *            explicit list of attributes/properties to extract for the selected nodes in xpath style syntax
     * @return
     */
    public ResultSet query(StoreRef store, String language, String query, Path[] attributePaths);
    
    
    /**
     * Execute a canned query
     * 
     * @param store -
     *       the store against which to search
     * @param queryId - the query identifier
     * @param queryParameters - parameterisation for the canned query
     * @return the query results
     */
    public ResultSet query(StoreRef store, QName queryId,  QueryParameter[] queryParameters);
}
