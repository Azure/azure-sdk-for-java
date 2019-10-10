// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.core.util.Context;
import com.azure.search.common.AutoCompletePagedResponse;
import com.azure.search.common.SearchPagedResponse;
import com.azure.search.common.SuggestPagedResponse;
import com.azure.search.implementation.SearchIndexRestClientBuilder;
import com.azure.search.implementation.SearchIndexRestClientImpl;
import com.azure.search.implementation.SerializationUtil;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteParameters;
import com.azure.search.models.AutocompleteRequest;
import com.azure.search.models.DocumentIndexResult;
import com.azure.search.models.IndexBatch;
import com.azure.search.models.SearchParameters;
import com.azure.search.models.SearchRequest;
import com.azure.search.models.SearchRequestOptions;
import com.azure.search.models.SearchResult;
import com.azure.search.models.SuggestParameters;
import com.azure.search.models.SuggestRequest;
import com.azure.search.models.SuggestResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;
import com.azure.core.util.logging.ClientLogger;

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
     * Search Service dns suffix
     */
    private final String searchDnsSuffix;

    /**
     * Search REST API Version
     */
    private final String apiVersion;

    /**
     * The name of the Azure Search service.
     */
    private final String searchServiceName;

    /**
     * The name of the Azure Search index.
     */
    private final String indexName;

    //TODO: remove this from the client instance level
    private Integer skip;

    private final ClientLogger logger = new ClientLogger(SearchIndexAsyncClient.class);

    /**
     * The underlying REST client to be used to actually interact with the Search service
     */
    private final SearchIndexRestClientImpl restClient;

    /**
     * Package private constructor to be used by {@link SearchIndexClientBuilder}
     */
    SearchIndexAsyncClient(
            String searchServiceName, String searchDnsSuffix, String indexName, String apiVersion,
            HttpClient httpClient,
            List<HttpPipelinePolicy> policies) {
        if (StringUtils.isBlank(searchServiceName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid searchServiceName"));
        }
        if (StringUtils.isBlank(searchDnsSuffix)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid searchDnsSuffix"));
        }
        if (StringUtils.isBlank(indexName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid indexName"));
        }
        if (StringUtils.isBlank(apiVersion)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid apiVersion"));
        }
        if (httpClient == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid httpClient"));
        }
        if (policies == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Invalid policies"));
        }

        this.searchServiceName = searchServiceName;
        this.searchDnsSuffix = searchDnsSuffix;
        this.indexName = indexName;
        this.apiVersion = apiVersion;
        this.skip = 0;

        restClient = new SearchIndexRestClientBuilder()
            .searchServiceName(searchServiceName)
            .indexName(indexName)
            .searchDnsSuffix(searchDnsSuffix)
            .apiVersion(apiVersion)
            .pipeline(new HttpPipelineBuilder()
                .httpClient(httpClient)
                .policies(policies.toArray(new HttpPipelinePolicy[0])).build())
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
        return this.indexWithResponse(new IndexBatch().addUploadAction(documents), context);
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
        return this.indexWithResponse(new IndexBatch().addMergeAction(documents));
    }

    @SuppressWarnings("unchecked")
    Mono<Response<DocumentIndexResult>> mergeDocumentsWithResponse(Iterable<?> documents, Context context) {
        return this.indexWithResponse(new IndexBatch().addMergeAction(documents), context);
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
        return this.indexWithResponse(new IndexBatch().addMergeOrUploadAction(documents), context);
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
        return this.indexWithResponse(new IndexBatch().addDeleteAction(documents), context);
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
     * Gets The DNS suffix of the Azure Search service. The default is search.windows.net.
     *
     * @return the searchDnsSuffix value.
     */
    public String getSearchDnsSuffix() {
        return this.searchDnsSuffix;
    }

    /**
     * Gets The name of the Azure Search service.
     *
     * @return the searchServiceName value.
     */
    public String getSearchServiceName() {
        return this.searchServiceName;
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
    public PagedFlux<SearchResult> search() {
        return this.search(null, null, null);
    }

    /**
     * Searches for documents in the Azure Search index
     *
     * @param searchText Search Test
     * @param searchParameters Search Parameters
     * @param searchRequestOptions Search Request Options
     * @return A {@link PagedFlux} of SearchResults
     */
    public PagedFlux<SearchResult> search(
            String searchText,
            SearchParameters searchParameters,
            SearchRequestOptions searchRequestOptions) {
        SearchRequest searchRequest = createSearchRequest(searchText, searchParameters);
        return new PagedFlux<>(
            () -> withContext(context -> searchFirstPage(searchRequest, searchRequestOptions, context)),
            nextLink -> withContext(context -> searchNextPage(searchRequest, searchRequestOptions, nextLink, context)));
    }

    PagedFlux<SearchResult> search(
            String searchText,
            SearchParameters searchParameters,
            SearchRequestOptions searchRequestOptions,
            Context context) {
        SearchRequest searchRequest = createSearchRequest(searchText, searchParameters);
        return new PagedFlux<>(
            () -> searchFirstPage(searchRequest, searchRequestOptions, context),
            nextLink -> searchNextPage(searchRequest, searchRequestOptions, nextLink, context));
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
     * @param searchRequestOptions search request options
     * @return the document object
     */
    public Mono<Document> getDocument(
            String key,
            List<String> selectedFields,
            SearchRequestOptions searchRequestOptions) {
        return this.getDocumentWithResponse(key, selectedFields, searchRequestOptions)
            .map(Response::getValue);
    }

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key document key
     * @param selectedFields selected fields to return
     * @param searchRequestOptions search request options
     * @return a response containing the document object
     */
    public Mono<Response<Document>> getDocumentWithResponse(
            String key,
            List<String> selectedFields,
            SearchRequestOptions searchRequestOptions) {
        return withContext(context -> getDocumentWithResponse(key, selectedFields, searchRequestOptions, context));
    }

    Mono<Response<Document>> getDocumentWithResponse(
            String key,
            List<String> selectedFields,
            SearchRequestOptions searchRequestOptions,
            Context context) {
        return restClient
            .documents()
            .getWithRestResponseAsync(key, selectedFields, searchRequestOptions, context)
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
                + "getDocument(key, selectedFields, searchRequestOptions): " + e.getMessage()))
            .map(Function.identity());
    }

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return suggests result
     */
    public PagedFlux<SuggestResult> suggest(String searchText, String suggesterName) {
        return suggest(searchText, suggesterName, null, null);
    }

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param suggestParameters suggest parameters
     * @param searchRequestOptions search request options
     * @return suggests results
     */
    public PagedFlux<SuggestResult> suggest(
            String searchText,
            String suggesterName,
            SuggestParameters suggestParameters,
            SearchRequestOptions searchRequestOptions) {
        SuggestRequest suggestRequest = createSuggestRequest(searchText, suggesterName, suggestParameters);
        return new PagedFlux<>(
            () -> withContext(context -> suggestFirst(searchRequestOptions, suggestRequest, context)),
            nextLink -> Mono.empty());
    }

    PagedFlux<SuggestResult> suggest(
            String searchText,
            String suggesterName,
            SuggestParameters suggestParameters,
            SearchRequestOptions searchRequestOptions,
            Context context) {
        SuggestRequest suggestRequest = createSuggestRequest(searchText, suggesterName, suggestParameters);
        return new PagedFlux<>(
            () -> suggestFirst(searchRequestOptions, suggestRequest, context),
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
     * @param searchRequestOptions search request options
     * @param autocompleteParameters auto complete parameters
     * @return auto complete result
     */
    public PagedFlux<AutocompleteItem> autocomplete(
            String searchText,
            String suggesterName,
            SearchRequestOptions searchRequestOptions,
            AutocompleteParameters autocompleteParameters) {
        AutocompleteRequest autocompleteRequest = createAutoCompleteRequest(searchText,
            suggesterName,
            autocompleteParameters);
        return new PagedFlux<>(
            () -> withContext(context -> autocompleteFirst(autocompleteRequest, searchRequestOptions, context)),
            nextLink -> Mono.empty());
    }

    PagedFlux<AutocompleteItem> autocomplete(
        String searchText,
        String suggesterName,
        SearchRequestOptions searchRequestOptions,
        AutocompleteParameters autocompleteParameters,
        Context context) {
        AutocompleteRequest autocompleteRequest = createAutoCompleteRequest(searchText,
            suggesterName,
            autocompleteParameters);
        return new PagedFlux<>(
            () -> autocompleteFirst(autocompleteRequest, searchRequestOptions, context),
            nextLink -> Mono.empty());
    }

    /**
     * Retrieve the first page of a document search
     *
     * @param searchRequest the search request
     * @param searchRequestOptions the search request options
     * @param context The context to associate with this operation.
     * @return {@link Mono}{@code <}{@link PagedResponse}{@code <}{@link SearchResult}{@code >}{@code >} next page
     * response with results
     */
    private Mono<PagedResponse<SearchResult>> searchFirstPage(
        SearchRequest searchRequest,
        SearchRequestOptions searchRequestOptions,
        Context context) {
        return restClient.documents()
            .searchPostWithRestResponseAsync(searchRequest, searchRequestOptions, context)
            .map(res -> {
                if (res.getValue().getNextPageParameters() != null) {
                    skip = res.getValue().getNextPageParameters().getSkip();
                }
                return new SearchPagedResponse(res);
            });
    }

    /**
     * Retrieve the next page of a document search
     *
     * @param searchRequest the search request
     * @param nextLink next page link
     * @param context The context to associate with this operation.
     * @return {@link Mono}{@code <}{@link PagedResponse}{@code <}{@link SearchResult}{@code >}{@code >} next page
     * response with results
     */
    private Mono<PagedResponse<SearchResult>> searchNextPage(
            SearchRequest searchRequest,
            SearchRequestOptions searchRequestOptions,
            String nextLink,
            Context context) {
        if (nextLink == null || nextLink.isEmpty()) {
            return Mono.empty();
        }
        if (skip == null) {
            return Mono.empty();
        }
        return restClient.documents()
            .searchPostWithRestResponseAsync(searchRequest.setSkip(skip), searchRequestOptions, context)
            .map(res -> {
                if (res.getValue().getNextPageParameters() == null
                    || res.getValue().getNextPageParameters().getSkip() == null) {
                    skip = null;
                } else {
                    skip = res.getValue().getNextPageParameters().getSkip();
                }
                return new SearchPagedResponse(res);
            });
    }

    private Mono<PagedResponse<AutocompleteItem>> autocompleteFirst(
            AutocompleteRequest autocompleteRequest,
            SearchRequestOptions searchRequestOptions,
            Context context) {
        return restClient.documents()
            .autocompletePostWithRestResponseAsync(autocompleteRequest, searchRequestOptions, context)
            .map(AutoCompletePagedResponse::new);
    }

    private Mono<PagedResponse<SuggestResult>> suggestFirst(
            SearchRequestOptions searchRequestOptions,
            SuggestRequest suggestRequest,
            Context context) {
        return restClient.documents()
            .suggestPostWithRestResponseAsync(suggestRequest, searchRequestOptions, context)
            .map(SuggestPagedResponse::new);
    }

    /**
     * Create search request from search text and parameters
     *
     * @param searchText search text
     * @param searchParameters search parameters
     * @return SearchRequest
     */
    private SearchRequest createSearchRequest(String searchText, SearchParameters searchParameters) {
        SearchRequest searchRequest = new SearchRequest().setSearchText(searchText);
        if (searchParameters != null) {
            searchRequest
                .setSearchMode(searchParameters.getSearchMode())
                .setFacets(searchParameters.getFacets())
                .setFilter(searchParameters.getFilter())
                .setHighlightPostTag(searchParameters.getHighlightPostTag())
                .setHighlightPreTag(searchParameters.getHighlightPreTag())
                .setIncludeTotalResultCount(searchParameters.isIncludeTotalResultCount())
                .setMinimumCoverage(searchParameters.getMinimumCoverage())
                .setQueryType(searchParameters.getQueryType())
                .setScoringParameters(searchParameters.getScoringParameters())
                .setScoringProfile(searchParameters.getScoringProfile())
                .setSkip(searchParameters.getSkip())
                .setTop(searchParameters.getTop());
            if (searchParameters.getHighlightFields() != null) {
                searchRequest.setHighlightFields(String.join(",", searchParameters.getHighlightFields()));
            }
            if (searchParameters.getSearchFields() != null) {
                searchRequest.setSearchFields(String.join(",", searchParameters.getSearchFields()));
            }
            if (searchParameters.getOrderBy() != null) {
                searchRequest.setOrderBy(String.join(",", searchParameters.getOrderBy()));
            }
            if (searchParameters.getSelect() != null) {
                searchRequest.setSelect(String.join(",", searchParameters.getSelect()));
            }
        }

        return searchRequest;
    }

    /**
     * Create suggest request from search text, suggester name, and parameters
     *
     * @param searchText search text
     * @param suggesterName search text
     * @param suggestParameters suggest parameters
     * @return SuggestRequest
     */
    private SuggestRequest createSuggestRequest(String searchText,
                                                String suggesterName,
                                                SuggestParameters suggestParameters) {
        SuggestRequest suggestRequest = new SuggestRequest()
            .setSearchText(searchText)
            .setSuggesterName(suggesterName);
        if (suggestParameters != null) {
            suggestRequest
                .setFilter(suggestParameters.getFilter())
                .setUseFuzzyMatching(suggestParameters.isUseFuzzyMatching())
                .setHighlightPostTag(suggestParameters.getHighlightPostTag())
                .setHighlightPreTag(suggestParameters.getHighlightPreTag())
                .setMinimumCoverage(suggestParameters.getMinimumCoverage())
                .setTop(suggestParameters.getTop());

            List<String> searchFields = suggestParameters.getSearchFields();
            if (searchFields != null) {
                suggestRequest.setSearchFields(String.join(",", searchFields));
            }

            List<String> orderBy = suggestParameters.getOrderBy();
            if (orderBy != null) {
                suggestRequest.setOrderBy(String.join(",", orderBy));
            }

            List<String> select = suggestParameters.getSelect();
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
     * @param autocompleteParameters autocomplete parameters
     * @return AutocompleteRequest
     */
    private AutocompleteRequest createAutoCompleteRequest(String searchText,
                                                          String suggesterName,
                                                          AutocompleteParameters autocompleteParameters) {
        AutocompleteRequest autoCompleteRequest = new AutocompleteRequest()
                                                        .setSearchText(searchText)
                                                        .setSuggesterName(suggesterName);
        if (autocompleteParameters != null) {
            autoCompleteRequest
                .setFilter(autocompleteParameters.getFilter())
                .setUseFuzzyMatching(autocompleteParameters.isUseFuzzyMatching())
                .setHighlightPostTag(autocompleteParameters.getHighlightPostTag())
                .setHighlightPreTag(autocompleteParameters.getHighlightPreTag())
                .setMinimumCoverage(autocompleteParameters.getMinimumCoverage())
                .setTop(autocompleteParameters.getTop())
                .setAutocompleteMode(autocompleteParameters.getAutocompleteMode());
            List<String> searchFields = autocompleteParameters.getSearchFields();
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
}
