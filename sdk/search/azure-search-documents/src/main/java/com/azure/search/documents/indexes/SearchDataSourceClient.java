// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchIndexerDataSource;

/**
 * Synchronous Client to manage and query data source, as well as manage other resources,
 * on a Cognitive Search service.
 */
@ServiceClient(builder = SearchServiceResourceClientBuilder.class)
public class SearchDataSourceClient {
    private final SearchDataSourceAsyncClient asyncClient;

    SearchDataSourceClient(SearchDataSourceAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
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
     * Retrieves a SearchIndexerDataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @return the SearchIndexerDataSource.
     */
    public SearchIndexerDataSource getDataSource(String dataSourceName) {
        return getDataSourceWithResponse(dataSourceName, null, Context.NONE).getValue();
    }

    /**
     * Retrieves a SearchIndexerDataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the data source to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return a response containing the SearchIndexerDataSource.
     */
    public Response<SearchIndexerDataSource> getDataSourceWithResponse(String dataSourceName,
        RequestOptions requestOptions, Context context) {
        return asyncClient.getDataSourceWithResponse(dataSourceName, requestOptions, context).block();
    }

    /**
     * List all SearchIndexerDataSource from an Azure Cognitive Search service.
     *
     * @return a list of SearchIndexerDataSource
     */
    public PagedIterable<SearchIndexerDataSource> listDataSources() {
        return listDataSources(null, null, Context.NONE);
    }

    /**
     * List all SearchIndexerDataSource from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of SearchIndexerDataSource definitions to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties.
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a response containing the list of SearchIndexerDataSource.
     */
    public PagedIterable<SearchIndexerDataSource> listDataSources(String select, RequestOptions requestOptions,
        Context context) {
        return new PagedIterable<>(asyncClient.listDataSources(select, requestOptions, context));
    }

    /**
     * Delete a SearchIndexerDataSource
     *
     * @param dataSourceName the name of the data source to be deleted
     */
    public void deleteDataSource(String dataSourceName) {
        deleteDataSourceWithResponse(new SearchIndexerDataSource().setName(dataSourceName), false,
            null, Context.NONE);
    }

    /**
     * Delete a SearchIndexerDataSource with Response
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
     * List all SearchIndexerDataSource names from an Azure Cognitive Search service.
     *
     * @return a list of SearchIndexerDataSource names.
     */
    public PagedIterable<SearchIndexerDataSource> listDataSourceNames() {
        return listDataSourceNames(null, Context.NONE);
    }

    /**
     * List all SearchIndexerDataSource names from an Azure Cognitive Search service.
     *
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return a list of SearchIndexerDataSource names
     */
    public PagedIterable<SearchIndexerDataSource> listDataSourceNames(RequestOptions requestOptions, Context context) {
        return new PagedIterable<>(asyncClient.listDataSourceNames(requestOptions, context));
    }
}
