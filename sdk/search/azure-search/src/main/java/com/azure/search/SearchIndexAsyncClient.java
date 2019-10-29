// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedFluxBase;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.search.SearchServiceUrlParser.SearchServiceUrlParts;
import com.azure.search.common.AutoCompletePagedResponse;
import com.azure.search.common.SearchPagedResponse;
import com.azure.search.common.SuggestPagedResponse;
import com.azure.search.implementation.SearchIndexRestClientBuilder;
import com.azure.search.implementation.SearchIndexRestClientImpl;
import com.azure.search.implementation.SerializationUtil;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteOptions;
import com.azure.search.models.AutocompleteRequest;
import com.azure.search.models.DocumentIndexResult;
import com.azure.search.models.IndexAction;
import com.azure.search.models.IndexActionType;
import com.azure.search.models.IndexBatch;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SearchOptions;
import com.azure.search.models.SearchRequest;
import com.azure.search.models.SearchResult;
import com.azure.search.models.SuggestOptions;
import com.azure.search.models.SuggestRequest;
import com.azure.search.models.SuggestResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

import static com.azure.core.implementation.util.FluxUtil.withContext;

@ServiceClient(builder = SearchIndexClientBuilder.class, isAsync = true)
public class SearchIndexAsyncClient {

    /**
     * The lazily-created serializer for search index client.
     */
    private static final SerializerAdapter SERIALIZER = initializeSerializerAdapter();

    /**
     * Search REST API Version
     */
    private final String apiVersion;

    /**
     * The endpoint for the Azure Cognitive Search service.
     */
    private final String endpoint;

    /**
     * The name of the Azure Search index.
     */
    private final String indexName;

    /**
     * The logger to be used
     */
    private final ClientLogger logger = new ClientLogger(SearchIndexAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Search service
     */
    private final SearchIndexRestClientImpl restClient;

    /**
     * The pipeline that powers this client.
     */
    private final HttpPipeline httpPipeline;

    /**
     * Package private constructor to be used by {@link SearchIndexClientBuilder}
     */
    SearchIndexAsyncClient(String endpoint, String indexName, String apiVersion, HttpPipeline httpPipeline) {

        SearchServiceUrlParts parts = SearchServiceUrlParser.parseServiceUrlParts(endpoint);

        if (StringUtils.isBlank(indexName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid indexName"));
        }
        if (StringUtils.isBlank(apiVersion)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid apiVersion"));
        }
        if (httpPipeline == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid httpPipeline"));
        }

        this.endpoint = endpoint;
        this.indexName = indexName;
        this.apiVersion = apiVersion;
        this.httpPipeline = httpPipeline;

        restClient = new SearchIndexRestClientBuilder()
            .searchServiceName(parts.serviceName)
            .indexName(indexName)
            .searchDnsSuffix(parts.dnsSuffix)
            .apiVersion(apiVersion)
            .pipeline(httpPipeline)
            .serializer(SERIALIZER)
            .build();
    }

    /**
     * Gets the name of the Azure Search index.
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
     * Uploads a collection of documents to the target index
     *
     * @param documents collection of documents to upload to the target Index.
     * @return document index result.
     */
    public Mono<DocumentIndexResult> uploadDocuments(Iterable<?> documents) {
        return this.uploadDocumentsWithResponse(documents)
            .map(Response::getValue);
    }

    /**
     * Uploads a collection of documents to the target index
     *
     * @param documents collection of documents to upload to the target Index.
     * @return response containing the document index result.
     */
    public Mono<Response<DocumentIndexResult>> uploadDocumentsWithResponse(Iterable<?> documents) {
        return withContext(context -> uploadDocumentsWithResponse(documents, context));
    }

    @SuppressWarnings("unchecked")
    Mono<Response<DocumentIndexResult>> uploadDocumentsWithResponse(Iterable<?> documents, Context context) {
        IndexBatch<?> batch = buildIndexBatch(documents, IndexActionType.UPLOAD);
        return this.indexWithResponse(batch, context);
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     *
     * @param documents collection of documents to be merged
     * @return document index result
     */
    public Mono<DocumentIndexResult> mergeDocuments(Iterable<?> documents) {
        return this.mergeDocumentsWithResponse(documents)
            .map(Response::getValue);
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     *
     * @param documents collection of documents to be merged
     * @return response containing the document index result.
     */
    public Mono<Response<DocumentIndexResult>> mergeDocumentsWithResponse(Iterable<?> documents) {
        return withContext(context -> mergeDocumentsWithResponse(documents, context));
    }

    @SuppressWarnings("unchecked")
    Mono<Response<DocumentIndexResult>> mergeDocumentsWithResponse(Iterable<?> documents, Context context) {
        IndexBatch<?> batch = buildIndexBatch(documents, IndexActionType.MERGE);
        return this.indexWithResponse(batch, context);
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index.
     * If the document does not exist, it behaves like upload with a new document.
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return document index result
     */
    public Mono<DocumentIndexResult> mergeOrUploadDocuments(Iterable<?> documents) {
        return this.mergeOrUploadDocumentsWithResponse(documents)
            .map(Response::getValue);
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index.
     * If the document does not exist, it behaves like upload with a new document.
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return response containing the document index result.
     */
    public Mono<Response<DocumentIndexResult>> mergeOrUploadDocumentsWithResponse(Iterable<?> documents) {
        return withContext(context -> mergeOrUploadDocumentsWithResponse(documents, context));
    }

    @SuppressWarnings("unchecked")
    Mono<Response<DocumentIndexResult>> mergeOrUploadDocumentsWithResponse(Iterable<?> documents, Context context) {
        IndexBatch<?> batch = buildIndexBatch(documents, IndexActionType.MERGE_OR_UPLOAD);
        return this.indexWithResponse(batch, context);
    }

    /**
     * Deletes a collection of documents from the target index
     *
     * @param documents collection of documents to delete from the target Index.
     * @return document index result.
     */
    public Mono<DocumentIndexResult> deleteDocuments(Iterable<?> documents) {
        return this.deleteDocumentsWithResponse(documents)
            .map(Response::getValue);
    }

    /**
     * Deletes a collection of documents from the target index
     *
     * @param documents collection of documents to delete from the target Index.
     * @return response containing the document index result.
     */
    public Mono<Response<DocumentIndexResult>> deleteDocumentsWithResponse(Iterable<?> documents) {
        return withContext(context -> deleteDocumentsWithResponse(documents, context));
    }

    @SuppressWarnings("unchecked")
    Mono<Response<DocumentIndexResult>> deleteDocumentsWithResponse(Iterable<?> documents, Context context) {
        IndexBatch<?> batch = buildIndexBatch(documents, IndexActionType.DELETE);
        return this.indexWithResponse(batch, context);
    }

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.apiVersion;
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
     * Gets the number of documents
     *
     * @return the number of documents.
     */
    public Mono<Long> getDocumentCount() {
        return this.getDocumentCountWithResponse()
            .map(Response::getValue);
    }

    /**
     * Gets the number of documents
     *
     * @return response containing the number of documents.
     */
    public Mono<Response<Long>> getDocumentCountWithResponse() {
        return withContext(this::getDocumentCountWithResponse);
    }

    Mono<Response<Long>> getDocumentCountWithResponse(Context context) {
        return restClient
            .documents()
            .countWithRestResponseAsync(context)
            .map(Function.identity());
    }

    /**
     * Searches for documents in the Azure Search index
     *
     * @return A {@link PagedFlux} of SearchResults
     */
    public PagedFluxBase<SearchResult, SearchPagedResponse> search() {
        return this.search(null, null, null);
    }

    /**
     * Searches for documents in the Azure Search index
     *
     * @param searchText Search text
     * @param searchOptions search options
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return A {@link PagedFlux} of SearchResults
     */
    public PagedFluxBase<SearchResult, SearchPagedResponse> search(
        String searchText,
        SearchOptions searchOptions,
        RequestOptions requestOptions) {
        SearchRequest searchRequest = createSearchRequest(searchText, searchOptions);
        return new PagedFluxBase<>(
            () -> withContext(context -> searchFirstPage(searchRequest, requestOptions, context)),
            skip -> withContext(context -> searchNextPage(searchRequest, requestOptions, skip, context)));
    }

    PagedFluxBase<SearchResult, SearchPagedResponse> search(
        String searchText,
        SearchOptions searchOptions,
        RequestOptions requestOptions,
        Context context) {
        SearchRequest searchRequest = createSearchRequest(searchText, searchOptions);
        return new PagedFluxBase<>(
            () -> searchFirstPage(searchRequest, requestOptions, context),
            skip -> searchNextPage(searchRequest, requestOptions, skip, context));
    }

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key the name of the document
     * @return the document object
     */
    public Mono<Document> getDocument(String key) {
        return this.getDocumentWithResponse(key, null, null)
            .map(Response::getValue);

    }

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key document key
     * @param selectedFields selected fields to return
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return the document object
     */
    public Mono<Document> getDocument(
        String key,
        List<String> selectedFields,
        RequestOptions requestOptions) {
        return this.getDocumentWithResponse(key, selectedFields, requestOptions)
            .map(Response::getValue);
    }

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key document key
     * @param selectedFields selected fields to return
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the document object
     */
    public Mono<Response<Document>> getDocumentWithResponse(
        String key,
        List<String> selectedFields,
        RequestOptions requestOptions) {
        return withContext(context -> getDocumentWithResponse(key, selectedFields, requestOptions, context));
    }

    Mono<Response<Document>> getDocumentWithResponse(
        String key,
        List<String> selectedFields,
        RequestOptions requestOptions,
        Context context) {
        return restClient
            .documents()
            .getWithRestResponseAsync(key, selectedFields, requestOptions, context)
            .map(res -> {
                Document doc = res.getValue();
                DocumentResponseConversions.cleanupDocument(doc);
                return new SimpleResponse<>(res, doc);
            })
            .onErrorMap(DocumentResponseConversions::exceptionMapper)
            //TODO: remove logging statements
            .doOnSuccess(s -> logger.info(
                "Document with key: " + key
                    + " and selectedFields: " + selectedFields
                    + " was retrieved successfully"))
            .doOnError(e -> logger.error("An error occurred in "
                + "getDocument(key, selectedFields, requestOptions): " + e.getMessage()))
            .map(Function.identity());
    }

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return suggests result
     */
    public PagedFluxBase<SuggestResult, SuggestPagedResponse> suggest(String searchText, String suggesterName) {
        return suggest(searchText, suggesterName, null, null);
    }

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param suggestOptions suggest options
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return suggests results
     */
    public PagedFluxBase<SuggestResult, SuggestPagedResponse> suggest(
        String searchText,
        String suggesterName,
        SuggestOptions suggestOptions,
        RequestOptions requestOptions) {
        SuggestRequest suggestRequest = createSuggestRequest(searchText, suggesterName, suggestOptions);
        return new PagedFluxBase<>(
            () -> withContext(context -> suggestFirst(requestOptions, suggestRequest, context)),
            nextLink -> Mono.empty());
    }

    PagedFluxBase<SuggestResult, SuggestPagedResponse> suggest(
        String searchText,
        String suggesterName,
        SuggestOptions suggestOptions,
        RequestOptions requestOptions,
        Context context) {
        SuggestRequest suggestRequest = createSuggestRequest(searchText, suggesterName, suggestOptions);
        return new PagedFluxBase<>(
            () -> suggestFirst(requestOptions, suggestRequest, context),
            nextLink -> Mono.empty());
    }

    /**
     * Sends a batch of document actions to the Azure Search index.
     *
     * @param batch batch of documents to send to the index with the requested action
     * @return document index result
     */
    public Mono<DocumentIndexResult> index(IndexBatch<?> batch) {
        return this.indexWithResponse(batch)
            .map(Response::getValue);
    }

    /**
     * Sends a batch of document actions to the Azure Search index.
     *
     * @param batch batch of documents to send to the index with the requested action
     * @return a response containing the document index result
     */
    public Mono<Response<DocumentIndexResult>> indexWithResponse(IndexBatch<?> batch) {
        return withContext(context -> indexWithResponse(batch, context));
    }

    Mono<Response<DocumentIndexResult>> indexWithResponse(IndexBatch<?> batch, Context context) {
        return restClient.documents()
            .indexWithRestResponseAsync(batch, context)
            .handle((res, sink) -> {
                if (res.getStatusCode() == 207) {
                    IndexBatchException ex = new IndexBatchException(res.getValue());
                    sink.error(ex);
                } else {
                    sink.next(res);
                }
            });
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the Azure Search index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return auto complete result
     */
    public PagedFlux<AutocompleteItem> autocomplete(String searchText, String suggesterName) {
        return autocomplete(searchText, suggesterName, null, null);
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the Azure Search index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param autocompleteOptions autocomplete options
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return auto complete result
     */
    public PagedFlux<AutocompleteItem> autocomplete(
        String searchText,
        String suggesterName,
        AutocompleteOptions autocompleteOptions,
        RequestOptions requestOptions) {
        AutocompleteRequest autocompleteRequest = createAutoCompleteRequest(searchText,
            suggesterName,
            autocompleteOptions);
        return new PagedFlux<>(
            () -> withContext(context -> autocompleteFirst(autocompleteRequest, requestOptions, context)),
            nextLink -> Mono.empty());
    }

    PagedFlux<AutocompleteItem> autocomplete(
        String searchText,
        String suggesterName,
        AutocompleteOptions autocompleteOptions,
        RequestOptions requestOptions,
        Context context) {
        AutocompleteRequest autocompleteRequest = createAutoCompleteRequest(searchText,
            suggesterName,
            autocompleteOptions);
        return new PagedFlux<>(
            () -> autocompleteFirst(autocompleteRequest, requestOptions, context),
            nextLink -> Mono.empty());
    }

    /**
     * Retrieve the first page of a document search
     *
     * @param searchRequest the search request
     * @param requestOptions the request options
     * @param context the context to associate with this operation.
     * @return {@link Mono}{@code <}{@link PagedResponse}{@code <}{@link SearchResult}{@code >}{@code >} next page
     * response with results
     */
    private Mono<SearchPagedResponse> searchFirstPage(
        SearchRequest searchRequest,
        RequestOptions requestOptions,
        Context context) {
        return restClient.documents()
            .searchPostWithRestResponseAsync(searchRequest, requestOptions, context)
            .map(SearchPagedResponse::new);
    }

    /**
     * Retrieve the next page of a document search
     *
     * @param searchRequest the search request
     * @param skip number of documents to skip. Due to a limitation in PageFlux, this value is stored as String and
     * converted to its Integer value before making the next request
     * @param context the context to associate with this operation.
     * @return {@link Mono}{@code <}{@link PagedResponse}{@code <}{@link SearchResult}{@code >}{@code >} next page
     * response with results
     */
    private Mono<SearchPagedResponse> searchNextPage(
        SearchRequest searchRequest,
        RequestOptions requestOptions,
        String skip,
        Context context) {
        if (skip == null || skip.isEmpty()) {
            return Mono.empty();
        }

        Integer skipValue = Integer.valueOf(skip);

        return restClient.documents()
            .searchPostWithRestResponseAsync(searchRequest.setSkip(skipValue), requestOptions, context)
            .map(SearchPagedResponse::new);
    }

    private Mono<PagedResponse<AutocompleteItem>> autocompleteFirst(
        AutocompleteRequest autocompleteRequest,
        RequestOptions requestOptions,
        Context context) {
        return restClient.documents()
            .autocompletePostWithRestResponseAsync(autocompleteRequest, requestOptions, context)
            .map(AutoCompletePagedResponse::new);
    }

    private Mono<SuggestPagedResponse> suggestFirst(
        RequestOptions requestOptions,
        SuggestRequest suggestRequest,
        Context context) {
        return restClient.documents()
            .suggestPostWithRestResponseAsync(suggestRequest, requestOptions, context)
            .map(SuggestPagedResponse::new);
    }

    /**
     * Create search request from search text and parameters
     *
     * @param searchText search text
     * @param searchOptions search options
     * @return SearchRequest
     */
    private SearchRequest createSearchRequest(String searchText, SearchOptions searchOptions) {
        SearchRequest searchRequest = new SearchRequest().setSearchText(searchText);
        if (searchOptions != null) {
            searchRequest
                .setSearchMode(searchOptions.getSearchMode())
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
    private SuggestRequest createSuggestRequest(String searchText,
                                                String suggesterName,
                                                SuggestOptions suggestOptions) {
        SuggestRequest suggestRequest = new SuggestRequest()
            .setSearchText(searchText)
            .setSuggesterName(suggesterName);
        if (suggestOptions != null) {
            suggestRequest
                .setFilter(suggestOptions.getFilter())
                .setUseFuzzyMatching(suggestOptions.isUseFuzzyMatching())
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
    private AutocompleteRequest createAutoCompleteRequest(String searchText,
                                                          String suggesterName,
                                                          AutocompleteOptions autocompleteOptions) {
        AutocompleteRequest autoCompleteRequest = new AutocompleteRequest()
            .setSearchText(searchText)
            .setSuggesterName(suggesterName);
        if (autocompleteOptions != null) {
            autoCompleteRequest
                .setFilter(autocompleteOptions.getFilter())
                .setUseFuzzyMatching(autocompleteOptions.isUseFuzzyMatching())
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


    private <T> IndexBatch<T> buildIndexBatch(Iterable<T> documents, IndexActionType actionType) {
        IndexBatch<T> batch = new IndexBatch<>();
        List<IndexAction<T>> actions = batch.getActions();
        documents.forEach(d -> actions.add(new IndexAction<T>()
            .setActionType(actionType)
            .setDocument(d)));
        return batch;
    }
}
