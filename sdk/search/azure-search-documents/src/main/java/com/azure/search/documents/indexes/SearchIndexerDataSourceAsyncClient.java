// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.implementation.SearchServiceRestClientBuilder;
import com.azure.search.documents.implementation.SearchServiceRestClientImpl;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchIndexerDataSource;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Synchronous Client to manage and query data source, as well as manage other resources,
 * on a Cognitive Search service.
 */
public class SearchIndexerDataSourceAsyncClient {
    /**
     * The logger to be used
     */
    private final ClientLogger logger = new ClientLogger(SearchIndexerDataSourceAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    private final SearchServiceRestClientImpl restClient;

    SearchIndexerDataSourceAsyncClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline) {
        this.restClient = new SearchServiceRestClientBuilder()
            .endpoint(endpoint)
            .apiVersion(serviceVersion.getVersion())
            .pipeline(httpPipeline)
            .build();
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * @param dataSource The definition of the {@link SearchIndexerDataSource} to create or update.
     * @return the data source that was created or updated.
     */
    public Mono<SearchIndexerDataSource> createOrUpdateDataSource(SearchIndexerDataSource dataSource) {
        return createOrUpdateDataSourceWithResponse(dataSource, false, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search data source or updates a data source if it already exists.
     *
     * @param dataSource The definition of the {@link SearchIndexerDataSource} to create or update.
     * @param onlyIfUnchanged {@code true} to update if the {@code dataSource} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a data source response.
     */
    public Mono<Response<SearchIndexerDataSource>> createOrUpdateDataSourceWithResponse(
        SearchIndexerDataSource dataSource, boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateDataSourceWithResponse(dataSource, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<SearchIndexerDataSource>> createOrUpdateDataSourceWithResponse(SearchIndexerDataSource dataSource,
        boolean onlyIfUnchanged, RequestOptions requestOptions, Context context) {
        Objects.requireNonNull(dataSource, "'DataSource' cannot be null.");
        String ifMatch = onlyIfUnchanged ? dataSource.getETag() : null;
        try {
            return restClient
                .dataSources()
                .createOrUpdateWithRestResponseAsync(dataSource.getName(),
                    dataSource, ifMatch, null, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * @param dataSource The definition of the dataSource to create.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<SearchIndexerDataSource> createDataSource(SearchIndexerDataSource dataSource) {
        return createDataSourceWithResponse(dataSource, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search data source
     *
     * @param dataSource The definition of the {@link SearchIndexerDataSource} to create.
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<Response<SearchIndexerDataSource>> createDataSourceWithResponse(SearchIndexerDataSource dataSource,
        RequestOptions requestOptions) {
        return withContext(context -> this.createDataSourceWithResponse(dataSource, requestOptions, context));
    }

    Mono<Response<SearchIndexerDataSource>> createDataSourceWithResponse(SearchIndexerDataSource dataSource,
        RequestOptions requestOptions, Context context) {
        try {
            return restClient.dataSources()
                .createWithRestResponseAsync(dataSource, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves a SearchIndexerDataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the {@link SearchIndexerDataSource} to retrieve.
     * @return the SearchIndexerDataSource.
     */
    public Mono<SearchIndexerDataSource> getDataSource(String dataSourceName) {
        return getDataSourceWithResponse(dataSourceName, null).map(Response::getValue);
    }

    /**
     * Retrieves a SearchIndexerDataSource from an Azure Cognitive Search service.
     *
     * @param dataSourceName the name of the {@link SearchIndexerDataSource} to retrieve.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a response containing the SearchIndexerDataSource.
     */
    public Mono<Response<SearchIndexerDataSource>> getDataSourceWithResponse(String dataSourceName,
        RequestOptions requestOptions) {
        return withContext(context -> getDataSourceWithResponse(dataSourceName, requestOptions, context));
    }

    Mono<Response<SearchIndexerDataSource>> getDataSourceWithResponse(String dataSourceName,
        RequestOptions requestOptions, Context context) {
        try {
            return restClient.dataSources()
                .getWithRestResponseAsync(dataSourceName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List all SearchIndexerDataSource from an Azure Cognitive Search service.
     *
     * @return a list of SearchIndexerDataSource
     */
    public PagedFlux<SearchIndexerDataSource> listDataSources() {
        return listDataSources(null, null);
    }

    /**
     * List all SearchIndexerDataSource from an Azure Cognitive Search service.
     *
     * @param select Selects which top-level properties of SearchIndexerDataSource definitions to retrieve.
     * Specified as a comma-separated list of JSON property names, or '*' for all properties.
     * The default is all properties.
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a list of SearchIndexerDataSource
     */
    public PagedFlux<SearchIndexerDataSource> listDataSources(String select, RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listDataSourcesWithResponse(select, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndexerDataSource> listDataSources(String select, RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> this.listDataSourcesWithResponse(select, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<SearchIndexerDataSource>> listDataSourcesWithResponse(String select,
        RequestOptions requestOptions, Context context) {
        return restClient.dataSources()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getDataSources(),
                null,
                null));
    }

    /**
     * Delete a SearchIndexerDataSource
     *
     * @param dataSourceName the name of the {@link SearchIndexerDataSource} for deletion
     * @return a void Mono
     */
    public Mono<Void> deleteDataSource(String dataSourceName) {
        return withContext(context ->
            deleteDataSourceWithResponse(dataSourceName, null, null, context).flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search data source.
     *
     * @param dataSource The {@link SearchIndexerDataSource} to delete.
     * @param onlyIfUnchanged {@code true} to delete if the {@code dataSource} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a mono response
     */
    public Mono<Response<Void>> deleteDataSourceWithResponse(SearchIndexerDataSource dataSource,
        boolean onlyIfUnchanged, RequestOptions requestOptions) {
        Objects.requireNonNull(dataSource, "'DataSource' cannot be null");
        String etag = onlyIfUnchanged ? dataSource.getETag() : null;
        return withContext(context ->
            deleteDataSourceWithResponse(dataSource.getName(), etag, requestOptions, context));
    }

    Mono<Response<Void>> deleteDataSourceWithResponse(String dataSourceName, String etag, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.dataSources()
                .deleteWithRestResponseAsync(
                    dataSourceName,
                    etag, null,
                    requestOptions,
                    context).map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List all SearchIndexerDataSource names from an Azure Cognitive Search service.
     *
     * @return a list of SearchIndexerDataSource names.
     */
    public PagedFlux<SearchIndexerDataSource> listDataSourceNames() {
        return listDataSourceNames(null);
    }

    /**
     * List all SearchIndexerDataSource names from an Azure Cognitive Search service.
     *
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a list of SearchIndexerDataSource names
     */
    public PagedFlux<SearchIndexerDataSource> listDataSourceNames(RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listDataSourcesWithResponse("name", requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndexerDataSource> listDataSourceNames(RequestOptions requestOptions,
        Context context) {
        try {
            return new PagedFlux<>(() -> this.listDataSourcesWithResponse("name", requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

}
