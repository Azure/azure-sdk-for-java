// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFluxBase;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.search.SearchServiceUrlParser.SearchServiceUrlParts;
import com.azure.search.implementation.SearchIndexRestClientBuilder;
import com.azure.search.implementation.SearchIndexRestClientImpl;
import com.azure.search.implementation.SerializationUtil;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteOptions;
import com.azure.search.models.AutocompleteRequest;
import com.azure.search.models.IndexAction;
import com.azure.search.models.IndexActionType;
import com.azure.search.models.IndexBatch;
import com.azure.search.models.IndexDocumentsResult;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SearchOptions;
import com.azure.search.models.SearchRequest;
import com.azure.search.models.SearchResult;
import com.azure.search.models.SuggestOptions;
import com.azure.search.models.SuggestRequest;
import com.azure.search.models.SuggestResult;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class SearchIndexAsyncClient {

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
    private final SearchServiceVersion apiVersion;

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
    SearchIndexAsyncClient(String endpoint, String indexName, SearchServiceVersion apiVersion,
                           HttpPipeline httpPipeline) {

        SearchServiceUrlParts parts = SearchServiceUrlParser.parseServiceUrlParts(endpoint);

        if (CoreUtils.isNullOrEmpty(indexName)) {
            throw logger.logExceptionAsError(new NullPointerException("Invalid indexName"));
        }
        if (apiVersion == null) {
            throw logger.logExceptionAsError(new NullPointerException("Invalid apiVersion"));
        }
        if (httpPipeline == null) {
            throw logger.logExceptionAsError(new NullPointerException("Invalid httpPipeline"));
        }

        this.endpoint = endpoint;
        this.indexName = indexName;
        this.apiVersion = apiVersion;
        this.httpPipeline = httpPipeline;

        restClient = new SearchIndexRestClientBuilder()
            .searchServiceName(parts.serviceName)
            .indexName(indexName)
            .searchDnsSuffix(parts.dnsSuffix)
            .apiVersion(apiVersion.getVersion())
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
     * Uploads a collection of documents to the target index
     * See https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents
     *
     * Exception IndexBatchException is thrown when some of the indexing actions failed,
     * but other actions succeeded and modified the state of the index.
     * This can happen when the Search Service is under heavy indexing load.
     * It is important to explicitly catch this exception and check the return value of
     * its getIndexingResult method. The indexing result reports the status of each indexing
     * action in the batch, making it possible to determine the state of the index after a
     * partial failure.
     *
     * @param documents collection of documents to upload to the target Index.
     * @return document index result.
     */
    public Mono<IndexDocumentsResult> uploadDocuments(Iterable<?> documents) {
        try {
            return this.uploadDocumentsWithResponse(documents)
                .map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Uploads a collection of documents to the target index
     * See https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents
     *
     * Exception IndexBatchException is thrown when some of the indexing actions failed,
     * but other actions succeeded and modified the state of the index.
     * This can happen when the Search Service is under heavy indexing load.
     * It is important to explicitly catch this exception and check the return value of
     * its getIndexingResult method. The indexing result reports the status of each indexing
     * action in the batch, making it possible to determine the state of the index after a
     * partial failure.
     *
     * @param documents collection of documents to upload to the target Index.
     * @return response containing the document index result.
     */
    public Mono<Response<IndexDocumentsResult>> uploadDocumentsWithResponse(Iterable<?> documents) {
        try {
            return withContext(context -> uploadDocumentsWithResponse(documents, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @SuppressWarnings("unchecked")
    Mono<Response<IndexDocumentsResult>> uploadDocumentsWithResponse(Iterable<?> documents, Context context) {
        IndexBatch<?> batch = buildIndexBatch(documents, IndexActionType.UPLOAD);
        return this.indexWithResponse(batch, context);
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     * See https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents
     *
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not
     * merge correctly. If you do not set such a property, it will automatically take its default value
     * (for example, 0 for int or false for boolean), which will override the value of the property currently stored
     * in the index, even if this was not your intent. For this reason, it is strongly recommended that you always
     * declare primitive-typed properties with their class equivalents (for example, an integer property should be
     * of type Integer instead of int).
     *
     * Exception IndexBatchException is thrown when some of the indexing actions failed,
     * but other actions succeeded and modified the state of the index.
     * This can happen when the Search Service is under heavy indexing load.
     * It is important to explicitly catch this exception and check the return value of
     * its getIndexingResult method. The indexing result reports the status of each indexing
     * action in the batch, making it possible to determine the state of the index after a
     * partial failure.
     *
     * @param documents collection of documents to be merged
     * @return document index result
     */
    public Mono<IndexDocumentsResult> mergeDocuments(Iterable<?> documents) {
        try {
            return this.mergeDocumentsWithResponse(documents)
                .map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     * See https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents
     *
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not
     * merge correctly. If you do not set such a property, it will automatically take its default value
     * (for example, 0 for int or false for boolean), which will override the value of the property currently stored
     * in the index, even if this was not your intent. For this reason, it is strongly recommended that you always
     * declare primitive-typed properties with their class equivalents (for example, an integer property should be
     * of type Integer instead of int).
     *
     * Exception IndexBatchException is thrown when some of the indexing actions failed,
     * but other actions succeeded and modified the state of the index.
     * This can happen when the Search Service is under heavy indexing load.
     * It is important to explicitly catch this exception and check the return value of
     * its getIndexingResult method. The indexing result reports the status of each indexing
     * action in the batch, making it possible to determine the state of the index after a
     * partial failure.
     *
     * @param documents collection of documents to be merged
     * @return response containing the document index result.
     */
    public Mono<Response<IndexDocumentsResult>> mergeDocumentsWithResponse(Iterable<?> documents) {
        try {
            return withContext(context -> mergeDocumentsWithResponse(documents, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @SuppressWarnings("unchecked")
    Mono<Response<IndexDocumentsResult>> mergeDocumentsWithResponse(Iterable<?> documents, Context context) {
        try {
            IndexBatch<?> batch = buildIndexBatch(documents, IndexActionType.MERGE);
            return this.indexWithResponse(batch, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index.
     * If the document does not exist, it behaves like upload with a new document.
     * See https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents
     *
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not
     * merge correctly. If you do not set such a property, it will automatically take its default value
     * (for example, 0 for int or false for boolean), which will override the value of the property currently stored
     * in the index, even if this was not your intent. For this reason, it is strongly recommended that you always
     * declare primitive-typed properties with their class equivalents (for example, an integer property should be
     * of type Integer instead of int).
     *
     * Exception IndexBatchException is thrown when some of the indexing actions failed,
     * but other actions succeeded and modified the state of the index.
     * This can happen when the Search Service is under heavy indexing load.
     * It is important to explicitly catch this exception and check the return value of
     * its getIndexingResult method. The indexing result reports the status of each indexing
     * action in the batch, making it possible to determine the state of the index after a
     * partial failure.
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return document index result
     */
    public Mono<IndexDocumentsResult> mergeOrUploadDocuments(Iterable<?> documents) {
        try {
            return this.mergeOrUploadDocumentsWithResponse(documents)
                .map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index.
     * If the document does not exist, it behaves like upload with a new document.
     * See https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents
     *
     * If the type of the document contains non-nullable primitive-typed properties, these properties may not
     * merge correctly. If you do not set such a property, it will automatically take its default value
     * (for example, 0 for int or false for boolean), which will override the value of the property currently stored
     * in the index, even if this was not your intent. For this reason, it is strongly recommended that you always
     * declare primitive-typed properties with their class equivalents (for example, an integer property should be
     * of type Integer instead of int).
     *
     * Exception IndexBatchException is thrown when some of the indexing actions failed,
     * but other actions succeeded and modified the state of the index.
     * This can happen when the Search Service is under heavy indexing load.
     * It is important to explicitly catch this exception and check the return value of
     * its getIndexingResult method. The indexing result reports the status of each indexing
     * action in the batch, making it possible to determine the state of the index after a
     * partial failure.
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return response containing the document index result.
     */
    public Mono<Response<IndexDocumentsResult>> mergeOrUploadDocumentsWithResponse(Iterable<?> documents) {
        try {
            return withContext(context -> mergeOrUploadDocumentsWithResponse(documents, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @SuppressWarnings("unchecked")
    Mono<Response<IndexDocumentsResult>> mergeOrUploadDocumentsWithResponse(Iterable<?> documents, Context context) {
        try {
            IndexBatch<?> batch = buildIndexBatch(documents, IndexActionType.MERGE_OR_UPLOAD);
            return this.indexWithResponse(batch, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a collection of documents from the target index
     * See https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents
     *
     * Exception IndexBatchException is thrown when some of the indexing actions failed,
     * but other actions succeeded and modified the state of the index.
     * This can happen when the Search Service is under heavy indexing load.
     * It is important to explicitly catch this exception and check the return value of
     * its getIndexingResult method. The indexing result reports the status of each indexing
     * action in the batch, making it possible to determine the state of the index after a
     * partial failure.
     *
     * @param documents collection of documents to delete from the target Index.
     *                  Fields other than the key are ignored.
     * @return document index result.
     */
    public Mono<IndexDocumentsResult> deleteDocuments(Iterable<?> documents) {
        try {
            return this.deleteDocumentsWithResponse(documents)
                .map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a collection of documents from the target index
     * See https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents
     *
     * Exception IndexBatchException is thrown when some of the indexing actions failed,
     * but other actions succeeded and modified the state of the index.
     * This can happen when the Search Service is under heavy indexing load.
     * It is important to explicitly catch this exception and check the return value of
     * its getIndexingResult method. The indexing result reports the status of each indexing
     * action in the batch, making it possible to determine the state of the index after a
     * partial failure.
     *
     * @param documents collection of documents to delete from the target Index.
     *                  Fields other than the key are ignored.
     * @return response containing the document index result.
     */
    public Mono<Response<IndexDocumentsResult>> deleteDocumentsWithResponse(Iterable<?> documents) {
        try {
            return withContext(context -> deleteDocumentsWithResponse(documents, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    @SuppressWarnings("unchecked")
    Mono<Response<IndexDocumentsResult>> deleteDocumentsWithResponse(Iterable<?> documents, Context context) {
        try {
            IndexBatch<?> batch = buildIndexBatch(documents, IndexActionType.DELETE);
            return this.indexWithResponse(batch, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public SearchServiceVersion getApiVersion() {
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
     * Queries the number of documents in the search index.
     *
     * @return the number of documents.
     */
    public Mono<Long> getDocumentCount() {
        try {
            return this.getDocumentCountWithResponse()
                .map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Queries the number of documents in the search index.
     *
     * @return response containing the number of documents.
     */
    public Mono<Response<Long>> getDocumentCountWithResponse() {
        try {
            return withContext(this::getDocumentCountWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Long>> getDocumentCountWithResponse(Context context) {
        return restClient
            .documents()
            .countWithRestResponseAsync(context)
            .map(Function.identity());
    }

    /**
     * Searches for documents in the Azure Cognitive Search index
     * See https://docs.microsoft.com/rest/api/searchservice/Search-Documents
     *
     * @param searchText A full-text search query expression;
     * Use null or "*" to match all documents. See
     * See https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search"
     * for more information about search query syntax.
     * @return A {@link PagedFluxBase} that iterates over {@link SearchResult} objects
     * and provides access to the {@link SearchPagedResponse} object for each page containing HTTP response and count,
     * facet, and coverage information.
     */
    public PagedFluxBase<SearchResult, SearchPagedResponse> search(String searchText) {
        try {
            return this.search(searchText, null, null);
        } catch (RuntimeException ex) {
            return new PagedFluxBase<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Searches for documents in the Azure Cognitive Search index
     * See https://docs.microsoft.com/rest/api/searchservice/Search-Documents
     *
     * @param searchText A full-text search query expression;
     * Use null or "*" to match all documents. See
     * See https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search"
     * for more information about search query syntax.
     * @param searchOptions Parameters to further refine the search query
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @return A {@link PagedFluxBase} that iterates over {@link SearchResult} objects
     * and provides access to the {@link SearchPagedResponse} object for each page containing HTTP response and count,
     * facet, and coverage information.
     */
    public PagedFluxBase<SearchResult, SearchPagedResponse> search(String searchText,
                                                                   SearchOptions searchOptions,
                                                                   RequestOptions requestOptions) {
        try {
            SearchRequest searchRequest = this.createSearchRequest(searchText, searchOptions);
            return new PagedFluxBase<>(

                () -> withContext(context -> this.searchFirstPage(searchRequest, requestOptions, context)),
                nextPageParameters -> withContext(context ->
                    this.searchNextPage(searchRequest, requestOptions, nextPageParameters, context)));
        } catch (RuntimeException ex) {
            return new PagedFluxBase<>(() -> monoError(logger, ex));
        }
    }

    PagedFluxBase<SearchResult, SearchPagedResponse> search(String searchText,
                                                            SearchOptions searchOptions,
                                                            RequestOptions requestOptions,
                                                            Context context) {
        SearchRequest searchRequest = this.createSearchRequest(searchText, searchOptions);
        return new PagedFluxBase<>(
            () -> this.searchFirstPage(searchRequest, requestOptions, context),
            nextPageParameters -> this.searchNextPage(searchRequest, requestOptions, nextPageParameters, context));
    }

    /**
     * Retrieves a document from the Azure Cognitive Search index.
     * See https://docs.microsoft.com/rest/api/searchservice/Lookup-Document
     *
     * @param key The key of the document to retrieve;
     * See https://docs.microsoft.com/rest/api/searchservice/Naming-rules
     * for the rules for constructing valid document keys.
     * @return the document object
     */
    public Mono<Document> getDocument(String key) {
        try {
            return this.getDocumentWithResponse(key, null, null)
                .map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves a document from the Azure Cognitive Search index.
     * See https://docs.microsoft.com/rest/api/searchservice/Lookup-Document
     *
     * @param key The key of the document to retrieve;
     * See https://docs.microsoft.com/rest/api/searchservice/Naming-rules
     * for the rules for constructing valid document keys.
     * @param selectedFields List of field names to retrieve for the document;
     * Any field not retrieved will have null or default as its
     * corresponding property value in the returned object.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return the document object
     */
    public Mono<Document> getDocument(
        String key,
        List<String> selectedFields,
        RequestOptions requestOptions) {
        try {
            return this.getDocumentWithResponse(key, selectedFields, requestOptions)
                .map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Retrieves a document from the Azure Cognitive Search index.
     * See https://docs.microsoft.com/rest/api/searchservice/Lookup-Document
     *
     * @param key The key of the document to retrieve;
     * See https://docs.microsoft.com/rest/api/searchservice/Naming-rules
     * for the rules for constructing valid document keys.
     * @param selectedFields List of field names to retrieve for the document;
     * Any field not retrieved will have null or default as its
     * corresponding property value in the returned object.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return a response containing the document object
     */
    public Mono<Response<Document>> getDocumentWithResponse(
        String key,
        List<String> selectedFields,
        RequestOptions requestOptions) {
        try {
            return withContext(context -> getDocumentWithResponse(key, selectedFields, requestOptions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Document>> getDocumentWithResponse(
        String key,
        List<String> selectedFields,
        RequestOptions requestOptions,
        Context context) {
        try {
            return restClient
                .documents()
                .getWithRestResponseAsync(key, selectedFields, requestOptions, context)
                .map(res -> {
                    Document doc = res.getValue();
                    DocumentResponseConversions.cleanupDocument(doc);
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
     * See https://docs.microsoft.com/rest/api/searchservice/Suggestions
     *
     * @param searchText The search text on which to base suggestions
     * @param suggesterName The name of the suggester as specified in the suggesters collection
     * that's part of the index definition
     * @return A {@link PagedFluxBase} that iterates over {@link SuggestResult} objects
     * and provides access to the {@link SuggestPagedResponse} object for each page containing
     * HTTP response and coverage information.
     */
    public PagedFluxBase<SuggestResult, SuggestPagedResponse> suggest(String searchText, String suggesterName) {
        try {
            return this.suggest(searchText, suggesterName, null, null);
        } catch (RuntimeException ex) {
            return new PagedFluxBase<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Suggests documents in the index that match the given partial query.
     * See https://docs.microsoft.com/rest/api/searchservice/Suggestions
     *
     * @param searchText The search text on which to base suggestions
     * @param suggesterName The name of the suggester as specified in the suggesters collection
     * that's part of the index definition
     * @param suggestOptions Parameters to further refine the suggestion query.
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return A {@link PagedFluxBase} that iterates over {@link SuggestResult} objects
     * and provides access to the {@link SuggestPagedResponse} object for each page containing
     * HTTP response and coverage information.
     */
    public PagedFluxBase<SuggestResult, SuggestPagedResponse> suggest(String searchText,
                                                                      String suggesterName,
                                                                      SuggestOptions suggestOptions,
                                                                      RequestOptions requestOptions) {
        try {
            SuggestRequest suggestRequest = this.createSuggestRequest(searchText,
                suggesterName, SuggestOptionsHandler.ensureSuggestOptions(suggestOptions));
            return new PagedFluxBase<>(
                () -> withContext(context -> this.suggestFirst(requestOptions, suggestRequest, context)));
        } catch (RuntimeException ex) {
            return new PagedFluxBase<>(() -> monoError(logger, ex));
        }
    }

    PagedFluxBase<SuggestResult, SuggestPagedResponse> suggest(String searchText,
                                                               String suggesterName,
                                                               SuggestOptions suggestOptions,
                                                               RequestOptions requestOptions,
                                                               Context context) {
        SuggestRequest suggestRequest = this.createSuggestRequest(searchText,
            suggesterName, SuggestOptionsHandler.ensureSuggestOptions(suggestOptions));
        return new PagedFluxBase<>(
            () -> this.suggestFirst(requestOptions, suggestRequest, context));
    }

    /**
     * Sends a batch of upload, merge, and/or delete actions to the search index.
     * See https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents
     *
     * Exception IndexBatchException is thrown when some of the indexing actions failed,
     * but other actions succeeded and modified the state of the index.
     * This can happen when the Search Service is under heavy indexing load.
     * It is important to explicitly catch this exception and check the return value of
     * its getIndexingResult method. The indexing result reports the status of each indexing
     * action in the batch, making it possible to determine the state of the index after a
     * partial failure.
     *
     * @param batch The batch of index actions
     * @return Response containing the status of operations for all actions in the batch.
     */
    public Mono<IndexDocumentsResult> index(IndexBatch<?> batch) {
        try {
            return this.indexWithResponse(batch)
                .map(Response::getValue);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Sends a batch of upload, merge, and/or delete actions to the search index.
     * See https://docs.microsoft.com/rest/api/searchservice/addupdate-or-delete-documents
     *
     * Exception IndexBatchException is thrown when some of the indexing actions failed,
     * but other actions succeeded and modified the state of the index.
     * This can happen when the Search Service is under heavy indexing load.
     * It is important to explicitly catch this exception and check the return value of
     * its getIndexingResult method. The indexing result reports the status of each indexing
     * action in the batch, making it possible to determine the state of the index after a
     * partial failure.
     *
     * @param batch The batch of index actions
     * @return Response containing the status of operations for all actions in the batch
     */
    public Mono<Response<IndexDocumentsResult>> indexWithResponse(IndexBatch<?> batch) {
        try {
            return withContext(context -> indexWithResponse(batch, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<IndexDocumentsResult>> indexWithResponse(IndexBatch<?> batch, Context context) {
        return restClient.documents()
            .indexWithRestResponseAsync(batch, context)
            .handle((res, sink) -> {
                if (res.getStatusCode() == MULTI_STATUS_CODE) {
                    IndexBatchException ex = new IndexBatchException(res.getValue());
                    sink.error(ex);
                } else {
                    sink.next(res);
                }
            });
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return auto complete result.
     */
    public PagedFluxBase<AutocompleteItem, AutocompletePagedResponse> autocomplete(
        String searchText, String suggesterName) {
        try {
            return this.autocomplete(searchText, suggesterName, null, null);
        } catch (RuntimeException ex) {
            return new PagedFluxBase<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param autocompleteOptions autocomplete options
     * @param requestOptions additional parameters for the operation.
     *                       Contains the tracking ID sent with the request to help with debugging
     * @return auto complete result.
     */
    public PagedFluxBase<AutocompleteItem, AutocompletePagedResponse> autocomplete(String searchText,
            String suggesterName, AutocompleteOptions autocompleteOptions, RequestOptions requestOptions) {
        try {
            AutocompleteRequest autocompleteRequest = createAutoCompleteRequest(
                searchText, suggesterName, autocompleteOptions);
            return new PagedFluxBase<>(
                () -> withContext(context -> this.autocompleteFirst(requestOptions, autocompleteRequest, context)));
        } catch (RuntimeException ex) {
            return new PagedFluxBase<>(() -> monoError(logger, ex));
        }
    }

    PagedFluxBase<AutocompleteItem, AutocompletePagedResponse> autocomplete(String searchText,
                                                                            String suggesterName,
                                                                            AutocompleteOptions autocompleteOptions,
                                                                            RequestOptions requestOptions,
                                                                            Context context) {
        AutocompleteRequest autocompleteRequest = createAutoCompleteRequest(
            searchText, suggesterName, autocompleteOptions);
        return new PagedFluxBase<>(
            () -> this.autocompleteFirst(requestOptions, autocompleteRequest, context));
    }

    private Mono<AutocompletePagedResponse> autocompleteFirst(RequestOptions requestOptions,
                                                              AutocompleteRequest autocompleteRequest,
                                                              Context context) {
        return restClient.documents()
            .autocompletePostWithRestResponseAsync(autocompleteRequest, requestOptions, context)
            .map(AutocompletePagedResponse::new);
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
    private Mono<SearchPagedResponse> searchFirstPage(SearchRequest searchRequest,
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
     * @param nextPageParameters json string holding the parameters required to get the next page:
     *                           skip is the number of documents to skip, top is the number of documents per page.
     *                           Due to a limitation in PageFlux, this value is stored as String and converted
     *                           to its Integer value
     *                           before making the next request
     * @param context the context to associate with this operation.
     * @return {@link Mono}{@code <}{@link PagedResponse}{@code <}{@link SearchResult}{@code >}{@code >} next page
     * response with results
     */
    private Mono<SearchPagedResponse> searchNextPage(
        SearchRequest searchRequest,
        RequestOptions requestOptions,
        String nextPageParameters,
        Context context) {

        if (CoreUtils.isNullOrEmpty(nextPageParameters)) {
            return Mono.empty();
        }

        // Extract the value of top and skip from @search.nextPageParameters in SearchPagedResponse
        ObjectMapper objectMapper = new ObjectMapper();
        SearchRequest nextPageRequest;
        try {
            nextPageRequest = objectMapper.readValue(nextPageParameters, SearchRequest.class);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse nextPageParameters with error: %s", e.getMessage());
            return Mono.empty();
        }
        if (nextPageRequest == null || nextPageRequest.getSkip() == null) {
            return Mono.empty();
        }

        searchRequest.setSkip(nextPageRequest.getSkip());
        if (nextPageRequest.getTop() != null) {
            searchRequest.setTop(nextPageRequest.getTop());
        }

        return restClient.documents()
            .searchPostWithRestResponseAsync(searchRequest, requestOptions, context)
            .map(SearchPagedResponse::new);
    }

    private Mono<SuggestPagedResponse> suggestFirst(RequestOptions requestOptions,
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
