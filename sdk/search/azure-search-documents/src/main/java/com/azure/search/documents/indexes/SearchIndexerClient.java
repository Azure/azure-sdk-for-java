// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;
import com.azure.search.documents.models.RequestOptions;

/**
 * Synchronous Client to manage and query indexers, as well as manage other resources, on a Cognitive Search service
 */
public class SearchIndexerClient {
    private final SearchIndexerAsyncClient asyncClient;

    SearchIndexerClient(SearchIndexerAsyncClient searchIndexerAsyncClient) {
        this.asyncClient = searchIndexerAsyncClient;
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
    public SearchIndexerDataSourceConnection createOrUpdateDataSource(SearchIndexerDataSourceConnection dataSource) {
        return createOrUpdateDataSourceWithResponse(dataSource, false, null, Context.NONE).getValue();
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * @param dataSource the {@link SearchIndexerDataSourceConnection} to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code dataSource} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing data source that was created or updated.
     */
    public Response<SearchIndexerDataSourceConnection> createOrUpdateDataSourceWithResponse(SearchIndexerDataSourceConnection dataSource,
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
    public SearchIndexerDataSourceConnection createDataSource(SearchIndexerDataSourceConnection dataSource) {
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
    public Response<SearchIndexerDataSourceConnection> createDataSourceWithResponse(SearchIndexerDataSourceConnection dataSource,
        RequestOptions requestOptions, Context context) {
        return asyncClient.createDataSourceWithResponse(dataSource, requestOptions, context).block();
    }

    /**
     * Retrieves a DataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @return the DataSource.
     */
    public SearchIndexerDataSourceConnection getDataSource(String dataSourceName) {
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
    public Response<SearchIndexerDataSourceConnection> getDataSourceWithResponse(String dataSourceName,
        RequestOptions requestOptions, Context context) {
        return asyncClient.getDataSourceWithResponse(dataSourceName, requestOptions, context).block();
    }

    /**
     * List all DataSources from an Azure Cognitive Search service.
     *
     * @return a list of DataSources
     */
    public PagedIterable<SearchIndexerDataSourceConnection> listDataSources() {
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
    public PagedIterable<SearchIndexerDataSourceConnection> listDataSources(String select, RequestOptions requestOptions,
        Context context) {
        return new PagedIterable<>(asyncClient.listDataSources(select, requestOptions, context));
    }

    /**
     * Delete a DataSource
     *
     * @param dataSourceName the name of the data source to be deleted
     */
    public void deleteDataSource(String dataSourceName) {
        deleteDataSourceWithResponse(new SearchIndexerDataSourceConnection().setName(dataSourceName), false, null, Context.NONE);
    }

    /**
     * Delete a DataSource with Response
     *
     * @param dataSource the {@link SearchIndexerDataSourceConnection} to be deleted.
     * @param onlyIfUnchanged {@code true} to delete if the {@code dataSource} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return an empty response
     */
    public Response<Void> deleteDataSourceWithResponse(SearchIndexerDataSourceConnection dataSource, boolean onlyIfUnchanged,
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

}
