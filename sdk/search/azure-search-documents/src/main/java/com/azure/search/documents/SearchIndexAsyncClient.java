// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.search.documents.implementation.SearchIndexRestClientImpl;
import com.azure.search.documents.implementation.models.SearchContinuationToken;
import com.azure.search.documents.implementation.util.DocumentResponseConversions;
import com.azure.search.documents.implementation.util.SuggestOptionsHandler;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.SearchRequest;
import com.azure.search.documents.implementation.SearchIndexRestClientBuilder;
import com.azure.search.documents.implementation.SerializationUtil;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.models.AutocompleteRequest;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestRequest;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.util.AutocompletePagedFlux;
import com.azure.search.documents.util.AutocompletePagedResponse;
import com.azure.search.documents.util.SearchPagedFlux;
import com.azure.search.documents.util.SearchPagedResponse;
import com.azure.search.documents.util.SuggestPagedFlux;
import com.azure.search.documents.util.SuggestPagedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Cognitive Search Asynchronous Client to query an index and upload, merge, or delete documents
 */
@ServiceClient(builder = SearchIndexClientBuilder.class, isAsync = true)
public final class SearchIndexAsyncClient {
    /*
     * Representation of the Multi-Status HTTP response code.
     */
    private static final int MULTI_STATUS_CODE = 207;

    /**
     * The lazily-created serializer for search index client.
     */
    private static final SerializerAdapter SERIALIZER = initializeSerializerAdapter();

    /**
     * Search REST API Version
     */
    private final SearchServiceVersion serviceVersion;

    /**
     * The endpoint for the Azure Cognitive Search service.
     */
    private final String endpoint;

    /**
     * The name of the Azure Cognitive Search index.
     */
    private final String indexName;

    /**
     * The logger to be used
     */
    private final ClientLogger logger = new ClientLogger(SearchIndexAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Azure Cognitive Search service
     */
    private final SearchIndexRestClientImpl restClient;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    /**
     * Package private constructor to be used by {@link SearchIndexClientBuilder}
     */
    SearchIndexAsyncClient(String endpoint, String indexName, SearchServiceVersion serviceVersion,
        HttpPipeline httpPipeline) {

        this.endpoint = endpoint;
        this.indexName = indexName;
        this.serviceVersion = serviceVersion;
        this.httpPipeline = httpPipeline;

        restClient = new SearchIndexRestClientBuilder()
            .endpoint(endpoint)
            .indexName(indexName)
            .apiVersion(serviceVersion.getVersion())
            .pipeline(httpPipeline)
            .serializer(SERIALIZER)
            .build();
    }

    /**
     * Gets the name of the Azure Cognitive Search index.
     *
     * @return the indexName value.
     */
    public String getIndexName() {
        return this.indexName;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return the pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * Uploads a collection of documents to the target index.
     *
     * @param documents collection of documents to upload to the target Index.
     * @return The result of the document indexing actions.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    public Mono<IndexDocumentsResult> uploadDocuments(Iterable<?> documents) {
        return uploadDocumentsWithResponse(documents).map(Response::getValue);
    }

    /**
     * Uploads a collection of documents to the target index.
     *
     * @param documents collection of documents to upload to the target Index.
     * @return A response containing the result of the document indexing actions.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    public Mono<Response<IndexDocumentsResult>> uploadDocumentsWithResponse(Iterable<?> documents) {
        return withContext(context -> uploadDocumentsWithResponse(documents, context));
    }

    Mono<Response<IndexDocumentsResult>> uploadDocumentsWithResponse(Iterable<?> documents, Context context) {
        return indexDocumentsWithResponse(buildIndexBatch(documents, IndexActionType.UPLOAD), context);
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or {@code false} for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * @param documents collection of documents to be merged
     * @return document index result
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    public Mono<IndexDocumentsResult> mergeDocuments(Iterable<?> documents) {
        return mergeDocumentsWithResponse(documents).map(Response::getValue);
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or {@code false} for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * @param documents collection of documents to be merged
     * @return response containing the document index result.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    public Mono<Response<IndexDocumentsResult>> mergeDocumentsWithResponse(Iterable<?> documents) {
        return withContext(context -> mergeDocumentsWithResponse(documents, context));
    }

    Mono<Response<IndexDocumentsResult>> mergeDocumentsWithResponse(Iterable<?> documents, Context context) {
        return indexDocumentsWithResponse(buildIndexBatch(documents, IndexActionType.MERGE), context);
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index. If the document does
     * not exist, it behaves like upload with a new document.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or {@code false} for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return document index result
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    public Mono<IndexDocumentsResult> mergeOrUploadDocuments(Iterable<?> documents) {
        return mergeOrUploadDocumentsWithResponse(documents).map(Response::getValue);
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index. If the document does
     * not exist, it behaves like upload with a new document.
     * <p>
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not merge
     * correctly. If you do not set such a property, it will automatically take its default value (for example, {@code
     * 0} for {@code int} or {@code false} for {@code boolean}), which will override the value of the property currently
     * stored in the index, even if this was not your intent. For this reason, it is strongly recommended that you
     * always declare primitive-typed properties with their class equivalents (for example, an integer property should
     * be of type {@code Integer} instead of {@code int}).
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return document index result
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    public Mono<Response<IndexDocumentsResult>> mergeOrUploadDocumentsWithResponse(Iterable<?> documents) {
        return withContext(context -> mergeOrUploadDocumentsWithResponse(documents, context));
    }

    Mono<Response<IndexDocumentsResult>> mergeOrUploadDocumentsWithResponse(Iterable<?> documents, Context context) {
        return indexDocumentsWithResponse(buildIndexBatch(documents, IndexActionType.MERGE_OR_UPLOAD), context);
    }

    /**
     * Deletes a collection of documents from the target index.
     *
     * @param documents collection of documents to delete from the target Index. Fields other than the key are ignored.
     * @return document index result.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    public Mono<IndexDocumentsResult> deleteDocuments(Iterable<?> documents) {
        return deleteDocumentsWithResponse(documents).map(Response::getValue);
    }

    /**
     * Deletes a collection of documents from the target index.
     *
     * @param documents collection of documents to delete from the target Index. Fields other than the key are ignored.
     * @return response containing the document index result.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    public Mono<Response<IndexDocumentsResult>> deleteDocumentsWithResponse(Iterable<?> documents) {
        return withContext(context -> deleteDocumentsWithResponse(documents, context));
    }

    Mono<Response<IndexDocumentsResult>> deleteDocumentsWithResponse(Iterable<?> documents, Context context) {
        return indexDocumentsWithResponse(buildIndexBatch(documents, IndexActionType.DELETE), context);
    }

    /**
     * Gets the version of the Search service the client is using.
     *
     * @return The version of the Search service the client is using.
     */
    public SearchServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * Gets the endpoint for the Azure Cognitive Search service.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Queries the number of documents in the search index.
     *
     * @return the number of documents.
     */
    public Mono<Long> getDocumentCount() {
        return this.getDocumentCountWithResponse().map(Response::getValue);
    }

    /**
     * Queries the number of documents in the search index.
     *
     * @return response containing the number of documents.
     */
    public Mono<Response<Long>> getDocumentCountWithResponse() {
        return withContext(this::getDocumentCountWithResponse);
    }

    Mono<Response<Long>> getDocumentCountWithResponse(Context context) {
        try {
            return restClient.documents()
                .countWithRestResponseAsync(context)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Searches for documents in the Azure Cognitive Search index.
     * <p>
     * If {@code searchText} is set to {@code null} or {@code "*"} all documents will be matched, see
     * <a href="https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search">simple query
     * syntax in Azure Search</a> for more information about search query syntax.
     *
     * @param searchText A full-text search query expression.
     * @return A {@link SearchPagedFlux} that iterates over {@link SearchResult} objects and provides access to the
     * {@link SearchPagedResponse} object for each page containing HTTP response and count, facet, and coverage
     * information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Search-Documents">Search documents</a>
     */
    public SearchPagedFlux search(String searchText) {
        return this.search(searchText, null, null);
    }

    /**
     * Searches for documents in the Azure Cognitive Search index.
     * <p>
     * If {@code searchText} is set to {@code null} or {@code "*"} all documents will be matched, see
     * <a href="https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search">simple query
     * syntax in Azure Search</a> for more information about search query syntax.
     *
     * @param searchText A full-text search query expression.
     * @param searchOptions Parameters to further refine the search query
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return A {@link SearchPagedFlux} that iterates over {@link SearchResult} objects and provides access to the
     * {@link SearchPagedResponse} object for each page containing HTTP response and count, facet, and coverage
     * information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Search-Documents">Search documents</a>
     */
    public SearchPagedFlux search(String searchText, SearchOptions searchOptions, RequestOptions requestOptions) {
        SearchRequest request = createSearchRequest(searchText, searchOptions);
        Function<String, Mono<SearchPagedResponse>> func = continuationToken -> withContext(context ->
            search(request, requestOptions, continuationToken, context));
        return new SearchPagedFlux(() -> func.apply(null), func);
    }

    SearchPagedFlux search(String searchText, SearchOptions searchOptions, RequestOptions requestOptions,
        Context context) {
        SearchRequest request = createSearchRequest(searchText, searchOptions);
        Function<String, Mono<SearchPagedResponse>> func = continuationToken ->
            search(request, requestOptions, continuationToken, context);
        return new SearchPagedFlux(() -> func.apply(null), func);
    }

    private Mono<SearchPagedResponse> search(SearchRequest request, RequestOptions requestOptions,
        String continuationToken, Context context) {
        SearchRequest requestToUse = (continuationToken == null) ? request
            : SearchContinuationToken.deserializeToken(serviceVersion.getVersion(), continuationToken);

        return restClient.documents().searchPostWithRestResponseAsync(requestToUse, requestOptions, context)
            .map(searchDocumentResponse -> new SearchPagedResponse(searchDocumentResponse, serviceVersion));
    }
    /**
     * Retrieves a document from the Azure Cognitive Search index.
     * <p>
     * View <a href="https://docs.microsoft.com/rest/api/searchservice/Naming-rules">naming rules</a> for guidelines on
     * constructing valid document keys.
     *
     * @param key The key of the document to retrieve.
     * @return the document object
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Lookup-Document">Lookup document</a>
     */
    public Mono<SearchDocument> getDocument(String key) {
        return getDocumentWithResponse(key, null, null).map(Response::getValue);
    }

    /**
     * Retrieves a document from the Azure Cognitive Search index.
     * <p>
     * View <a href="https://docs.microsoft.com/rest/api/searchservice/Naming-rules">naming rules</a> for guidelines on
     * constructing valid document keys.
     *
     * @param key The key of the document to retrieve.
     * @param selectedFields List of field names to retrieve for the document; Any field not retrieved will have null or
     * default as its corresponding property value in the returned object.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return a response containing the document object
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Lookup-Document">Lookup document</a>
     */
    public Mono<Response<SearchDocument>> getDocumentWithResponse(String key, List<String> selectedFields,
        RequestOptions requestOptions) {
        return withContext(context -> getDocumentWithResponse(key, selectedFields, requestOptions, context));
    }

    Mono<Response<SearchDocument>> getDocumentWithResponse(String key, List<String> selectedFields,
        RequestOptions requestOptions, Context context) {
        try {
            return restClient.documents()
                .getWithRestResponseAsync(key, selectedFields, requestOptions, context)
                .map(res -> {
                    SearchDocument doc = new SearchDocument(res.getValue());
                    return new SimpleResponse<>(res, doc);
                })
                .onErrorMap(DocumentResponseConversions::exceptionMapper)
                .map(Function.identity());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Suggests documents in the index that match the given partial query.
     *
     * @param searchText The search text on which to base suggestions
     * @param suggesterName The name of the suggester as specified in the suggesters collection that's part of the index
     * definition
     * @return A {@link SuggestPagedFlux} that iterates over {@link SuggestResult} objects and provides access to the
     * {@link SuggestPagedResponse} object for each page containing HTTP response and coverage information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Suggestions">Suggestions</a>
     */
    public SuggestPagedFlux suggest(String searchText, String suggesterName) {
        return suggest(searchText, suggesterName, null, null);
    }

    /**
     * Suggests documents in the index that match the given partial query.
     *
     * @param searchText The search text on which to base suggestions
     * @param suggesterName The name of the suggester as specified in the suggesters collection that's part of the index
     * definition
     * @param suggestOptions Parameters to further refine the suggestion query.
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return A {@link SuggestPagedFlux} that iterates over {@link SuggestResult} objects and provides access to the
     * {@link SuggestPagedResponse} object for each page containing HTTP response and coverage information.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/Suggestions">Suggestions</a>
     */
    public SuggestPagedFlux suggest(String searchText, String suggesterName, SuggestOptions suggestOptions,
        RequestOptions requestOptions) {
        SuggestRequest suggestRequest = createSuggestRequest(searchText, suggesterName,
            SuggestOptionsHandler.ensureSuggestOptions(suggestOptions));

        return new SuggestPagedFlux(() -> withContext(context -> suggest(requestOptions, suggestRequest, context)));
    }

    SuggestPagedFlux suggest(String searchText, String suggesterName, SuggestOptions suggestOptions,
        RequestOptions requestOptions, Context context) {
        SuggestRequest suggestRequest = createSuggestRequest(searchText,
            suggesterName, SuggestOptionsHandler.ensureSuggestOptions(suggestOptions));

        return new SuggestPagedFlux(() -> suggest(requestOptions, suggestRequest, context));
    }

    private Mono<SuggestPagedResponse> suggest(RequestOptions requestOptions, SuggestRequest suggestRequest,
        Context context) {
        return restClient.documents().suggestPostWithRestResponseAsync(suggestRequest, requestOptions, context)
            .map(SuggestPagedResponse::new);
    }

    /**
     * Sends a batch of upload, merge, and/or delete actions to the search index.
     *
     * @param batch The batch of index actions
     * @return Response containing the status of operations for all actions in the batch.
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    public Mono<IndexDocumentsResult> indexDocuments(IndexDocumentsBatch<?> batch) {
        return indexDocumentsWithResponse(batch).map(Response::getValue);
    }

    /**
     * Sends a batch of upload, merge, and/or delete actions to the search index.
     *
     * @param batch The batch of index actions
     * @return Response containing the status of operations for all actions in the batch
     * @throws IndexBatchException If some of the indexing actions fail but other actions succeed and modify the state
     * of the index. This can happen when the Search Service is under heavy indexing load. It is important to explicitly
     * catch this exception and check the return value {@link IndexBatchException#getIndexingResults()}. The indexing
     * result reports the status of each indexing action in the batch, making it possible to determine the state of the
     * index after a partial failure.
     * @see <a href="https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents">Add, update, or
     * delete documents</a>
     */
    public Mono<Response<IndexDocumentsResult>> indexDocumentsWithResponse(IndexDocumentsBatch<?> batch) {
        return withContext(context -> indexDocumentsWithResponse(batch, context));
    }

    Mono<Response<IndexDocumentsResult>> indexDocumentsWithResponse(IndexDocumentsBatch<?> batch, Context context) {
        try {
            return restClient.documents()
                .indexWithRestResponseAsync(batch, context)
                .flatMap(response -> (response.getStatusCode() == MULTI_STATUS_CODE)
                    ? Mono.error(new IndexBatchException(response.getValue()))
                    : Mono.just(response));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return auto complete result.
     */
    public AutocompletePagedFlux autocomplete(String searchText, String suggesterName) {
        return autocomplete(searchText, suggesterName, null, null);
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param autocompleteOptions autocomplete options
     * @param requestOptions additional parameters for the operation. Contains the tracking ID sent with the request to
     * help with debugging
     * @return auto complete result.
     */
    public AutocompletePagedFlux autocomplete(String searchText, String suggesterName,
        AutocompleteOptions autocompleteOptions, RequestOptions requestOptions) {
        AutocompleteRequest request = createAutoCompleteRequest(searchText, suggesterName, autocompleteOptions);

        return new AutocompletePagedFlux(() -> withContext(context -> autocomplete(requestOptions, request, context)));
    }

    AutocompletePagedFlux autocomplete(String searchText, String suggesterName, AutocompleteOptions autocompleteOptions,
        RequestOptions requestOptions, Context context) {
        AutocompleteRequest request = createAutoCompleteRequest(searchText, suggesterName, autocompleteOptions);

        return new AutocompletePagedFlux(() -> autocomplete(requestOptions, request, context));
    }

    private Mono<AutocompletePagedResponse> autocomplete(RequestOptions requestOptions, AutocompleteRequest request,
        Context context) {
        return restClient.documents().autocompletePostWithRestResponseAsync(request, requestOptions, context)
            .map(AutocompletePagedResponse::new);
    }

    /**
     * Create search request from search text and parameters
     *
     * @param searchText search text
     * @param searchOptions search options
     * @return SearchRequest
     */
    private static SearchRequest createSearchRequest(String searchText, SearchOptions searchOptions) {
        SearchRequest searchRequest = new SearchRequest().setSearchText(searchText);

        if (searchOptions != null) {
            searchRequest.setSearchMode(searchOptions.getSearchMode())
                .setFacets(searchOptions.getFacets())
                .setFilter(searchOptions.getFilter())
                .setHighlightPostTag(searchOptions.getHighlightPostTag())
                .setHighlightPreTag(searchOptions.getHighlightPreTag())
                .setIncludeTotalResultCount(searchOptions.isIncludeTotalResultCount())
                .setMinimumCoverage(searchOptions.getMinimumCoverage())
                .setQueryType(searchOptions.getQueryType())
                .setScoringParameters(searchOptions.getScoringParameters())
                .setScoringProfile(searchOptions.getScoringProfile())
                .setSkip(searchOptions.getSkip())
                .setTop(searchOptions.getTop());

            if (searchOptions.getHighlightFields() != null) {
                searchRequest.setHighlightFields(String.join(",", searchOptions.getHighlightFields()));
            }

            if (searchOptions.getSearchFields() != null) {
                searchRequest.setSearchFields(String.join(",", searchOptions.getSearchFields()));
            }

            if (searchOptions.getOrderBy() != null) {
                searchRequest.setOrderBy(String.join(",", searchOptions.getOrderBy()));
            }

            if (searchOptions.getSelect() != null) {
                searchRequest.setSelect(String.join(",", searchOptions.getSelect()));
            }
        }

        return searchRequest;
    }

    /**
     * Create suggest request from search text, suggester name, and parameters
     *
     * @param searchText search text
     * @param suggesterName search text
     * @param suggestOptions suggest options
     * @return SuggestRequest
     */
    private static SuggestRequest createSuggestRequest(String searchText, String suggesterName,
        SuggestOptions suggestOptions) {
        SuggestRequest suggestRequest = new SuggestRequest()
            .setSearchText(searchText)
            .setSuggesterName(suggesterName);

        if (suggestOptions != null) {
            suggestRequest.setFilter(suggestOptions.getFilter())
                .setUseFuzzyMatching(suggestOptions.useFuzzyMatching())
                .setHighlightPostTag(suggestOptions.getHighlightPostTag())
                .setHighlightPreTag(suggestOptions.getHighlightPreTag())
                .setMinimumCoverage(suggestOptions.getMinimumCoverage())
                .setTop(suggestOptions.getTop());

            List<String> searchFields = suggestOptions.getSearchFields();
            if (searchFields != null) {
                suggestRequest.setSearchFields(String.join(",", searchFields));
            }

            List<String> orderBy = suggestOptions.getOrderBy();
            if (orderBy != null) {
                suggestRequest.setOrderBy(String.join(",", orderBy));
            }

            List<String> select = suggestOptions.getSelect();
            if (select != null) {
                suggestRequest.setSelect(String.join(",", select));
            }
        }

        return suggestRequest;
    }

    /**
     * Create Autocomplete request from search text, suggester name, and parameters
     *
     * @param searchText search text
     * @param suggesterName search text
     * @param autocompleteOptions autocomplete options
     * @return AutocompleteRequest
     */
    private static AutocompleteRequest createAutoCompleteRequest(String searchText, String suggesterName,
        AutocompleteOptions autocompleteOptions) {
        AutocompleteRequest autoCompleteRequest = new AutocompleteRequest()
            .setSearchText(searchText)
            .setSuggesterName(suggesterName);

        if (autocompleteOptions != null) {
            autoCompleteRequest.setFilter(autocompleteOptions.getFilter())
                .setUseFuzzyMatching(autocompleteOptions.useFuzzyMatching())
                .setHighlightPostTag(autocompleteOptions.getHighlightPostTag())
                .setHighlightPreTag(autocompleteOptions.getHighlightPreTag())
                .setMinimumCoverage(autocompleteOptions.getMinimumCoverage())
                .setTop(autocompleteOptions.getTop())
                .setAutocompleteMode(autocompleteOptions.getAutocompleteMode());

            List<String> searchFields = autocompleteOptions.getSearchFields();
            if (searchFields != null) {
                autoCompleteRequest.setSearchFields(String.join(",", searchFields));
            }
        }

        return autoCompleteRequest;
    }

    /**
     * initialize singleton instance of the default serializer adapter.
     */
    private static synchronized SerializerAdapter initializeSerializerAdapter() {
        JacksonAdapter adapter = new JacksonAdapter();

        ObjectMapper mapper = adapter.serializer();
        SerializationUtil.configureMapper(mapper);

        return adapter;
    }


    private static <T> IndexDocumentsBatch<T> buildIndexBatch(Iterable<T> documents, IndexActionType actionType) {
        IndexDocumentsBatch<T> batch = new IndexDocumentsBatch<>();
        List<IndexAction<T>> actions = batch.getActions();
        documents.forEach(d -> actions.add(new IndexAction<T>()
            .setActionType(actionType)
            .setDocument(d)));
        return batch;
    }
}
