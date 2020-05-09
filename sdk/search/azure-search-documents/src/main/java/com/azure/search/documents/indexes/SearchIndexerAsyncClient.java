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
import com.azure.search.documents.models.SearchIndexer;
import com.azure.search.documents.models.SearchIndexerStatus;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Asynchronous Client to manage and query search indexer, as well as manage other resources,
 * on a Cognitive Search service.
 */
public class SearchIndexerAsyncClient {
    /**
     * The logger to be used
     */
    private final ClientLogger logger = new ClientLogger(SearchIndexerAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    private final SearchServiceRestClientImpl restClient;

    SearchIndexerAsyncClient(String endpoint, SearchServiceVersion serviceVersion, HttpPipeline httpPipeline) {
        this.restClient = new SearchServiceRestClientBuilder()
            .endpoint(endpoint)
            .apiVersion(serviceVersion.getVersion())
            .pipeline(httpPipeline)
            .build();
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * @param indexer definition of the indexer to create.
     * @return the created Indexer.
     */
    public Mono<SearchIndexer> create(SearchIndexer indexer) {
        return createWithResponse(indexer, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search indexer.
     *
     * @param indexer definition of the indexer to create
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the created Indexer.
     */
    public Mono<Response<SearchIndexer>> createWithResponse(SearchIndexer indexer,
        RequestOptions requestOptions) {
        return withContext(context -> createWithResponse(indexer, requestOptions, context));
    }

    Mono<Response<SearchIndexer>> createWithResponse(SearchIndexer indexer, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexers()
                .createWithRestResponseAsync(indexer, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer The definition of the indexer to create or update.
     * @return a response containing the created Indexer.
     */
    public Mono<SearchIndexer> createOrUpdate(SearchIndexer indexer) {
        return createOrUpdateWithResponse(indexer, false, null).map(Response::getValue);
    }

    /**
     * Creates a new Azure Cognitive Search indexer or updates an indexer if it already exists.
     *
     * @param indexer the definition of the {@link SearchIndexer} to create or update
     * @param onlyIfUnchanged {@code true} to update if the {@code indexer} is the same as the current service value.
     * {@code false} to always update existing value.
     * @param requestOptions additional parameters for the operation Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the created Indexer.
     */
    public Mono<Response<SearchIndexer>> createOrUpdateWithResponse(SearchIndexer indexer,
        boolean onlyIfUnchanged, RequestOptions requestOptions) {
        return withContext(context ->
            createOrUpdateWithResponse(indexer, onlyIfUnchanged, requestOptions, context));
    }

    Mono<Response<SearchIndexer>> createOrUpdateWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        RequestOptions requestOptions, Context context) {
        Objects.requireNonNull(indexer, "'Indexer' cannot be 'null'");
        String ifMatch = onlyIfUnchanged ? indexer.getETag() : null;
        try {
            return restClient.indexers()
                .createOrUpdateWithRestResponseAsync(indexer.getName(), indexer, ifMatch, null, requestOptions,
                    context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves an indexer definition.
     *
     * @param indexerName the name of the indexer to retrieve
     * @return the indexer.
     */
    public Mono<SearchIndexer> getIndexer(String indexerName) {
        return getIndexerWithResponse(indexerName, null).map(Response::getValue);
    }

    /**
     * Retrieves an indexer definition.
     *
     * @param indexerName the name of the indexer to retrieve
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the indexer.
     */
    public Mono<Response<SearchIndexer>> getIndexerWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(context -> getIndexerWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<SearchIndexer>> getIndexerWithResponse(String indexerName, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexers()
                .getWithRestResponseAsync(indexerName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * @return all Indexers from the Search service.
     */
    public PagedFlux<SearchIndexer> listIndexers() {
        return listIndexers(null);
    }

    /**
     * Lists all indexers available for an Azure Cognitive Search service.
     *
     * @param requestOptions Additional parameters for the operation.
     * @return a response containing all Indexers from the Search service.
     */
    public PagedFlux<SearchIndexer> listIndexers(RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexersWithResponse(null, requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndexer> listIndexers(RequestOptions requestOptions, Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexersWithResponse(null, requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private Mono<PagedResponse<SearchIndexer>> listIndexersWithResponse(String select, RequestOptions requestOptions,
        Context context) {
        return restClient.indexers()
            .listWithRestResponseAsync(select, requestOptions, context)
            .map(response -> new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue().getIndexers(),
                null,
                null));
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     * @return a response signalling completion.
     */
    public Mono<Void> delete(String indexerName) {
        return withContext(context -> deleteWithResponse(indexerName, null, null, context)
            .flatMap(FluxUtil::toMono));
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexer the {@link SearchIndexer} to delete
     * @param onlyIfUnchanged {@code true} to delete if the {@code indexer} is the same as the current service value.
     * {@code false} to always delete existing value.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> deleteWithResponse(SearchIndexer indexer, boolean onlyIfUnchanged,
        RequestOptions requestOptions) {
        Objects.requireNonNull(indexer, "'Indexer' cannot be null");
        String etag = onlyIfUnchanged ? indexer.getETag() : null;
        return withContext(context -> deleteWithResponse(indexer.getName(), etag, requestOptions, context));
    }

    /**
     * Deletes an Azure Cognitive Search indexer.
     *
     * @param indexerName the name of the indexer to delete
     * @param etag Optional. The etag to match.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @param context the context
     * @return a response signalling completion.
     */
    Mono<Response<Void>> deleteWithResponse(String indexerName, String etag, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexers()
                .deleteWithRestResponseAsync(indexerName, etag, null, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * @param indexerName the name of the indexer to reset
     * @return a response signalling completion.
     */
    public Mono<Void> reset(String indexerName) {
        return resetWithResponse(indexerName, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Resets the change tracking state associated with an indexer.
     *
     * @param indexerName the name of the indexer to reset
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> resetWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(context -> resetWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<Void>> resetWithResponse(String indexerName, RequestOptions requestOptions, Context context) {
        try {
            return restClient.indexers()
                .resetWithRestResponseAsync(indexerName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Runs an indexer on-demand.
     *
     * @param indexerName the name of the indexer to run
     * @return a response signalling completion.
     */
    public Mono<Void> run(String indexerName) {
        return runWithResponse(indexerName, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Runs an indexer on-demand.
     *
     * @param indexerName the name of the indexer to run
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response signalling completion.
     */
    public Mono<Response<Void>> runWithResponse(String indexerName, RequestOptions requestOptions) {
        return withContext(context -> runWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<Void>> runWithResponse(String indexerName, RequestOptions requestOptions, Context context) {
        try {
            return restClient.indexers().runWithRestResponseAsync(indexerName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @return the indexer execution info.
     */
    public Mono<SearchIndexerStatus> getStatus(String indexerName) {
        return getStatusWithResponse(indexerName, null).map(Response::getValue);
    }

    /**
     * Returns the current status and execution history of an indexer.
     *
     * @param indexerName the name of the indexer for which to retrieve status
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response with the indexer execution info.
     */
    public Mono<Response<SearchIndexerStatus>> getStatusWithResponse(String indexerName,
        RequestOptions requestOptions) {
        return withContext(context -> getStatusWithResponse(indexerName, requestOptions, context));
    }

    Mono<Response<SearchIndexerStatus>> getStatusWithResponse(String indexerName, RequestOptions requestOptions,
        Context context) {
        try {
            return restClient.indexers()
                .getStatusWithRestResponseAsync(indexerName, requestOptions, context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * List all SearchIndexer names from an Azure Cognitive Search service.
     *
     * @return a list of SearchIndexer names.
     */
    public PagedFlux<SearchIndexer> listSearchIndexerNames() {
        return listSearchIndexerNames(null);
    }

    /**
     * List all SearchIndexer names from an Azure Cognitive Search service.
     *
     * @param requestOptions Additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging.
     * @return a list of SearchIndexer names
     */
    public PagedFlux<SearchIndexer> listSearchIndexerNames(RequestOptions requestOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context -> this.listIndexersWithResponse("name", requestOptions, context)));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<SearchIndexer> listSearchIndexerNames(RequestOptions requestOptions,
        Context context) {
        try {
            return new PagedFlux<>(() -> this.listIndexersWithResponse("name", requestOptions, context));
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

}
