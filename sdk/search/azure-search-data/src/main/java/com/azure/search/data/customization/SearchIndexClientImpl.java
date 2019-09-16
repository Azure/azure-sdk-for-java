// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.SearchIndexClient;
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

public class SearchIndexClientImpl extends SearchIndexBaseClient implements SearchIndexClient {

    private SearchIndexAsyncClientImpl asyncClient;

    /**
     * Package private constructor to be used by {@link SearchIndexClientBuilder}
     * @param searchIndexAsyncClient Async SearchIndex Client
     */
    public SearchIndexClientImpl(SearchIndexAsyncClient searchIndexAsyncClient) {
        this.asyncClient = (SearchIndexAsyncClientImpl) searchIndexAsyncClient;
    }

    @Override
    public String getIndexName() {
        return asyncClient.getIndexName();
    }

    @Override
    public <T> DocumentIndexResult uploadDocuments(List<T> documents) {
        return this.index(new IndexBatchBuilder().upload(documents).build());
    }

    @Override
    public String getApiVersion() {

        return asyncClient.getApiVersion();
    }

    @Override
    public String getSearchDnsSuffix() {

        return asyncClient.getSearchDnsSuffix();
    }

    @Override
    public String getSearchServiceName() {
        return asyncClient.getSearchServiceName();
    }

    @Override
    public SearchIndexClientImpl setIndexName(String indexName) {
        asyncClient.setIndexName(indexName);
        return this;
    }

    @Override
    public Long countDocuments() {
        Mono<Long> result = asyncClient.countDocuments();
        return blockWithOptionalTimeout(result, null);
    }

    @Override
    public PagedIterable<SearchResult> search() {
        PagedFlux<SearchResult> result = asyncClient.search();
        return new PagedIterable<>(result);
    }

    @Override
    public PagedIterable<SearchResult> search(String searchText, SearchParameters searchParameters, SearchRequestOptions searchRequestOptions) {
        PagedFlux<SearchResult> result = asyncClient.search(searchText, searchParameters, searchRequestOptions);
        return new PagedIterable<>(result);
    }

    @Override
    public Document getDocument(String key) {
        Mono<Document> results = asyncClient.getDocument(key);
        return blockWithOptionalTimeout(results, null);
    }

    @Override
    public Document getDocument(String key, List<String> selectedFields, SearchRequestOptions searchRequestOptions) {
        Mono<Document> results = asyncClient.getDocument(key, selectedFields, searchRequestOptions);
        return blockWithOptionalTimeout(results, null);
    }

    @Override
    public PagedIterable<SuggestResult> suggest(String searchText, String suggesterName) {
        PagedFlux<SuggestResult> result = asyncClient.suggest(searchText, suggesterName);
        return new PagedIterable<>(result);
    }

    @Override
    public PagedIterable<SuggestResult> suggest(String searchText, String suggesterName, SuggestParameters suggestParameters, SearchRequestOptions searchRequestOptions) {
        PagedFlux<SuggestResult> result = asyncClient.suggest(searchText, suggesterName, suggestParameters, searchRequestOptions);
        return new PagedIterable<>(result);
    }

    @Override
    public DocumentIndexResult index(IndexBatch batch) {
        Mono<DocumentIndexResult> results = asyncClient.index(batch);
        return blockWithOptionalTimeout(results, null);
    }

    @Override
    public PagedIterable<AutocompleteItem> autocomplete(String searchText, String suggesterName) {
        PagedFlux<AutocompleteItem> result = asyncClient.autocomplete(searchText, suggesterName, null, null);
        return new PagedIterable<>(result);
    }

    @Override
    public PagedIterable<AutocompleteItem> autocomplete(String searchText, String suggesterName, SearchRequestOptions searchRequestOptions, AutocompleteParameters autocompleteParameters) {
        PagedFlux<AutocompleteItem> result = asyncClient.autocomplete(searchText, suggesterName, searchRequestOptions, autocompleteParameters);
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
