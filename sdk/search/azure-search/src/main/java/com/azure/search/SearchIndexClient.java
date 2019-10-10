// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
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
import reactor.util.annotation.Nullable;
import java.time.Duration;

import java.util.List;

public class SearchIndexClient {

    private final SearchIndexAsyncClient asyncClient;

    /**
     * Package private constructor to be used by {@link SearchIndexClientBuilder}
     * @param searchIndexAsyncClient Async SearchIndex Client
     */
    public SearchIndexClient(SearchIndexAsyncClient searchIndexAsyncClient) {
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
        return this.uploadDocumentsWithResponse(documents).value();
    }

    /**
     * Uploads a collection of documents to the target index
     *
     * @param documents collection of documents to upload to the target Index.
     * @return response containing the document index result.
     */
    @SuppressWarnings("unchecked")
    public Response<DocumentIndexResult> uploadDocumentsWithResponse(Iterable<?> documents) {
        return this.indexWithResponse(new IndexBatch().addUploadAction(documents));
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     *
     * @param documents collection of documents to be merged
     * @return document index result
     */
    public DocumentIndexResult mergeDocuments(Iterable<?> documents) {
        return this.mergeDocumentsWithResponse(documents).value();
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     *
     * @param documents collection of documents to be merged
     * @return response containing the document index result.
     */
    @SuppressWarnings("unchecked")
    public Response<DocumentIndexResult> mergeDocumentsWithResponse(Iterable<?> documents) {
        return this.indexWithResponse(new IndexBatch().addMergeAction(documents));
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index.
     * If the document does not exist, it behaves like upload with a new document.
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return document index result
     */
    public DocumentIndexResult mergeOrUploadDocuments(Iterable<?> documents) {
        return this.mergeOrUploadDocumentsWithResponse(documents).value();
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index.
     * If the document does not exist, it behaves like upload with a new document.
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @return response containing a document index result
     */
    @SuppressWarnings("unchecked")
    public Response<DocumentIndexResult> mergeOrUploadDocumentsWithResponse(Iterable<?> documents) {
        return this.indexWithResponse(new IndexBatch().addMergeOrUploadAction(documents));
    }

    /**
     * Deletes a collection of documents from the target index
     *
     * @param documents collection of documents to delete from the target Index.
     * @return document index result.
     */
    public DocumentIndexResult deleteDocuments(Iterable<?> documents) {
        return this.deleteDocumentsWithResponse(documents).value();
    }

    /**
     * Deletes a collection of documents from the target index
     *
     * @param documents collection of documents to delete from the target Index.
     * @return response containing a document index result.
     */
    @SuppressWarnings("unchecked")
    public Response<DocumentIndexResult> deleteDocumentsWithResponse(Iterable<?> documents) {
        return this.indexWithResponse(new IndexBatch().addDeleteAction(documents));
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
        return this.getDocumentCountWithResponse().value();
    }

    /**
     * Gets the number of documents
     *
     * @return response containing the number of documents.
     */
    public Response<Long> getDocumentCountWithResponse() {
        Mono<Response<Long>> result = asyncClient.getDocumentCountWithResponse();
        return blockWithOptionalTimeout(result, null);
    }

    /**
     * Searches for documents in the Azure Search index
     *
     * @return A {@link PagedIterable} of SearchResults
     */
    public PagedIterable<SearchResult> search() {
        PagedFlux<SearchResult> result = asyncClient.search();
        return new PagedIterable<>(result);
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
        PagedFlux<SearchResult> result = asyncClient.search(searchText, searchParameters, searchRequestOptions);
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
        return blockWithOptionalTimeout(results, null);
    }

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key document key
     * @param selectedFields selected fields to return
     * @param searchRequestOptions search request options
     * @return document object
     */
    public Document getDocument(String key, List<String> selectedFields, SearchRequestOptions searchRequestOptions) {
        return this.getDocumentWithResponse(key, selectedFields, searchRequestOptions).value();
    }

    /**
     * Retrieves a document from the Azure Search index.
     *
     * @param key document key
     * @param selectedFields selected fields to return
     * @param searchRequestOptions search request options
     * @return response containing a document object
     */
    public Response<Document> getDocumentWithResponse(String key, List<String> selectedFields,
                                            SearchRequestOptions searchRequestOptions) {
        Mono<Response<Document>> results = asyncClient.getDocumentWithResponse(key,
            selectedFields,
            searchRequestOptions);
        return blockWithOptionalTimeout(results, null);
    }

    /**
     * Suggests documents in the Azure Search index that match the given partial query text.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return suggests result
     */
    public PagedIterable<SuggestResult> suggest(String searchText, String suggesterName) {
        PagedFlux<SuggestResult> result = asyncClient.suggest(searchText, suggesterName);
        return new PagedIterable<>(result);
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
        PagedFlux<SuggestResult> result = asyncClient.suggest(searchText,
            suggesterName,
            suggestParameters,
            searchRequestOptions);
        return new PagedIterable<>(result);
    }

    /**
     * Sends a batch of document write to the Azure Search index.
     *
     * @param batch batch of documents to send to the index with the requested action
     * @return document index result
     */
    public DocumentIndexResult index(IndexBatch<?> batch) {
        return this.indexWithResponse(batch).value();
    }

    /**
     * Sends a batch of document write to the Azure Search index.
     *
     * @param batch batch of documents to send to the index with the requested action
     * @return a response containing a document index result
     */
    public Response<DocumentIndexResult> indexWithResponse(IndexBatch<?> batch) {
        Mono<Response<DocumentIndexResult>> results = asyncClient.indexWithResponse(batch);
        return blockWithOptionalTimeout(results, null);
    }

    /**
     * Autocompletes incomplete query terms based on input text and matching terms in the Azure Search index.
     *
     * @param searchText search text
     * @param suggesterName suggester name
     * @return auto complete result
     */
    public PagedIterable<AutocompleteItem> autocomplete(String searchText, String suggesterName) {
        PagedFlux<AutocompleteItem> result = asyncClient.autocomplete(searchText, suggesterName, null, null);
        return new PagedIterable<>(result);
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
        PagedFlux<AutocompleteItem> result = asyncClient.autocomplete(searchText,
                                                                    suggesterName,
                                                                    searchRequestOptions,
                                                                    autocompleteParameters);
        return new PagedIterable<>(result);
    }

    private <T> T blockWithOptionalTimeout(Mono<T> response, @Nullable Duration timeout) {
        if (timeout == null) {
            return response.block();
        } else {
            return response.block(timeout);
        }
    }
}
