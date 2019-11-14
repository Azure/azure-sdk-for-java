// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzeRequest;
import com.azure.search.models.DataSource;
import com.azure.search.models.Index;
import com.azure.search.models.IndexGetStatisticsResult;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.Skillset;
import com.azure.search.models.SynonymMap;

import com.azure.search.models.TokenInfo;
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
     * Creates a new Azure Cognitive Search datasource or updates a datasource if it already exists
     *
     * @param dataSource The definition of the datasource to create or update.
     * @return the datasource that was created or updated.
     */
    public DataSource createOrUpdateDataSource(DataSource dataSource) {
        return asyncClient.createOrUpdateDataSource(dataSource).block();
    }

    /**
     * Creates a new Azure Cognitive Search datasource or updates a datasource if it already exists.
     *
     * @param dataSource the definition of the datasource to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the datasource that was created or updated.
     */
    public DataSource createOrUpdateDataSource(DataSource dataSource,
                                               AccessCondition accessCondition,
                                               RequestOptions requestOptions) {
        return asyncClient.createOrUpdateDataSource(dataSource, accessCondition, requestOptions).block();
    }

    /**
     * Creates a new Azure Cognitive Search datasource or updates a datasource if it already exists.
     *
     * @param dataSource the definition of the datasource to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing datasource that was created or updated.
     */
    public Response<DataSource> createOrUpdateDataSourceWithResponse(DataSource dataSource,
                                                                     AccessCondition accessCondition,
                                                                     RequestOptions requestOptions,
                                                                     Context context) {
        return asyncClient.createOrUpdateDataSourceWithResponse(dataSource,
            accessCondition, requestOptions, context).block();
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @return the DataSource.
     */
    public DataSource getDataSource(String dataSourceName) {
        return asyncClient.getDataSource(dataSourceName).block();
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging.
     * @return the DataSource.
     */
    public DataSource getDataSource(String dataSourceName, RequestOptions requestOptions) {
        return asyncClient.getDataSource(dataSourceName, requestOptions).block();
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging.
     * @param context Context
     * @return a response containing the DataSource.
     */
    public Response<DataSource> getDataSourceWithResponse(String dataSourceName,
                                                          RequestOptions requestOptions,
                                                          Context context) {
        return asyncClient.getDataSourceWithResponse(dataSourceName, requestOptions, context).block();
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
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging.
     * @return a list of DataSources
     */
    public PagedIterable<DataSource> listDataSources(String select, RequestOptions requestOptions) {
        return new PagedIterable<>(asyncClient.listDataSources(select, requestOptions));
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of DataSource definitions to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a response containing the list of DataSources.
     */
    public PagedResponse<DataSource> listDataSourcesWithResponse(String select,
                                                                 RequestOptions requestOptions, Context context) {
        return asyncClient.listDataSourcesWithResponse(select, requestOptions, context).block();
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
     * @param dataSourceName The name of the datasource to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     */
    public void deleteDataSource(String dataSourceName,
                                 AccessCondition accessCondition,
                                 RequestOptions requestOptions) {
        asyncClient.deleteDataSource(dataSourceName, accessCondition, requestOptions).block();
    }

    /**
     * Delete a DataSource with Response
     *
     * @param dataSourceName the name of the datasource to be deleted
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return an empty response
     */
    public Response<Void> deleteDataSourceWithResponse(String dataSourceName,
                                                       AccessCondition accessCondition,
                                                       RequestOptions requestOptions,
                                                       Context context) {
        return asyncClient.deleteDataSourceWithResponse(dataSourceName,
            accessCondition, requestOptions, context).block();
    }

    /**
     * Determines whether or not the given datasource exists.
     *
     * @param datasourceName the name of the datasource
     * @return true if the datasource exists; false otherwise.
     */
    public Boolean datasourceExists(String datasourceName) {
        return asyncClient.datasourceExists(datasourceName).block();
    }

    /**
     * Determines whether or not the given datasource exists.
     *
     * @param datasourceName the name of the datasource
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return true if the datasource exists; false otherwise.
     */
    public Boolean datasourceExists(String datasourceName, RequestOptions requestOptions) {
        return asyncClient.datasourceExists(datasourceName, requestOptions).block();
    }

    /**
     * Determines whether or not the given datasource exists.
     *
     * @param datasourceName the name of the datasource
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return true if the datasource exists; false otherwise.
     */
    public Response<Boolean> datasourceExistsWithResponse(String datasourceName,
        RequestOptions requestOptions,
        Context context) {
        return asyncClient
            .datasourceExistsWithResponse(datasourceName, requestOptions, context)
            .block();
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
     * Creates a new Azure Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The definition of the indexer to create or update.
     * @return a response containing the created Indexer.
     */
    public Indexer createOrUpdateIndexer(Indexer indexer) {
        return asyncClient.createOrUpdateIndexer(indexer).block();
    }

    /**
     * Creates a new Azure Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The definition of the indexer to create or update.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the created Indexer.
     */
    public Indexer createOrUpdateIndexer(Indexer indexer,
                                         AccessCondition accessCondition,
                                         RequestOptions requestOptions) {
        return asyncClient.createOrUpdateIndexer(indexer, accessCondition, requestOptions).block();
    }

    /**
     * Creates a new Azure Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The definition of the indexer to create or update.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context Context
     * @return A response object containing the Indexer.
     */
    public Response<Indexer> createOrUpdateIndexerWithResponse(Indexer indexer,
                                                               AccessCondition accessCondition,
                                                               RequestOptions requestOptions,
                                                               Context context) {
        return asyncClient.createOrUpdateIndexerWithResponse(indexer, accessCondition, requestOptions, context).block();
    }

    /**
     * @return all Indexers from the Search service.
     */
    public PagedIterable<Indexer> listIndexers() {
        return new PagedIterable<>(asyncClient.listIndexers());
    }

    /**
     * Lists all indexers available for an Azure Search service.
     *
     * @param select Selects which top-level properties of the indexers to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     * @return all Indexers from the Search service.
     */
    public PagedIterable<Indexer> listIndexers(String select, RequestOptions requestOptions) {
        return new PagedIterable<>(asyncClient.listIndexers(select, requestOptions));
    }

    /**
     * Lists all indexers available for an Azure Search service.
     *
     * @param select Selects which top-level properties of the indexers to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     * @param context The context to associate with this operation.
     * @return a response containing all Indexers from the Search service.
     */
    public PagedResponse<Indexer> listIndexersWithResponse(String select,
                                                           RequestOptions requestOptions,
                                                           Context context) {
        return asyncClient.listIndexersWithResponse(select, requestOptions, context).block();
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
    public IndexerExecutionInfo getIndexerStatus() {
        throw logger.logExceptionAsError(new NotImplementedException("not implemented."));
    }

    /**
     * @return a response containing the Indexer execution information.
     * @throws NotImplementedException not implemented
     */
    public Response<IndexerExecutionInfo> getIndexerStatusWithResponse() {
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
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties
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
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a response emitting the list of indexes.
     */
    public PagedResponse<Index> listIndexesWithResponse(String select, RequestOptions requestOptions, Context context) {
        return asyncClient.listIndexesWithResponse(select, requestOptions, context).block();
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
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
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
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
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
     * Shows how an analyzer breaks text into tokens.
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeRequest the text and analyzer or analysis components to test
     * @return analyze result.
     */
    public PagedIterable<TokenInfo> analyzeIndex(String indexName,
                                                 AnalyzeRequest analyzeRequest) {
        return new PagedIterable<>(asyncClient.analyzeIndex(indexName, analyzeRequest));
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeRequest the text and analyzer or analysis components to test
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return analyze result.
     */
    public PagedIterable<TokenInfo> analyzeIndex(String indexName,
                                                 AnalyzeRequest analyzeRequest,
                                                 RequestOptions requestOptions) {
        return new PagedIterable<>(asyncClient.analyzeIndex(indexName, analyzeRequest, requestOptions));
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeRequest the text and analyzer or analysis components to test
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing analyze result.
     */
    public PagedResponse<TokenInfo> analyzeIndexWithResponse(String indexName,
                                                             AnalyzeRequest analyzeRequest,
                                                             RequestOptions requestOptions,
                                                             Context context) {
        return asyncClient.analyzeIndexWithResponse(indexName, analyzeRequest, requestOptions, context).block();
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     *
     * @return the created Skillset.
     */
    public Skillset createSkillset(Skillset skillset) {
        return asyncClient.createSkillset(skillset).block();
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the created Skillset.
     */
    public Skillset createSkillset(Skillset skillset, RequestOptions requestOptions) {
        return asyncClient.createSkillset(skillset, requestOptions).block();
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created Skillset.
     */
    public Response<Skillset> createSkillsetWithResponse(Skillset skillset,
                                                         RequestOptions requestOptions,
                                                         Context context) {
        return asyncClient.createSkillsetWithResponse(skillset, requestOptions, context).block();
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @return the Skillset.
     */
    public Skillset getSkillset(String skillsetName) {
        return asyncClient.getSkillset(skillsetName).block();
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the Skillset.
     */
    public Skillset getSkillset(String skillsetName, RequestOptions requestOptions) {
        return asyncClient.getSkillset(skillsetName, requestOptions).block();
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the Skillset.
     */
    public Response<Skillset> getSkillsetWithResponse(String skillsetName,
                                                      RequestOptions requestOptions,
                                                      Context context) {
        return asyncClient.getSkillsetWithResponse(skillsetName, requestOptions, context).block();
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @return the list of skillsets.
     */
    public PagedIterable<Skillset> listSkillsets() {
        return new PagedIterable<>(asyncClient.listSkillsets());
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the skillset definitions to retrieve.
     *               Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *               The default is all properties
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the list of skillsets.
     */
    public PagedIterable<Skillset> listSkillsets(String select, RequestOptions requestOptions) {
        return new PagedIterable<>(asyncClient.listSkillsets(select, requestOptions));
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the skillset definitions to retrieve.
     *               Specified as a comma-separated list of JSON property names, or '*' for all properties.
     *               The default is all properties
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a response emitting the list of skillsets.
     */
    public PagedResponse<Skillset> listSkillsetsWithResponse(String select,
                                                             RequestOptions requestOptions,
                                                             Context context) {
        return asyncClient.listSkillsetsWithResponse(select, requestOptions, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the definition of the skillset to create or update
     * @return the skillset that was created or updated.
     */
    public Skillset createOrUpdateSkillset(Skillset skillset) {
        return asyncClient.createOrUpdateSkillset(skillset).block();
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the definition of the skillset to create or update
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the skillset that was created or updated.
     */
    public Skillset createOrUpdateSkillset(Skillset skillset, RequestOptions requestOptions) {
        return asyncClient.createOrUpdateSkillset(skillset, requestOptions).block();
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the definition of the skillset to create or update
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the skillset that was created or updated.
     */
    public Response<Skillset> createOrUpdateSkillsetWithResponse(Skillset skillset,
                                                                 RequestOptions requestOptions,
                                                                 Context context) {
        return asyncClient.createOrUpdateSkillsetWithResponse(skillset,
            requestOptions,
            context).block();
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillsetName the name of the skillset to delete
     */
    public void deleteSkillset(String skillsetName) {
        asyncClient.deleteSkillset(skillsetName).block();
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillsetName the name of the skillset to delete
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     */
    public void deleteSkillset(String skillsetName, RequestOptions requestOptions) {
        asyncClient.deleteSkillsetWithResponse(skillsetName, requestOptions).block();
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillsetName the name of the skillset to delete
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteSkillsetWithResponse(String skillsetName,
                                                     RequestOptions requestOptions,
                                                     Context context) {
        return asyncClient.deleteSkillsetWithResponse(skillsetName, requestOptions, context).block();
    }

    /**
     * Determines whether or not the given skillset exists.
     *
     * @param skillsetName the name of the skillset
     * @return true if the skillset exists; false otherwise.
     */
    public Boolean skillsetExists(String skillsetName) {
        return asyncClient.skillsetExists(skillsetName).block();
    }

    /**
     * Determines whether or not the given skillset exists.
     *
     * @param skillsetName the name of the skillset
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return true if the skillset exists; false otherwise.
     */
    public Boolean skillsetExists(String skillsetName, RequestOptions requestOptions) {
        return asyncClient.skillsetExists(skillsetName, requestOptions).block();
    }

    /**
     * Determines whether or not the given skillset exists.
     *
     * @param skillsetName the name of the skillset
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return true if the skillset exists; false otherwise.
     */
    public Response<Boolean> skillsetExistsWithResponse(String skillsetName,
                                                        RequestOptions requestOptions,
                                                        Context context) {
        return asyncClient
            .skillsetExistsWithResponse(skillsetName, requestOptions, context)
            .block();
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
     * @return a response containing the created SynonymMap.
     */
    public Response<SynonymMap> createSynonymMapWithResponse(SynonymMap synonymMap,
                                                             RequestOptions requestOptions,
                                                             Context context) {
        return asyncClient.createSynonymMapWithResponse(synonymMap, requestOptions, context).block();
    }

    /**
     * Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @return the {@link SynonymMap} definition
     */
    public SynonymMap getSynonymMap(String synonymMapName) {
        return asyncClient.getSynonymMap(synonymMapName).block();
    }

    /**
     * Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the {@link SynonymMap} definition.
     */
    public SynonymMap getSynonymMap(String synonymMapName, RequestOptions requestOptions) {
        return asyncClient.getSynonymMap(synonymMapName, requestOptions).block();
    }

    /**
     * Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
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
     * @return a response containing the list of synonym maps.
     */
    public PagedResponse<SynonymMap> listSynonymMapsWithResponse(String select,
                                                                 RequestOptions requestOptions, Context context) {
        return asyncClient.listSynonymMapsWithResponse(select, requestOptions, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @return the synonym map that was created or updated.
     */
    public SynonymMap createOrUpdateSynonymMap(SynonymMap synonymMap) {
        return asyncClient.createOrUpdateSynonymMap(synonymMap).block();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the synonym map that was created or updated.
     */
    public SynonymMap createOrUpdateSynonymMap(SynonymMap synonymMap,
                                               AccessCondition accessCondition,
                                               RequestOptions requestOptions) {
        return asyncClient.createOrUpdateSynonymMap(synonymMap, accessCondition, requestOptions).block();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the synonym map that was created or updated.
     */
    public Response<SynonymMap> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
                                                                     AccessCondition accessCondition,
                                                                     RequestOptions requestOptions,
                                                                     Context context) {
        return asyncClient.createOrUpdateSynonymMapWithResponse(synonymMap,
            accessCondition, requestOptions, context).block();
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMapName the name of the synonym map to delete
     */
    public void deleteSynonymMap(String synonymMapName) {
        asyncClient.deleteSynonymMap(synonymMapName).block();
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMapName the name of the synonym map to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     */
    public void deleteSynonymMap(String synonymMapName,
                                 AccessCondition accessCondition,
                                 RequestOptions requestOptions) {
        asyncClient.deleteSynonymMap(synonymMapName, accessCondition, requestOptions).block();
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMapName the name of the synonym map to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteSynonymMapWithResponse(String synonymMapName,
                                                       AccessCondition accessCondition,
                                                       RequestOptions requestOptions,
                                                       Context context) {
        return asyncClient.deleteSynonymMapWithResponse(synonymMapName,
            accessCondition, requestOptions, context).block();
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
     * Contains the tracking ID sent with the request to help with debugging
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
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return true if the synonym map exists; false otherwise.
     */
    public Response<Boolean> synonymMapExistsWithResponse(String synonymMapName,
                                                          RequestOptions requestOptions,
                                                          Context context) {
        return asyncClient
            .synonymMapExistsWithResponse(synonymMapName, requestOptions, context).block();
    }
}
