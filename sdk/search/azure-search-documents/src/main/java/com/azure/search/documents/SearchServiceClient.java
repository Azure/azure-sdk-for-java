// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.models.AnalyzeRequest;
import com.azure.search.documents.indexes.models.AnalyzedTokenInfo;
import com.azure.search.documents.indexes.models.GetIndexStatisticsResult;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSource;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;
import com.azure.search.documents.indexes.models.ServiceStatistics;
import com.azure.search.documents.indexes.models.SynonymMap;

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
     * Gets search service version.
     *
     * @return the search service version value.
     */
    public SearchServiceVersion getServiceVersion() {
        return this.asyncClient.getServiceVersion();
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
    public SearchIndexerDataSource createOrUpdateDataSource(SearchIndexerDataSource dataSource) {
        return createOrUpdateDataSourceWithResponse(dataSource, false, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * @param dataSource the {@link SearchIndexerDataSource} to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code dataSource} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing data source that was created or updated.
     */
    public Response<SearchIndexerDataSource> createOrUpdateDataSourceWithResponse(SearchIndexerDataSource dataSource,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateDataSourceWithResponse(dataSource, onlyIfUnchanged, requestOptions, context)
            .block();
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * @param dataSource The definition of the data source to create
     * @return the data source that was created.
     */
    public SearchIndexerDataSource createDataSource(SearchIndexerDataSource dataSource) {
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
    public Response<SearchIndexerDataSource> createDataSourceWithResponse(SearchIndexerDataSource dataSource,
        RequestOptions requestOptions, Context context) {
        return asyncClient.createDataSourceWithResponse(dataSource, requestOptions, context).block();
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @return the DataSource.
     */
    public SearchIndexerDataSource getDataSource(String dataSourceName) {
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
    public Response<SearchIndexerDataSource> getDataSourceWithResponse(String dataSourceName,
        RequestOptions requestOptions, Context context) {
        return asyncClient.getDataSourceWithResponse(dataSourceName, requestOptions, context).block();
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @return a list of DataSources
     */
    public PagedIterable<SearchIndexerDataSource> listDataSources() {
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
    public PagedIterable<SearchIndexerDataSource> listDataSources(String select, RequestOptions requestOptions,
        Context context) {
        return new PagedIterable<>(asyncClient.listDataSources(select, requestOptions, context));
    }

    /**
     * Delete a DataSource
     *
     * @param dataSourceName the name of the data source to be deleted
     */
    public void deleteDataSource(String dataSourceName) {
        deleteDataSourceWithResponse(new SearchIndexerDataSource().setName(dataSourceName), false, null, Context.NONE);
    }

    /**
     * Delete a DataSource with Response
     *
     * @param dataSource the {@link SearchIndexerDataSource} to be deleted.
     * @param onlyIfUnchanged {@code true} to delete if the {@code dataSource} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return an empty response
     */
    public Response<Void> deleteDataSourceWithResponse(SearchIndexerDataSource dataSource, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        String etag = onlyIfUnchanged ? dataSource.getETag() : null;
        return asyncClient.deleteDataSourceWithResponse(dataSource.getName(), etag, requestOptions, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * @param indexer definition of the indexer to create.
     * @return the created Indexer.
     */
    public SearchIndexer createIndexer(SearchIndexer indexer) {
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
    public Response<SearchIndexer> createIndexerWithResponse(SearchIndexer indexer, RequestOptions requestOptions,
        Context context) {
        return asyncClient.createIndexerWithResponse(indexer, requestOptions, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The definition of the indexer to create or update.
     * @return a response containing the created Indexer.
     */
    public SearchIndexer createOrUpdateIndexer(SearchIndexer indexer) {
        return createOrUpdateIndexerWithResponse(indexer, false, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The {@link SearchIndexer} to create or update.
     * @param onlyIfUnchanged {@code true} to update if the {@code indexer} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return A response object containing the Indexer.
     */
    public Response<SearchIndexer> createOrUpdateIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateIndexerWithResponse(indexer, onlyIfUnchanged, requestOptions, context).block();
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * @return all Indexers from the Search service.
     */
    public PagedIterable<SearchIndexer> listIndexers() {
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
    public PagedIterable<SearchIndexer> listIndexers(String select, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listIndexers(select, requestOptions, context));
    }

    /**
     * Retrieves an indexer definition.
     *
     * @param indexerName the name of the indexer to retrieve
     * @return the indexer.
     */
    public SearchIndexer getIndexer(String indexerName) {
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
    public Response<SearchIndexer> getIndexerWithResponse(String indexerName, RequestOptions requestOptions,
        Context context) {
        return asyncClient.getIndexerWithResponse(indexerName, requestOptions, context).block();
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     */
    public void deleteIndexer(String indexerName) {
        deleteIndexerWithResponse(new SearchIndexer().setName(indexerName), false, null, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexer the search {@link SearchIndexer}
     * @param onlyIfUnchanged {@code true} to delete if the {@code indexer} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context the context
     * @return a response signalling completion.
     */
    public Response<Void> deleteIndexerWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        String etag = onlyIfUnchanged ? indexer.getETag() : null;
        return asyncClient.deleteIndexerWithResponse(indexer.getName(), etag, requestOptions, context).block();
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
    public SearchIndexerStatus getIndexerStatus(String indexerName) {
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
    public Response<SearchIndexerStatus> getIndexerStatusWithResponse(String indexerName,
        RequestOptions requestOptions, Context context) {
        return asyncClient.getIndexerStatusWithResponse(indexerName, requestOptions, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search index
     *
     * @param index definition of the index to create
     * @return the created Index.
     */
    public SearchIndex createIndex(SearchIndex index) {
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
    public Response<SearchIndex> createIndexWithResponse(SearchIndex index, RequestOptions requestOptions,
        Context context) {
        return asyncClient.createIndexWithResponse(index, requestOptions, context).block();
    }

    /**
     * Retrieves an index definition from the Azure Cognitive Search.
     *
     * @param indexName the name of the index to retrieve
     * @return the Index.
     */
    public SearchIndex getIndex(String indexName) {
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
    public Response<SearchIndex> getIndexWithResponse(String indexName, RequestOptions requestOptions,
        Context context) {
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
    public PagedIterable<SearchIndex> listIndexes() {
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
    public PagedIterable<SearchIndex> listIndexes(String select, RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listIndexes(select, requestOptions, context));
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the definition of the index to create or update
     * @return the index that was created or updated.
     */
    public SearchIndex createOrUpdateIndex(SearchIndex index) {
        return createOrUpdateIndexWithResponse(index, false, false, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search index or updates an index if it already exists.
     *
     * @param index the {@link SearchIndex} to create or update
     * @param allowIndexDowntime allows new analyzers, tokenizers, token filters, or char filters to be added to an
     * index by taking the index offline for at least a few seconds. This temporarily causes indexing and query requests
     * to fail. Performance and write availability of the index can be impaired for several minutes after the index is
     * updated, or longer for very large indexes.
     * @param onlyIfUnchanged {@code true} to update if the {@code index} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the Index that was created or updated.
     */
    public Response<SearchIndex> createOrUpdateIndexWithResponse(SearchIndex index, boolean allowIndexDowntime,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateIndexWithResponse(index, allowIndexDowntime, onlyIfUnchanged, requestOptions,
            context).block();
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param indexName the name of the index to delete
     */
    public void deleteIndex(String indexName) {
        deleteIndexWithResponse(new SearchIndex().setName(indexName), false, null, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search index and all the documents it contains.
     *
     * @param index the Search {@link SearchIndex} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code index} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteIndexWithResponse(SearchIndex index, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        String etag = onlyIfUnchanged ? index.getETag() : null;
        return asyncClient.deleteIndexWithResponse(index.getName(), etag, requestOptions, context).block();
    }

    /**
     * Shows how an analyzer breaks text into tokens.
     *
     * @param indexName the name of the index for which to test an analyzer
     * @param analyzeRequest the text and analyzer or analysis components to test
     * @return analyze result.
     */
    public PagedIterable<AnalyzedTokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest) {
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
    public PagedIterable<AnalyzedTokenInfo> analyzeText(String indexName, AnalyzeRequest analyzeRequest,
        RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.analyzeText(indexName, analyzeRequest, requestOptions, context));
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @return the created SearchIndexerSkillset.
     */
    public SearchIndexerSkillset createSkillset(SearchIndexerSkillset skillset) {
        return createSkillsetWithResponse(skillset, null, Context.NONE).getValue();
    }

    /**
     * Creates a new skillset in an Azure Cognitive Search service.
     *
     * @param skillset definition of the skillset containing one or more cognitive skills
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the created SearchIndexerSkillset.
     */
    public Response<SearchIndexerSkillset> createSkillsetWithResponse(SearchIndexerSkillset skillset,
        RequestOptions requestOptions,
        Context context) {
        return asyncClient.createSkillsetWithResponse(skillset, requestOptions, context).block();
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @return the SearchIndexerSkillset.
     */
    public SearchIndexerSkillset getSkillset(String skillsetName) {
        return getSkillsetWithResponse(skillsetName, null, Context.NONE).getValue();
    }

    /**
     * Retrieves a skillset definition.
     *
     * @param skillsetName the name of the skillset to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the SearchIndexerSkillset.
     */
    public Response<SearchIndexerSkillset> getSkillsetWithResponse(String skillsetName, RequestOptions requestOptions,
        Context context) {
        return asyncClient.getSkillsetWithResponse(skillsetName, requestOptions, context).block();
    }

    /**
     * Lists all skillsets available for an Azure Cognitive Search service.
     *
     * @return the list of skillsets.
     */
    public PagedIterable<SearchIndexerSkillset> listSkillsets() {
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
    public PagedIterable<SearchIndexerSkillset> listSkillsets(String select, RequestOptions requestOptions,
        Context context) {
        return new PagedIterable<>(asyncClient.listSkillsets(select, requestOptions, context));
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the {@link SearchIndexerSkillset} to create or update.
     * @return the skillset that was created or updated.
     */
    public SearchIndexerSkillset createOrUpdateSkillset(SearchIndexerSkillset skillset) {
        return createOrUpdateSkillsetWithResponse(skillset, false, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search skillset or updates a skillset if it already exists.
     *
     * @param skillset the {@link SearchIndexerSkillset} to create or update.
     * @param onlyIfUnchanged {@code true} to update if the {@code skillset} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the skillset that was created or updated.
     */
    public Response<SearchIndexerSkillset> createOrUpdateSkillsetWithResponse(SearchIndexerSkillset skillset,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateSkillsetWithResponse(skillset, onlyIfUnchanged, requestOptions, context)
            .block();
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillsetName the name of the skillset to delete
     */
    public void deleteSkillset(String skillsetName) {
        deleteSkillsetWithResponse(new SearchIndexerSkillset().setName(skillsetName), false, null, Context.NONE);
    }

    /**
     * Deletes a cognitive skillset in an Azure Cognitive Search service.
     *
     * @param skillset the {@link SearchIndexerSkillset} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code skillset} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteSkillsetWithResponse(SearchIndexerSkillset skillset, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        String etag = onlyIfUnchanged ? skillset.getETag() : null;
        return asyncClient.deleteSkillsetWithResponse(skillset.getName(), etag, requestOptions, context).block();
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
        return createOrUpdateSynonymMapWithResponse(synonymMap, false, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search synonym map or updates a synonym map if it already exists.
     *
     * @param synonymMap the definition of the synonym map to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code synonymMap} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the synonym map that was created or updated.
     */
    public Response<SynonymMap> createOrUpdateSynonymMapWithResponse(SynonymMap synonymMap,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateSynonymMapWithResponse(synonymMap, onlyIfUnchanged, requestOptions, context)
            .block();
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMapName the name of the synonym map to delete
     */
    public void deleteSynonymMap(String synonymMapName) {
        deleteSynonymMapWithResponse(new SynonymMap().setName(synonymMapName), false, null, Context.NONE);
    }

    /**
     * Deletes an Azure Cognitive Search synonym map.
     *
     * @param synonymMap the {@link SynonymMap} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code synonymMap} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response signalling completion.
     */
    public Response<Void> deleteSynonymMapWithResponse(SynonymMap synonymMap, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        String etag = onlyIfUnchanged ? synonymMap.getETag() : null;
        return asyncClient.deleteSynonymMapWithResponse(synonymMap.getName(), etag, requestOptions, context)
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
