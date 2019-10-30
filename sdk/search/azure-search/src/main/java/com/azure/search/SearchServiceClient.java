// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzeResult;
import com.azure.search.models.DataSource;
import com.azure.search.models.Index;
import com.azure.search.models.IndexGetStatisticsResult;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.IndexerListResult;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.Skillset;
import com.azure.search.models.SkillsetListResult;
import com.azure.search.models.SynonymMap;

import org.apache.commons.lang3.NotImplementedException;

@ServiceClient(builder = SearchServiceClientBuilder.class)
public class SearchServiceClient {

    private final ClientLogger logger = new ClientLogger(SearchServiceClient.class);

    private final SearchServiceAsyncClient asyncClient;

    SearchServiceClient(SearchServiceAsyncClient searchServiceAsyncClient) {
        this.asyncClient = searchServiceAsyncClient;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return the pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.asyncClient.getHttpPipeline();
    }

    /**
     * Initializes a new {@link SearchIndexClient} using the given Index name and the
     * same configuration as the SearchServiceClient.
     *
     * @param indexName the name of the Index for the client
     * @return a {@link SearchIndexClient} created from the service client configuration
     */
    public SearchIndexClient getIndexClient(String indexName) {
        return new SearchIndexClient(asyncClient.getIndexClient(indexName));
    }

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.asyncClient.getApiVersion();
    }

    /**
     * Gets the endpoint for the Azure Cognitive Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.asyncClient.getEndpoint();
    }

    /**
     * @param dataSource the name of the datasource to be created
     * @return the created DataSource.
     * @throws NotImplementedException not implemented
     */
    public DataSource createOrUpdateDataSource(DataSource dataSource) {
        return asyncClient.createOrUpdateDataSource(dataSource).block();
    }

    /**
     * Creates a new Azure Search datasource or updates a datasource if it already exists
     *
     * @param dataSourceName The name of the datasource to create or update.
     * @param dataSource The definition of the datasource to create or update.
     * @param requestOptions Request options
     * @param accessCondition Access conditions
     * @param context Context
     * @return a datasource response
     */
    public DataSource createOrUpdateDataSource(
        String dataSourceName,
        DataSource dataSource,
        RequestOptions requestOptions,
        AccessCondition accessCondition,
        Context context) {
        return asyncClient.createOrUpdateDataSource(dataSourceName,
            dataSource,
            requestOptions,
            accessCondition,
            context).block();
    }

    /**
     * Creates a new Azure Search datasource or updates a datasource if it already exists
     *
     * @param dataSourceName The name of the datasource to create or update.
     * @param dataSource The definition of the datasource to create or update.
     * @param requestOptions Request options
     * @param accessCondition Access conditions
     * @param context Context
     * @return a datasource response
     */
    public Response<DataSource> createOrUpdateDataSourceWithResponse(
        String dataSourceName,
        DataSource dataSource,
        RequestOptions requestOptions,
        AccessCondition accessCondition,
        Context context) {
        return asyncClient.createOrUpdateDataSourceWithResponse(dataSourceName,
            dataSource,
            requestOptions,
            accessCondition,
            context).block();
    }

    /**
     * @return the DataSource.
     * @throws NotImplementedException not implemented
     */
    public DataSource getDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the DataSource.
     * @throws NotImplementedException not implemented
     */
    public Response<DataSource> getDataSourceWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @return a list of DataSources
     */
    public PagedIterable<DataSource> listDataSources() {
        return new PagedIterable<>(asyncClient.listDataSources());
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of DataSource definitions to retrieve.
     *               Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *               The default is all properties.
     *
     * @return a list of DataSources
     */
    public PagedIterable<DataSource> listDataSources(String select) {
        return new PagedIterable<>(asyncClient.listDataSources(select));
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of DataSource definitions to retrieve.
     *               Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *               The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging.
     *
     * @return a list of DataSources
     */
    public PagedIterable<DataSource> listDataSources(String select, RequestOptions requestOptions) {
        return new PagedIterable<>(asyncClient.listDataSources(select, requestOptions));
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of DataSource definitions to retrieve.
     *               Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *               The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return a list of DataSources
     */
    public PagedIterable<DataSource> listDataSources(String select, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listDataSources(select, requestOptions, context));
    }

    /**
     * @return the updated DataSource.
     * @throws NotImplementedException not implemented
     */
    public DataSource createOrUpdateDataSource() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the updated DataSource.
     * @throws NotImplementedException not implemented
     */
    public Response<DataSource> createOrUpdateDataSourceWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Delete a DataSource
     *
     * @param dataSourceName the name of the datasource to be deleted
     */
    public void deleteDataSource(String dataSourceName) {
        asyncClient.deleteDataSource(dataSourceName).block();
    }

    /**
     * Deletes an Azure Search datasource.
     *
     * @param dataSourceName The name of the datasource to delete.
     * @param requestOptions Additional parameters for the operation.
     * @param accessCondition Additional parameters for the operation.
     */
    public void deleteDataSource(String dataSourceName,
                                 RequestOptions requestOptions,
                                 AccessCondition accessCondition) {
        asyncClient.deleteDataSource(
            dataSourceName,
            requestOptions,
            accessCondition).block();
    }

    /**
     * Delete a DataSource with Response
     *
     * @param dataSourceName the name of the datasource to be deleted
     * @return an empty response
     */
    public Response<Void> deleteDataSourceWithResponse(String dataSourceName) {
        return asyncClient.deleteDataSourceWithResponse(dataSourceName, null, null).block();
    }

    /**
     * @return the created Indexer.
     * @throws NotImplementedException not implemented
     */
    public Indexer createIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the created Indexer.
     * @throws NotImplementedException not implemented
     */
    public Response<Indexer> createIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the Indexer.
     * @throws NotImplementedException not implemented
     */
    public Indexer getIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Indexer.
     * @throws NotImplementedException not implemented
     */
    public Response<Indexer> getIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return all Indexers from the Search service.
     * @throws NotImplementedException not implemented
     */
    public IndexerListResult listIndexers() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing all Indexers from the Search service.
     * @throws NotImplementedException not implemented
     */
    public Response<IndexerListResult> listIndexersWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the updated Indexer.
     * @throws NotImplementedException not implemented
     */
    public Indexer createOrUpdateIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the updated Indexer.
     * @throws NotImplementedException not implemented
     */
    public Response<Indexer> createOrUpdateIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void deleteIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response signalling completion
     * @throws NotImplementedException not implemented
     */
    public Response<Response<Void>> deleteIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void resetIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response signalling completion
     * @throws NotImplementedException not implemented
     */
    public Response<Response<Void>> resetIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void runIndexer() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response signalling completion
     * @throws NotImplementedException not implemented
     */
    public Response<Response<Void>> runIndexerWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the Indexer execution information.
     * @throws NotImplementedException not implemented
     */
    public IndexerExecutionInfo deleteIndexerStatus() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Indexer execution information.
     * @throws NotImplementedException not implemented
     */
    public Response<IndexerExecutionInfo> deleteIndexerStatusWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Creates a new Azure Cognitive Search index
     *
     * @param index definition of the index to create
     * @return the created Index.
     */
    public Index createIndex(Index index) {
        return asyncClient.createIndex(index).block();
    }

    /**
     * Creates a new Azure Cognitive Search index
     *
     * @param index definition of the index to create
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the created Index.
     */
    public Index createIndex(Index index, RequestOptions requestOptions) {
        return asyncClient.createIndex(index, requestOptions).block();
    }

    /**
     * Creates a new Azure Cognitive Search index
     *
     * @param index definition of the index to create
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the created Index.
     */
    public Index createIndex(Index index, RequestOptions requestOptions, Context context) {
        return this.createIndexWithResponse(index, requestOptions, context).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index
     *
     * @param index definition of the index to create
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created Index.
     */
    public Response<Index> createIndexWithResponse(Index index,
                                                   RequestOptions requestOptions,
                                                   Context context) {
        return asyncClient.createIndexWithResponse(index, requestOptions, context).block();
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName the name of the index to retrieve
     * @return the Index.
     */
    public Index getIndex(String indexName) {
        return asyncClient.getIndex(indexName).block();
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName the name of the index to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the Index.
     */
    public Index getIndex(String indexName, RequestOptions requestOptions) {
        return asyncClient.getIndex(indexName, requestOptions).block();
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName the name of the index to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the Index.
     */
    public Index getIndex(String indexName, RequestOptions requestOptions, Context context) {
        return this.getIndexWithResponse(indexName, requestOptions, context).getValue();
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName the name of the index to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the Index.
     */
    public Response<Index> getIndexWithResponse(String indexName,
                                                RequestOptions requestOptions,
                                                Context context) {
        return asyncClient.getIndexWithResponse(indexName, requestOptions, context).block();
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     *
     * @param indexName the name of the index
     * @return true if the index exists; false otherwise.
     */
    public Boolean indexExists(String indexName) {
        return asyncClient.indexExists(indexName).block();
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     *
     * @param indexName the name of the index
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return true if the index exists; false otherwise.
     */
    public Boolean indexExists(String indexName, RequestOptions requestOptions) {
        return asyncClient.indexExists(indexName, requestOptions).block();
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     *
     * @param indexName the name of the index
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return true if the index exists; false otherwise.
     */
    public Boolean indexExists(String indexName, RequestOptions requestOptions, Context context) {
        return this.indexExistsWithResponse(indexName, requestOptions, context).getValue();
    }

    /**
     * Determines whether or not the given index exists in the Azure Cognitive Search.
     *
     * @param indexName the name of the index
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return true if the index exists; false otherwise.
     */
    public Response<Boolean> indexExistsWithResponse(String indexName,
                                                     RequestOptions requestOptions,
                                                     Context context) {
        return asyncClient.indexExistsWithResponse(indexName, requestOptions, context).block();
    }

    /**
     * @return the Index statistics.
     * @throws NotImplementedException not implemented
     */
    public IndexGetStatisticsResult getIndexStatistics() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Index statistics.
     * @throws NotImplementedException not implemented
     */
    public Response<IndexGetStatisticsResult> getIndexStatisticsWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @return the list of indexes.
     */
    public PagedIterable<Index> listIndexes() {
        return new PagedIterable<>(asyncClient.listIndexes());
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
     *               Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *               The default is all properties
     * @return the list of indexes.
     */
    public PagedIterable<Index> listIndexes(String select) {
        return new PagedIterable<>(asyncClient.listIndexes(select));
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
     *                       Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *                       The default is all properties
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the list of indexes.
     */
    public PagedIterable<Index> listIndexes(String select, RequestOptions requestOptions) {
        return new PagedIterable<>(asyncClient.listIndexes(select, requestOptions));
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
     *                       Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *                       The default is all properties
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of indexes.
     */
    public PagedIterable<Index> listIndexes(String select, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listIndexes(select, requestOptions, context));
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @return the index that was created or updated.
     */
    public Index createOrUpdateIndex(Index index) {
        return asyncClient.createOrUpdateIndex(index).block();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes
     * indexing and query requests to fail. Performance and write availability of the index
     * can be impaired for several minutes after the index is updated, or longer for very
     * large indexes.
     * @return the index that was created or updated.
     */
    public Index createOrUpdateIndex(Index index, boolean allowIndexDowntime) {
        return asyncClient.createOrUpdateIndex(index, allowIndexDowntime).block();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @return the index that was created or updated
     */
    public Index createOrUpdateIndex(Index index, AccessCondition accessCondition) {
        return asyncClient.createOrUpdateIndex(index, accessCondition).block();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes
     * indexing and query requests to fail. Performance and write availability of the index
     * can be impaired for several minutes after the index is updated, or longer for very
     * large indexes.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @return the index that was created or updated.
     */
    public Index createOrUpdateIndex(Index index, boolean allowIndexDowntime, AccessCondition accessCondition) {
        return asyncClient.createOrUpdateIndex(index, allowIndexDowntime, accessCondition).block();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes
     * indexing and query requests to fail. Performance and write availability of the index
     * can be impaired for several minutes after the index is updated, or longer for very
     * large indexes.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     *                           doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the index that was created or updated.
     */
    public Index createOrUpdateIndex(Index index,
                                     boolean allowIndexDowntime,
                                     AccessCondition accessCondition,
                                     RequestOptions requestOptions) {
        return asyncClient.createOrUpdateIndex(index,
            allowIndexDowntime,
            accessCondition,
            requestOptions).block();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes
     * indexing and query requests to fail. Performance and write availability of the index
     * can be impaired for several minutes after the index is updated, or longer for very
     * large indexes.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the index that was created or updated.
     */
    public Index createOrUpdateIndex(Index index,
                                     boolean allowIndexDowntime,
                                     AccessCondition accessCondition,
                                     RequestOptions requestOptions,
                                     Context context) {
        return this.createOrUpdateIndexWithResponse(index,
            allowIndexDowntime,
            accessCondition,
            requestOptions, context).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes
     * indexing and query requests to fail. Performance and write availability of the index
     * can be impaired for several minutes after the index is updated, or longer for very
     * large indexes.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     *                           doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the Index that was created or updated.
     */
    public Response<Index> createOrUpdateIndexWithResponse(Index index,
                                                           boolean allowIndexDowntime,
                                                           AccessCondition accessCondition,
                                                           RequestOptions requestOptions,
                                                           Context context) {
        return asyncClient.createOrUpdateIndexWithResponse(index,
            allowIndexDowntime,
            accessCondition,
            requestOptions,
            context).block();
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     */
    public void deleteIndex(String indexName) {
        asyncClient.deleteIndex(indexName).block();
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     */
    public void deleteIndex(String indexName, AccessCondition accessCondition) {
        asyncClient.deleteIndex(indexName, accessCondition).block();
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     *                        Contains the tracking ID sent with the request to help with debugging
     */
    public void deleteIndex(String indexName, AccessCondition accessCondition, RequestOptions requestOptions) {
        asyncClient.deleteIndex(indexName, accessCondition, requestOptions).block();
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * Contains the tracking ID sent with the request to help with debugging
     */
    public void deleteIndex(String indexName,
                            AccessCondition accessCondition,
                            RequestOptions requestOptions,
                            Context context) {
        this.deleteIndexWithResponse(indexName, accessCondition, requestOptions, context);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     *                        Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteIndexWithResponse(String indexName,
                                                  AccessCondition accessCondition,
                                                  RequestOptions requestOptions,
                                                  Context context) {
        return asyncClient.deleteIndexWithResponse(indexName,
            accessCondition,
            requestOptions,
            context).block();
    }

    /**
     * @return the Index analysis results.
     * @throws NotImplementedException not implemented
     */
    public Response<AnalyzeResult> analyzeIndex() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Index analysis results.
     * @throws NotImplementedException not implemented
     */
    public Response<AnalyzeResult> analyzeIndexWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the created Skillset.
     * @throws NotImplementedException not implemented
     */
    public Skillset createSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the created Skillset.
     * @throws NotImplementedException not implemented
     */
    public Response<Skillset> createSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the Skillset.
     * @throws NotImplementedException not implemented
     */
    public Skillset getSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Skillset.
     * @throws NotImplementedException not implemented
     */
    public Response<Skillset> getSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return all Skillsets in the Search service.
     * @throws NotImplementedException not implemented
     */
    public SkillsetListResult listSkillsets() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing all Skillsets in the Search service.
     * @throws NotImplementedException not implemented
     */
    public Response<SkillsetListResult> listSkillsetsWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return the updated Skillset.
     * @throws NotImplementedException not implemented
     */
    public Skillset createOrUpdateSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the updated Skillset.
     * @throws NotImplementedException not implemented
     */
    public Response<Skillset> createOrUpdateSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void deleteSkillset() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response signalling completion
     * @throws NotImplementedException not implemented
     */
    public Response<Response<Void>> deleteSkillsetWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @return the created {@link SynonymMap}.
     */
    public SynonymMap createSynonymMap(SynonymMap synonymMap) {
        return asyncClient.createSynonymMap(synonymMap).block();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the created {@link SynonymMap}.
     */
    public SynonymMap createSynonymMap(SynonymMap synonymMap, RequestOptions requestOptions) {
        return asyncClient.createSynonymMap(synonymMap, requestOptions).block();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the created {@link SynonymMap}.
     */
    public SynonymMap createSynonymMap(SynonymMap synonymMap, RequestOptions requestOptions, Context context) {
        return this.createSynonymMapWithResponse(synonymMap, requestOptions, context).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created SynonymMap.
     */
    public Response<SynonymMap> createSynonymMapWithResponse(SynonymMap synonymMap,
                                                             RequestOptions requestOptions,
                                                             Context context) {
        return asyncClient.createSynonymMapWithResponse(synonymMap,
            requestOptions,
            context).block();
    }

    /** Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @return the {@link SynonymMap} definition
     */
    public SynonymMap getSynonymMap(String synonymMapName) {
        return asyncClient.getSynonymMap(synonymMapName).block();
    }

    /** Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the {@link SynonymMap} definition.
     */
    public SynonymMap getSynonymMap(String synonymMapName, RequestOptions requestOptions) {
        return asyncClient.getSynonymMap(synonymMapName, requestOptions).block();
    }

    /** Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context a context that is passed through the HTTP pipeline during the service call
     * @return the {@link SynonymMap} definition.
     */
    public SynonymMap getSynonymMap(String synonymMapName,
                                    RequestOptions requestOptions,
                                    Context context) {
        return this.getSynonymMapWithResponse(synonymMapName, requestOptions, context).getValue();
    }

    /** Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context a context that is passed through the HTTP pipeline during the service call
     * @return a response containing the SynonymMap.
     */
    public Response<SynonymMap> getSynonymMapWithResponse(String synonymMapName,
                                                          RequestOptions requestOptions,
                                                          Context context) {
        return asyncClient.getSynonymMapWithResponse(synonymMapName, requestOptions, context).block();
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @return the list of synonym maps.
     */
    public PagedIterable<SynonymMap> listSynonymMaps() {
        return new PagedIterable<>(asyncClient.listSynonymMaps());
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties
     * @return the list of synonym maps.
     */
    public PagedIterable<SynonymMap> listSynonymMaps(String select) {
        return new PagedIterable<>(asyncClient.listSynonymMaps(select));
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the list of synonym maps.
     */
    public PagedIterable<SynonymMap> listSynonymMaps(String select, RequestOptions requestOptions) {
        return new PagedIterable<>(asyncClient.listSynonymMaps(select, requestOptions));
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of synonym maps.
     */
    public PagedIterable<SynonymMap> listSynonymMaps(String select, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listSynonymMaps(select, requestOptions, context));
    }

    /**
     * @return the updated SynonymMap.
     * @throws NotImplementedException not implemented
     */
    public SynonymMap createOrUpdateSynonymMap() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the updated SynonymMap.
     * @throws NotImplementedException not implemented
     */
    public Response<SynonymMap> createOrUpdateSynonymMapWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @throws NotImplementedException not implemented
     */
    public void deleteSynonymMap() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response signalling completion
     * @throws NotImplementedException not implemented
     */
    public Response<Response<Void>> deleteSynonymMapWithResponse() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * Determines whether or not the given synonym map exists.
     *
     * @param synonymMapName the name of the synonym map
     * @return true if the synonym map exists; false otherwise.
     */
    public Boolean synonymMapExists(String synonymMapName) {
        return asyncClient.synonymMapExists(synonymMapName).block();
    }

    /**
     * Determines whether or not the given synonym map exists.
     *
     * @param synonymMapName the name of the synonym map
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return true if the synonym map exists; false otherwise.
     */
    public Boolean synonymMapExists(String synonymMapName, RequestOptions requestOptions) {
        return asyncClient.synonymMapExists(synonymMapName, requestOptions).block();
    }

    /**
     * Determines whether or not the given synonym map exists.
     *
     * @param synonymMapName the name of the synonym map
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return true if the index exists; false otherwise.
     */
    public Boolean synonymMapExists(String synonymMapName, RequestOptions requestOptions, Context context) {
        return this.synonymMapExistsWithResponse(synonymMapName, requestOptions, context)
            .getValue();
    }

    /**
     * Determines whether or not the given synonym map exists.
     *
     * @param synonymMapName the name of the synonym map
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return true if the index exists; false otherwise.
     */
    public Response<Boolean> synonymMapExistsWithResponse(String synonymMapName,
                                                          RequestOptions requestOptions,
                                                          Context context) {
        return asyncClient
            .synonymMapExistsWithResponse(synonymMapName, requestOptions, context)
            .block();
    }
}
