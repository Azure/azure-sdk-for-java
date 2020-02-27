// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.AnalyzeRequest;
import com.azure.search.models.DataSource;
import com.azure.search.models.GetIndexStatisticsResult;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.ServiceStatistics;
import com.azure.search.models.Skillset;
import com.azure.search.models.SynonymMap;
import com.azure.search.models.TokenInfo;

/**
 * Synchronous Client to manage and query indexes, as well as manage other resources, on a Cognitive Search service
 */
@ServiceClient(builder = SearchServiceClientBuilder.class)
public final class SearchServiceClient {
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
     * Initializes a new {@link SearchIndexClient} using the given Index name and the same configuration as the
     * SearchServiceClient.
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
    public SearchServiceVersion getApiVersion() {
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
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists
     *
     * @param dataSource The definition of the data source to create or update.
     * @return the data source that was created or updated.
     */
    public DataSource createOrUpdateDataSource(DataSource dataSource) {
        return createOrUpdateDataSourceWithResponse(dataSource, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * @param dataSource the definition of the data source to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing data source that was created or updated.
     */
    public Response<DataSource> createOrUpdateDataSourceWithResponse(DataSource dataSource,
        AccessCondition accessCondition, RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateDataSourceWithResponse(dataSource, accessCondition, requestOptions, context)
            .block();
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * @param dataSource The definition of the data source to create
     * @return the data source that was created.
     */
    public DataSource createDataSource(DataSource dataSource) {
        return createDataSourceWithResponse(dataSource, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * @param dataSource the definition of the data source to create doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing data source that was created.
     */
    public Response<DataSource> createDataSourceWithResponse(DataSource dataSource, RequestOptions requestOptions,
        Context context) {
        return asyncClient.createDataSourceWithResponse(dataSource, requestOptions, context).block();
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @return the DataSource.
     */
    public DataSource getDataSource(String dataSourceName) {
        return getDataSourceWithResponse(dataSourceName, null, Context.NONE).getValue();
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the DataSource.
     */
    public Response<DataSource> getDataSourceWithResponse(String dataSourceName, RequestOptions requestOptions,
        Context context) {
        return asyncClient.getDataSourceWithResponse(dataSourceName, requestOptions, context).block();
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @return a list of DataSources
     */
    public PagedIterable<DataSource> listDataSources() {
        return listDataSources(null, null, Context.NONE);
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of DataSource definitions to retrieve. Specified as a
     * comma-separated list of JSON property names, or '*' for all properties. The default is all properties.
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a response containing the list of DataSources.
     */
    public PagedIterable<DataSource> listDataSources(String select, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listDataSources(select, requestOptions, context));
    }

    /**
     * Delete a DataSource
     *
     * @param dataSourceName the name of the data source to be deleted
     */
    public void deleteDataSource(String dataSourceName) {
        deleteDataSourceWithResponse(dataSourceName, null, null, Context.NONE);
    }

    /**
     * Delete a DataSource with Response
     *
     * @param dataSourceName the name of the data source to be deleted
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return an empty response
     */
    public Response<Void> deleteDataSourceWithResponse(String dataSourceName, AccessCondition accessCondition,
        RequestOptions requestOptions, Context context) {
        return asyncClient.deleteDataSourceWithResponse(dataSourceName, accessCondition, requestOptions, context)
            .block();
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * @param indexer definition of the indexer to create.
     * @return the created Indexer.
     */
    public Indexer createIndexer(Indexer indexer) {
        return createIndexerWithResponse(indexer, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * @param indexer definition of the indexer to create
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created Indexer.
     */
    public Response<Indexer> createIndexerWithResponse(Indexer indexer, RequestOptions requestOptions,
        Context context) {
        return asyncClient.createIndexerWithResponse(indexer, requestOptions, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The definition of the indexer to create or update.
     * @return a response containing the created Indexer.
     */
    public Indexer createOrUpdateIndexer(Indexer indexer) {
        return createOrUpdateIndexerWithResponse(indexer, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The definition of the indexer to create or update.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return A response object containing the Indexer.
     */
    public Response<Indexer> createOrUpdateIndexerWithResponse(Indexer indexer, AccessCondition accessCondition,
        RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateIndexerWithResponse(indexer, accessCondition, requestOptions, context).block();
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * @return all Indexers from the Search service.
     */
    public PagedIterable<Indexer> listIndexers() {
        return listIndexers(null, null, Context.NONE);
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of the indexers to retrieve. Specified as a comma-separated list
     * of JSON property names, or '*' for all properties. The default is all properties.
     * @param requestOptions Additional parameters for the operation.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return all Indexers from the Search service.
     */
    public PagedIterable<Indexer> listIndexers(String select, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listIndexers(select, requestOptions, context));
    }

    /**
     * Retrieves an indexer definition.
     *
     * @param indexerName the name of the indexer to retrieve
     * @return the indexer.
     */
    public Indexer getIndexer(String indexerName) {
        return getIndexerWithResponse(indexerName, null, Context.NONE).getValue();
    }

    /**
     * Retrieves an indexer definition.
     *
     * @param indexerName the name of the indexer to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the indexer.
     */
    public Response<Indexer> getIndexerWithResponse(String indexerName, RequestOptions requestOptions,
        Context context) {
        return asyncClient.getIndexerWithResponse(indexerName, requestOptions, context).block();
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     */
    public void deleteIndexer(String indexerName) {
        deleteIndexerWithResponse(indexerName, null, null, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context the context
     * @return a response signalling completion.
     */
    public Response<Void> deleteIndexerWithResponse(String indexerName, AccessCondition accessCondition,
        RequestOptions requestOptions, Context context) {
        return asyncClient.deleteIndexerWithResponse(indexerName, accessCondition, requestOptions, context).block();
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * @param indexerName the name of the indexer to reset
     */
    public void resetIndexer(String indexerName) {
        resetIndexerWithResponse(indexerName, null, Context.NONE);
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * @param indexerName the name of the indexer to reset
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> resetIndexerWithResponse(String indexerName, RequestOptions requestOptions, Context context) {
        return asyncClient.resetIndexerWithResponse(indexerName, requestOptions, context).block();
    }

    /**
     * Runs an indexer on-demand.
     *
     * @param indexerName the name of the indexer to run
     */
    public void runIndexer(String indexerName) {
        runIndexerWithResponse(indexerName, null, Context.NONE);
    }

    /**
     * Runs an indexer on-demand.
     *
     * @param indexerName the name of the indexer to run
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> runIndexerWithResponse(String indexerName, RequestOptions requestOptions, Context context) {
        return asyncClient.runIndexerWithResponse(indexerName, requestOptions, context).block();
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @return a response with the indexer execution info.
     */
    public IndexerExecutionInfo getIndexerStatus(String indexerName) {
        return getIndexerStatusWithResponse(indexerName, null, Context.NONE).getValue();
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response with the indexer execution info.
     */
    public Response<IndexerExecutionInfo> getIndexerStatusWithResponse(String indexerName,
        RequestOptions requestOptions, Context context) {
        return asyncClient.getIndexerStatusWithResponse(indexerName, requestOptions, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search index
     *
     * @param index definition of the index to create
     * @return the created Index.
     */
    public Index createIndex(Index index) {
        return createIndexWithResponse(index, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index
     *
     * @param index definition of the index to create
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created Index.
     */
    public Response<Index> createIndexWithResponse(Index index, RequestOptions requestOptions, Context context) {
        return asyncClient.createIndexWithResponse(index, requestOptions, context).block();
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName the name of the index to retrieve
     * @return the Index.
     */
    public Index getIndex(String indexName) {
        return getIndexWithResponse(indexName, null, Context.NONE).getValue();
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName the name of the index to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the Index.
     */
    public Response<Index> getIndexWithResponse(String indexName, RequestOptions requestOptions, Context context) {
        return asyncClient.getIndexWithResponse(indexName, requestOptions, context).block();
    }

    /**
     * Returns statistics for the given index, including a document count and storage usage.
     *
     * @param indexName the name of the index for which to retrieve statistics
     * @return the index statistics result.
     */
    public GetIndexStatisticsResult getIndexStatistics(String indexName) {
        return getIndexStatisticsWithResponse(indexName, null, Context.NONE).getValue();
    }

    /**
     * Returns statistics for the given index, including a document count and storage usage.
     *
     * @param indexName the name of the index for which to retrieve statistics
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the index statistics result.
     */
    public Response<GetIndexStatisticsResult> getIndexStatisticsWithResponse(String indexName,
        RequestOptions requestOptions, Context context) {
        return asyncClient.getIndexStatisticsWithResponse(indexName, requestOptions, context).block();
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @return the list of indexes.
     */
    public PagedIterable<Index> listIndexes() {
        return listIndexes(null, null, Context.NONE);
    }

    /**
     * Lists all indexes available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve. Specified as a
     * comma-separated list of JSON property names, or '*' for all properties. The default is all properties
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
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
        return createOrUpdateIndexWithResponse(index, false, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes indexing and query requests
     * to fail. Performance and write availability of the index can be impaired for several minutes after the index is
     * updated, or longer for very large indexes.
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the Index that was created or updated.
     */
    public Response<Index> createOrUpdateIndexWithResponse(Index index, boolean allowIndexDowntime,
        AccessCondition accessCondition, RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateIndexWithResponse(index, allowIndexDowntime, accessCondition, requestOptions,
            context).block();
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     */
    public void deleteIndex(String indexName) {
        deleteIndexWithResponse(indexName, null, null, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteIndexWithResponse(String indexName, AccessCondition accessCondition,
        RequestOptions requestOptions, Context context) {
        return asyncClient.deleteIndexWithResponse(indexName, accessCondition, requestOptions, context).block();
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeRequest the text and analyzer or analysis components to test
     * @return analyze result.
     */
    public PagedIterable<TokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest) {
        return analyzeText(indexName, analyzeRequest, null, Context.NONE);
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeRequest the text and analyzer or analysis components to test
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return analyze result.
     */
    public PagedIterable<TokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest,
        RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.analyzeText(indexName, analyzeRequest, requestOptions, context));
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @return the created Skillset.
     */
    public Skillset createSkillset(Skillset skillset) {
        return createSkillsetWithResponse(skillset, null, Context.NONE).getValue();
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created Skillset.
     */
    public Response<Skillset> createSkillsetWithResponse(Skillset skillset, RequestOptions requestOptions,
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
        return getSkillsetWithResponse(skillsetName, null, Context.NONE).getValue();
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the Skillset.
     */
    public Response<Skillset> getSkillsetWithResponse(String skillsetName, RequestOptions requestOptions,
        Context context) {
        return asyncClient.getSkillsetWithResponse(skillsetName, requestOptions, context).block();
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @return the list of skillsets.
     */
    public PagedIterable<Skillset> listSkillsets() {
        return listSkillsets(null, null, Context.NONE);
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the skillset definitions to retrieve. Specified as a
     * comma-separated list of JSON property names, or '*' for all properties. The default is all properties
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of skillsets.
     */
    public PagedIterable<Skillset> listSkillsets(String select, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listSkillsets(select, requestOptions, context));
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the definition of the skillset to create or update
     * @return the skillset that was created or updated.
     */
    public Skillset createOrUpdateSkillset(Skillset skillset) {
        return createOrUpdateSkillsetWithResponse(skillset, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the definition of the skillset to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the skillset that was created or updated.
     */
    public Response<Skillset> createOrUpdateSkillsetWithResponse(Skillset skillset, AccessCondition accessCondition,
        RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateSkillsetWithResponse(skillset, accessCondition, requestOptions, context)
            .block();
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillsetName the name of the skillset to delete
     */
    public void deleteSkillset(String skillsetName) {
        deleteSkillsetWithResponse(skillsetName, null, null, Context.NONE);
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillsetName the name of the skillset to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteSkillsetWithResponse(String skillsetName, AccessCondition accessCondition,
        RequestOptions requestOptions, Context context) {
        return asyncClient.deleteSkillsetWithResponse(skillsetName, accessCondition, requestOptions, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @return the created {@link SynonymMap}.
     */
    public SynonymMap createSynonymMap(SynonymMap synonymMap) {
        return createSynonymMapWithResponse(synonymMap, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map.
     *
     * @param synonymMap the definition of the synonym map to create
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created SynonymMap.
     */
    public Response<SynonymMap> createSynonymMapWithResponse(SynonymMap synonymMap, RequestOptions requestOptions,
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
        return getSynonymMapWithResponse(synonymMapName, null, Context.NONE).getValue();
    }

    /**
     * Retrieves a synonym map definition.
     *
     * @param synonymMapName name of the synonym map to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context a context that is passed through the HTTP pipeline during the service call
     * @return a response containing the SynonymMap.
     */
    public Response<SynonymMap> getSynonymMapWithResponse(String synonymMapName, RequestOptions requestOptions,
        Context context) {
        return asyncClient.getSynonymMapWithResponse(synonymMapName, requestOptions, context).block();
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @return the list of synonym maps.
     */
    public PagedIterable<SynonymMap> listSynonymMaps() {
        return listSynonymMaps(null, null, Context.NONE);
    }

    /**
     * Lists all synonym maps available for an Azure Cognitive Search service.
     *
     * @param select selects which top-level properties of the index definitions to retrieve. Specified as a
     * comma-separated list of JSON property names, or '*' for all properties. The default is all properties
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the list of synonym maps.
     */
    public PagedIterable<SynonymMap> listSynonymMaps(String select, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listSynonymMaps(select, requestOptions, context));
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @return the synonym map that was created or updated.
     */
    public SynonymMap createOrUpdateSynonymMap(SynonymMap synonymMap) {
        return createOrUpdateSynonymMapWithResponse(synonymMap, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the synonym map that was created or updated.
     */
    public Response<SynonymMap> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
        AccessCondition accessCondition, RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateSynonymMapWithResponse(synonymMap, accessCondition, requestOptions, context)
            .block();
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMapName the name of the synonym map to delete
     */
    public void deleteSynonymMap(String synonymMapName) {
        deleteSynonymMapWithResponse(synonymMapName, null, null, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMapName the name of the synonym map to delete
     * @param accessCondition the condition where the operation will be performed if the ETag on the server matches or
     * doesn't match specified values
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteSynonymMapWithResponse(String synonymMapName, AccessCondition accessCondition,
        RequestOptions requestOptions, Context context) {
        return asyncClient.deleteSynonymMapWithResponse(synonymMapName, accessCondition, requestOptions, context)
            .block();
    }

    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * @return the search service statistics result.
     */
    public ServiceStatistics getServiceStatistics() {
        return getServiceStatisticsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Returns service level statistics for a search service, including service counters and limits.
     *
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return the search service statistics result.
     */
    public Response<ServiceStatistics> getServiceStatisticsWithResponse(RequestOptions requestOptions,
        Context context) {
        return asyncClient.getServiceStatisticsWithResponse(requestOptions, context).block();
    }
}
