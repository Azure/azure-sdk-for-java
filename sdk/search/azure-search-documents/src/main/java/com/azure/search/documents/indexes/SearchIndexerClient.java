// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.models.RequestOptions;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;

/**
 * Synchronous Client to manage and query search indexer, as well as manage other resources,
 * on a Cognitive Search service.
 */
public class SearchIndexerClient {
    private final SearchIndexerAsyncClient asyncClient;

    SearchIndexerClient(SearchIndexerAsyncClient searchServiceAsyncClient) {
        this.asyncClient = searchServiceAsyncClient;
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * @param indexer definition of the indexer to create.
     * @return the created Indexer.
     */
    public SearchIndexer create(SearchIndexer indexer) {
        return createWithResponse(indexer, null, Context.NONE).getValue();
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
    public Response<SearchIndexer> createWithResponse(SearchIndexer indexer, RequestOptions requestOptions,
        Context context) {
        return asyncClient.createWithResponse(indexer, requestOptions, context).block();
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The definition of the indexer to create or update.
     * @return a response containing the created Indexer.
     */
    public SearchIndexer createOrUpdate(SearchIndexer indexer) {
        return createOrUpdateWithResponse(indexer, false, null, Context.NONE).getValue();
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
    public Response<SearchIndexer> createOrUpdateWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        return asyncClient.createOrUpdateWithResponse(indexer, onlyIfUnchanged, requestOptions, context).block();
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * @return all Indexers from the Search service.
     */
    public PagedIterable<SearchIndexer> listIndexers() {
        return listIndexers(null, Context.NONE);
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * @param requestOptions Additional parameters for the operation.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return all Indexers from the Search service.
     */
    public PagedIterable<SearchIndexer> listIndexers(RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listIndexers(requestOptions, context));
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
    public void delete(String indexerName) {
        deleteWithResponse(new SearchIndexer().setName(indexerName), false, null, Context.NONE);
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
    public Response<Void> deleteWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        String etag = onlyIfUnchanged ? indexer.getETag() : null;
        return asyncClient.deleteWithResponse(indexer.getName(), etag, requestOptions, context).block();
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * @param indexerName the name of the indexer to reset
     */
    public void reset(String indexerName) {
        resetWithResponse(indexerName, null, Context.NONE);
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
    public Response<Void> resetWithResponse(String indexerName, RequestOptions requestOptions, Context context) {
        return asyncClient.resetWithResponse(indexerName, requestOptions, context).block();
    }

    /**
     * Runs an indexer on-demand.
     *
     * @param indexerName the name of the indexer to run
     */
    public void run(String indexerName) {
        runWithResponse(indexerName, null, Context.NONE);
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
    public Response<Void> runWithResponse(String indexerName, RequestOptions requestOptions, Context context) {
        return asyncClient.runWithResponse(indexerName, requestOptions, context).block();
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @return a response with the indexer execution info.
     */
    public SearchIndexerStatus getStatus(String indexerName) {
        return getStatusWithResponse(indexerName, null, Context.NONE).getValue();
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
    public Response<SearchIndexerStatus> getStatusWithResponse(String indexerName,
        RequestOptions requestOptions, Context context) {
        return asyncClient.getStatusWithResponse(indexerName, requestOptions, context).block();
    }

    /**
     * List all SearchIndexer names from an Azure Cognitive Search service.
     *
     * @return a list of SearchIndexerDataSource names.
     */
    public PagedIterable<SearchIndexer> listSearchIndexerNames() {
        return listSearchIndexerNames(null, Context.NONE);
    }

    /**
     * List all SearchIndexer names from an Azure Cognitive Search service.
     *
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a list of SearchIndexer names
     */
    public PagedIterable<SearchIndexer> listSearchIndexerNames(RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listSearchIndexerNames(requestOptions, context));
    }
}
