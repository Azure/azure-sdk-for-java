// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteParameters;
import com.azure.search.models.DocumentIndexResult;
import com.azure.search.models.IndexBatch;
import com.azure.search.models.SearchParameters;
import com.azure.search.models.SearchRequestOptions;
import com.azure.search.models.SearchResult;
import com.azure.search.models.SuggestParameters;
import com.azure.search.models.SuggestResult;

import reactor.core.publisher.Mono;

import java.util.List;

@ServiceClient(builder = SearchIndexClientBuilder.class)
public class SearchIndexClient {

    private final SearchIndexAsyncClient asyncClient;

    /**
     * Package private constructor to be used by {@link SearchIndexClientBuilder}
     * @param searchIndexAsyncClient Async SearchIndex Client
     */
    SearchIndexClient(SearchIndexAsyncClient searchIndexAsyncClient) {
        this.asyncClient = searchIndexAsyncClient;
    }

    /**
     * Gets the name of the Azure Search index.
     *
     * @return the indexName value.
     */
    public String getIndexName() {
        return asyncClient.getIndexName();
    }

    /**
     * Uploads a collection of documents to the target index
     *
     * @param documents collection of documents to upload to the target Index.
     * @return document index result.
     */
    public DocumentIndexResult uploadDocuments(Iterable<?> documents) {
        return this.uploadDocumentsWithResponse(documents, Context.NONE).getValue();
    }

    /**
     * Uploads a collection of documents to the target index
     *
     * @param documents collection of documents to upload to the target Index.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing the document index result.
     */
    @SuppressWarnings("unchecked")
    public Response<DocumentIndexResult> uploadDocumentsWithResponse(Iterable<?> documents, Context context) {
        return this.indexWithResponse(new IndexBatch().addUploadAction(documents), context);
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     *
     * @param documents collection of documents to be merged
     * @return document index result
     */
    public DocumentIndexResult mergeDocuments(Iterable<?> documents) {
        return this.mergeDocumentsWithResponse(documents, Context.NONE).getValue();
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     *
     * @param documents collection of documents to be merged
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing the document index result.
     */
    @SuppressWarnings("unchecked")
    public Response<DocumentIndexResult> mergeDocumentsWithResponse(Iterable<?> documents, Context context) {
        return this.indexWithResponse(new IndexBatch().addMergeAction(documents), context);
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index.
     * If the document does not exist, it behaves like upload with a new document.
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return document index result
     */
    public DocumentIndexResult mergeOrUploadDocuments(Iterable<?> documents) {
        return this.mergeOrUploadDocumentsWithResponse(documents, Context.NONE).getValue();
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index.
     * If the document does not exist, it behaves like upload with a new document.
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing a document index result
     */
    @SuppressWarnings("unchecked")
    public Response<DocumentIndexResult> mergeOrUploadDocumentsWithResponse(Iterable<?> documents, Context context) {
        return this.indexWithResponse(new IndexBatch().addMergeOrUploadAction(documents), context);
    }

    /**
     * Deletes a collection of documents from the target index
     *
     * @param documents collection of documents to delete from the target Index.
     * @return document index result.
     */
    public DocumentIndexResult deleteDocuments(Iterable<?> documents) {
        return this.deleteDocumentsWithResponse(documents, Context.NONE).getValue();
    }

    /**
     * Deletes a collection of documents from the target index
     *
     * @param documents collection of documents to delete from the target Index.
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing a document index result.
     */
    @SuppressWarnings("unchecked")
    public Response<DocumentIndexResult> deleteDocumentsWithResponse(Iterable<?> documents, Context context) {
        return this.indexWithResponse(new IndexBatch().addDeleteAction(documents), context);
    }

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return asyncClient.getApiVersion();
    }

    /**
     * Gets The DNS suffix of the Azure Search service. The default is search.windows.net.
     *
     * @return the searchDnsSuffix value.
     */
    public String getSearchDnsSuffix() {
        return asyncClient.getSearchDnsSuffix();
    }

    /**
     * Gets The name of the Azure Search service.
     *
     * @return the searchServiceName value.
     */
    public String getSearchServiceName() {
        return asyncClient.getSearchServiceName();
    }

    /**
     * Gets the number of documents
     *
     * @return the number of documents.
     */
    public Long getDocumentCount() {
        return this.getDocumentCountWithResponse(Context.NONE).getValue();
    }

    /**
     * Gets the number of documents
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing the number of documents.
     */
    public Response<Long> getDocumentCountWithResponse(Context context) {
        Mono<Response<Long>> result = asyncClient.getDocumentCountWithResponse(context);
        return result.block();
    }

    /**
     * Searches for documents in the Azure Search index
     *
     * @return A {@link PagedIterable} of SearchResults
     */
    public PagedIterable<SearchResult> search() {
        return this.search(null,
            null,
            null,
            Context.NONE);
    }

    /**
     * Searches for documents in the Azure Search index
     *
     * @param searchText Search Test
     * @param searchParameters Search Parameters
     * @param searchRequestOptions Search Request Options
     * @return A {@link PagedIterable} of SearchResults
     */
    public PagedIterable<SearchResult> search(String searchText,
                                              SearchParameters searchParameters,
                                              SearchRequestOptions searchRequestOptions) {
        return this.search(searchText,
            searchParameters,
            searchRequestOptions,
            Context.NONE);
    }

    /**
     * Searches for documents in the Azure Search index
     *
     * @param searchText Search Test
     * @param searchParameters Search Parameters
     * @param searchRequestOptions Search Request Options
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return A {@link PagedIterable} of SearchResults
     */
    public PagedIterable<SearchResult> search(String searchText,
                                              SearchParameters searchParameters,
                                              SearchRequestOptions searchRequestOptions,
                                              Context context) {
        PagedFlux<SearchResult> result = asyncClient.search(searchText,
            searchParameters,
            searchRequestOptions,
            context);
        return new PagedIterable<>(result);
    }

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key the name of the document
     * @return document object
     */
    public Document getDocument(String key) {
        Mono<Document> results = asyncClient.getDocument(key);
        return results.block();
    }

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key document key
     * @param selectedFields selected fields to return
     * @param searchRequestOptions search request options
     * @return document object
     */
    public Document getDocument(String key,
                                List<String> selectedFields,
                                SearchRequestOptions searchRequestOptions) {
        return this.getDocumentWithResponse(key,
            selectedFields,
            searchRequestOptions,
            Context.NONE).getValue();
    }

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key document key
     * @param selectedFields selected fields to return
     * @param searchRequestOptions search request options
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return response containing a document object
     */
    public Response<Document> getDocumentWithResponse(String key,
                                                      List<String> selectedFields,
                                                      SearchRequestOptions searchRequestOptions,
                                                      Context context) {
        Mono<Response<Document>> results = asyncClient.getDocumentWithResponse(key,
            selectedFields,
            searchRequestOptions,
            context);
        return results.block();
    }

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return suggests result
     */
    public PagedIterable<SuggestResult> suggest(String searchText, String suggesterName) {
        return this.suggest(searchText,
            suggesterName,
            null,
            null,
            Context.NONE);
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
    public PagedIterable<SuggestResult> suggest(String searchText,
                                                String suggesterName,
                                                SuggestParameters suggestParameters,
                                                SearchRequestOptions searchRequestOptions) {
        return this.suggest(searchText,
            suggesterName,
            suggestParameters,
            searchRequestOptions,
            Context.NONE);
    }

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param suggestParameters suggest parameters
     * @param searchRequestOptions search request options
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return suggests results
     */
    public PagedIterable<SuggestResult> suggest(String searchText,
                                                String suggesterName,
                                                SuggestParameters suggestParameters,
                                                SearchRequestOptions searchRequestOptions,
                                                Context context) {
        PagedFlux<SuggestResult> result = asyncClient.suggest(searchText,
            suggesterName,
            suggestParameters,
            searchRequestOptions,
            context);
        return new PagedIterable<>(result);
    }

    /**
     * Sends a batch of document write to the Azure Search index.
     *
     * @param batch batch of documents to send to the index with the requested action
     * @return document index result
     */
    public DocumentIndexResult index(IndexBatch<?> batch) {
        return this.indexWithResponse(batch, Context.NONE).getValue();
    }

    /**
     * Sends a batch of document write to the Azure Search index.
     *
     * @param batch batch of documents to send to the index with the requested action
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return a response containing a document index result
     */
    public Response<DocumentIndexResult> indexWithResponse(IndexBatch<?> batch, Context context) {
        Mono<Response<DocumentIndexResult>> results = asyncClient.indexWithResponse(batch, context);
        return results.block();
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the Azure Search index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return auto complete result
     */
    public PagedIterable<AutocompleteItem> autocomplete(String searchText, String suggesterName) {
        return this.autocomplete(searchText,
            suggesterName,
            null,
            null,
            Context.NONE);
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
    public PagedIterable<AutocompleteItem> autocomplete(String searchText,
                                                        String suggesterName,
                                                        SearchRequestOptions searchRequestOptions,
                                                        AutocompleteParameters autocompleteParameters) {
        return this.autocomplete(searchText,
            suggesterName,
            searchRequestOptions,
            autocompleteParameters,
            Context.NONE);
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the Azure Search index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @param searchRequestOptions search request options
     * @param autocompleteParameters auto complete parameters
     * @param context additional context that is passed through the Http pipeline during the service call
     * @return auto complete result
     */
    public PagedIterable<AutocompleteItem> autocomplete(String searchText,
                                                        String suggesterName,
                                                        SearchRequestOptions searchRequestOptions,
                                                        AutocompleteParameters autocompleteParameters,
                                                        Context context) {
        PagedFlux<AutocompleteItem> result = asyncClient.autocomplete(searchText,
            suggesterName,
            searchRequestOptions,
            autocompleteParameters,
            context);
        return new PagedIterable<>(result);
    }
}
