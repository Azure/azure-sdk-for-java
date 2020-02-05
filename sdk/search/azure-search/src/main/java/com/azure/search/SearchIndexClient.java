// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteOptions;
import com.azure.search.models.IndexBatch;
import com.azure.search.models.IndexDocumentsResult;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SearchOptions;
import com.azure.search.models.SearchResult;
import com.azure.search.models.SuggestOptions;
import com.azure.search.models.SuggestResult;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Cognitive Search Synchronous Client to query an index and upload, merge, or delete documents
 */
@ServiceClient(builder = SearchIndexClientBuilder.class)
public class SearchIndexClient {

    private final SearchIndexAsyncClient asyncClient;

    /**
     * Package private constructor to be used by {@link SearchIndexClientBuilder}
     *
     * @param searchIndexAsyncClient Async SearchIndex Client
     */
    SearchIndexClient(SearchIndexAsyncClient searchIndexAsyncClient) {
        this.asyncClient = searchIndexAsyncClient;
    }

    /**
     * Gets the name of the Azure Cognitive Search index.
     *
     * @return the indexName value.
     */
    public String getIndexName() {
        return asyncClient.getIndexName();
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
    public IndexDocumentsResult uploadDocuments(Iterable<?> documents) {
        return this.uploadDocumentsWithResponse(documents, Context.NONE).getValue();
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
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing the document index result.
     */
    public Response<IndexDocumentsResult> uploadDocumentsWithResponse(Iterable<?> documents, Context context) {
        return asyncClient.uploadDocumentsWithResponse(documents, context).block();
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
    public IndexDocumentsResult mergeDocuments(Iterable<?> documents) {
        return this.mergeDocumentsWithResponse(documents, Context.NONE).getValue();
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
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing the document index result.
     */
    public Response<IndexDocumentsResult> mergeDocumentsWithResponse(Iterable<?> documents, Context context) {
        return asyncClient.mergeDocumentsWithResponse(documents, context).block();
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
    public IndexDocumentsResult mergeOrUploadDocuments(Iterable<?> documents) {
        return this.mergeOrUploadDocumentsWithResponse(documents, Context.NONE).getValue();
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
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing a document index result
     */
    public Response<IndexDocumentsResult> mergeOrUploadDocumentsWithResponse(Iterable<?> documents, Context context) {
        return asyncClient.mergeOrUploadDocumentsWithResponse(documents, context).block();
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
    public IndexDocumentsResult deleteDocuments(Iterable<?> documents) {
        return this.deleteDocumentsWithResponse(documents, Context.NONE).getValue();
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
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing a document index result.
     */
    public Response<IndexDocumentsResult> deleteDocumentsWithResponse(Iterable<?> documents, Context context) {
        return asyncClient.deleteDocumentsWithResponse(documents, context).block();
    }

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public SearchServiceVersion getApiVersion() {
        return asyncClient.getApiVersion();
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
     * Queries the number of documents in the search index.
     *
     * @return the number of documents.
     */
    public Long getDocumentCount() {
        return this.getDocumentCountWithResponse(Context.NONE).getValue();
    }

    /**
     * Queries the number of documents in the search index.
     *
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing the number of documents.
     */
    public Response<Long> getDocumentCountWithResponse(Context context) {
        Mono<Response<Long>> result = asyncClient.getDocumentCountWithResponse(context);
        return result.block();
    }

    /**
     * Searches for documents in the Azure Cognitive Search index
     * See https://docs.microsoft.com/rest/api/searchservice/Search-Documents
     *
     * @param searchText A full-text search query expression;
     * Use null or "*" to match all documents. See
     * see https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search"
     * for more information about search query syntax.
     * @return A {@link PagedIterable} that iterates over {@link SearchResult} objects
     * and provides access to the {@link SearchPagedResponse} object for each page containing HTTP response and count,
     * facet, and coverage information.
     */
    public PagedIterableBase<SearchResult, SearchPagedResponse> search(String searchText) {
        return new PagedIterableBase<>(asyncClient.search(searchText, null, null));
    }

    /**
     * Searches for documents in the Azure Cognitive Search index
     * See https://docs.microsoft.com/rest/api/searchservice/Search-Documents
     *
     * @param searchText A full-text search query expression;
     * Use null or "*" to match all documents. See
     * see https://docs.microsoft.com/rest/api/searchservice/Simple-query-syntax-in-Azure-Search"
     * for more information about search query syntax.
     * @param searchOptions Parameters to further refine the search query
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return A {@link PagedIterable} that iterates over {@link SearchResult} objects
     * and provides access to the {@link SearchPagedResponse} object for each page containing HTTP response and count,
     * facet, and coverage information.
     */
    public PagedIterableBase<SearchResult, SearchPagedResponse> search(String searchText,
                                                                       SearchOptions searchOptions,
                                                                       RequestOptions requestOptions,
                                                                       Context context) {
        return new PagedIterableBase<>(asyncClient.search(searchText, searchOptions, requestOptions, context));
    }

    /**
     * Retrieves a document from the Azure Cognitive Search index.
     * See https://docs.microsoft.com/rest/api/searchservice/Lookup-Document
     *
     * @param key The key of the document to retrieve;
     * See https://docs.microsoft.com/rest/api/searchservice/Naming-rules
     * for the rules for constructing valid document keys.
     * @return document object
     */
    public Document getDocument(String key) {
        Mono<Document> results = asyncClient.getDocument(key);
        return results.block();
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
     * Contains the tracking ID sent with the request to help with debugging
     * @return document object
     */
    public Document getDocument(String key,
                                List<String> selectedFields,
                                RequestOptions requestOptions) {
        return this.getDocumentWithResponse(key,
            selectedFields,
            requestOptions,
            Context.NONE).getValue();
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
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing a document object
     */
    public Response<Document> getDocumentWithResponse(String key,
                                                      List<String> selectedFields,
                                                      RequestOptions requestOptions,
                                                      Context context) {
        Mono<Response<Document>> results = asyncClient.getDocumentWithResponse(key,
            selectedFields,
            requestOptions,
            context);
        return results.block();
    }

    /**
     * Suggests documents in the index that match the given partial query.
     * see https://docs.microsoft.com/rest/api/searchservice/Suggestions
     *
     * @param searchText The search text on which to base suggestions
     * @param suggesterName The name of the suggester as specified in the suggesters collection
     * that's part of the index definition
     * @return A {@link PagedIterableBase} that iterates over {@link SuggestResult} objects
     * and provides access to the {@link SuggestPagedResponse} object for each page containing
     * HTTP response and coverage information.
     */
    public PagedIterableBase<SuggestResult, SuggestPagedResponse> suggest(String searchText, String suggesterName) {
        return new PagedIterableBase<>(asyncClient.suggest(searchText,
            suggesterName, null, null));
    }

    /**
     * Suggests documents in the index that match the given partial query.
     * see https://docs.microsoft.com/rest/api/searchservice/Suggestions
     *
     * @param searchText The search text on which to base suggestions
     * @param suggesterName The name of the suggester as specified in the suggesters collection
     * that's part of the index definition
     * @param suggestOptions Parameters to further refine the suggestion query.
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return A {@link PagedIterableBase} that iterates over {@link SuggestResult} objects
     * and provides access to the {@link SuggestPagedResponse} object for each page containing
     * HTTP response and coverage information.
     */
    public PagedIterableBase<SuggestResult, SuggestPagedResponse> suggest(String searchText,
                                                                          String suggesterName,
                                                                          SuggestOptions suggestOptions,
                                                                          RequestOptions requestOptions,
                                                                          Context context) {
        return new PagedIterableBase<>(asyncClient.suggest(searchText,
            suggesterName, suggestOptions, requestOptions, context));
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
    public IndexDocumentsResult index(IndexBatch<?> batch) {
        return this.indexWithResponse(batch, Context.NONE).getValue();
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
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return Response containing the status of operations for all actions in the batch
     */
    public Response<IndexDocumentsResult> indexWithResponse(IndexBatch<?> batch, Context context) {
        Mono<Response<IndexDocumentsResult>> results = asyncClient.indexWithResponse(batch, context);
        return results.block();
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return auto complete result.
     */
    public PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> autocomplete(
        String searchText, String suggesterName) {
        return new PagedIterableBase<>(asyncClient.autocomplete(searchText, suggesterName));
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param autocompleteOptions autocomplete options
     * @param requestOptions additional parameters for the operation.
     * Contains the tracking ID sent with the request to help with debugging
     * @param context additional context that is passed through the HTTP pipeline during the service call
     * @return auto complete result.
     */
    public PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> autocomplete(String searchText,
                                                        String suggesterName,
                                                        AutocompleteOptions autocompleteOptions,
                                                        RequestOptions requestOptions,
                                                        Context context) {
        return new PagedIterableBase<>(asyncClient.autocomplete(searchText,
            suggesterName, autocompleteOptions, requestOptions, context));
    }
}
