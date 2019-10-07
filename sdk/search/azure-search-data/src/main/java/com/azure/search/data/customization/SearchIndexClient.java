// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.search.data.generated.models.AutocompleteItem;
import com.azure.search.data.generated.models.AutocompleteParameters;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import com.azure.search.data.generated.models.SuggestParameters;
import com.azure.search.data.generated.models.SuggestResult;
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
     * Uploads a document to the target index.
     *
     * @param document the document to upload to the target Index
     * @param <T> the type of object to serialize
     * @return document index result
     */
    public <T> DocumentIndexResult uploadDocument(T document) {
        return this.index(new IndexBatchBuilder<T>().upload(document).build());
    }

    /**
     * Uploads a collection of documents to the target index
     *
     * @param documents collection of documents to upload to the target Index.
     * @param <T> The type of object to serialize.
     * @return document index result.
     */
    public <T> DocumentIndexResult uploadDocuments(List<T> documents) {
        return this.index(new IndexBatchBuilder<T>().upload(documents).build());
    }

    /**
     * Merges a document with an existing document in the target index.
     *
     * @param document the document to be merged
     * @param <T> the type of object to serialize
     * @return document index result
     */
    public <T> DocumentIndexResult mergeDocument(T document) {
        return this.index(new IndexBatchBuilder<T>().merge(document).build());
    }

    /**
     * Merges a collection of documents with existing documents in the target index.
     *
     * @param documents collection of documents to be merged
     * @param <T> the type of object to serialize
     * @return document index result
     */
    public <T> DocumentIndexResult mergeDocuments(List<T> documents) {
        return this.index(new IndexBatchBuilder<T>().merge(documents).build());
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index.
     * If the document does not exist, it behaves like upload with a new document.
     *
     * @param document the document to be merged, if exists, otherwise uploaded as a new document
     * @param <T> the type of object to serialize
     * @return document index result
     */
    public <T> DocumentIndexResult mergeOrUploadDocument(T document) {
        return this.index(new IndexBatchBuilder<T>().mergeOrUpload(document).build());
    }

    /**
     * This action behaves like merge if a document with the given key already exists in the index.
     * If the document does not exist, it behaves like upload with a new document.
     *
     * @param documents collection of documents to be merged, if exists, otherwise uploaded
     * @param <T> the type of object to serialize
     * @return document index result
     */
    public <T> DocumentIndexResult mergeOrUploadDocuments(List<T> documents) {
        return this.index(new IndexBatchBuilder<T>().mergeOrUpload(documents).build());
    }

    /**
     * Deletes a document from the target index.
     * Note that any field you specify in a delete operation, other than the key field, will be ignored.
     *
     * @param document the document to delete from the target Index
     * @param <T> The type of object to serialize
     * @return document index result
     */
    public <T> DocumentIndexResult deleteDocument(T document) {
        return this.index(new IndexBatchBuilder<T>().delete(document).build());
    }

    /**
     * Deletes a collection of documents from the target index
     *
     * @param documents collection of documents to delete from the target Index.
     * @param <T> The type of object to serialize.
     * @return document index result.
     */
    public <T> DocumentIndexResult deleteDocuments(List<T> documents) {
        return this.index(new IndexBatchBuilder<T>().delete(documents).build());
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
    public Long countDocuments() {
        Mono<Long> result = asyncClient.getDocumentCount();
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
        Mono<Document> results = asyncClient.getDocument(key, selectedFields, searchRequestOptions);
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
     * @param <T> The type of document to be indexed
     * @return document index result
     */
    public <T> DocumentIndexResult index(IndexBatch<T> batch) {
        Mono<DocumentIndexResult> results = asyncClient.index(batch);
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
