// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.generated.models.AutocompleteParameters;
import com.azure.search.data.generated.models.AutocompleteResult;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.DocumentSearchResult;
import com.azure.search.data.generated.models.DocumentSuggestResult;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SuggestParameters;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;
import java.time.Duration;

import java.util.List;
import java.util.Map;

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
    public DocumentSearchResult search() {
        //PagedFlux<DocumentSearchResult> result = asyncClient.search();
        //return blockWithOptionalTimeout(result, null);
        return null;
    }

    @Override
    public DocumentSearchResult search(String searchText, SearchParameters searchParameters, SearchRequestOptions searchRequestOptions) {
        //PagedFlux<DocumentSearchResult> result = asyncClient.search(searchText, searchParameters, searchRequestOptions);
        //return blockWithOptionalTimeout(result, null);
        return null;
    }

    @Override
    public Map<String, Object> getDocument(String key) {
        return asyncClient.getDocument(key).block();
    }

    @Override
    public Map<String, Object> getDocument(String key, List<String> selectedFields, SearchRequestOptions searchRequestOptions) {
        return asyncClient.getDocument(key, selectedFields, searchRequestOptions).block();
    }

    @Override
    public DocumentSuggestResult suggest(String searchText, String suggesterName) {

        return null;
    }

    @Override
    public DocumentSuggestResult suggest(String searchText, String suggesterName, SuggestParameters suggestParameters, SearchRequestOptions searchRequestOptions) {
        return null;
    }

    @Override
    public DocumentIndexResult index(IndexBatch batch) {

        return null;
    }

    @Override
    public AutocompleteResult autocomplete(String searchText, String suggesterName) {

        return null;
    }

    @Override
    public AutocompleteResult autocomplete(String searchText, String suggesterName, SearchRequestOptions searchRequestOptions, AutocompleteParameters autocompleteParameters) {
        return null;
    }

    private <T> T blockWithOptionalTimeout(Mono<T> response, @Nullable Duration timeout) {
        if (timeout == null) {
            return response.block();
        } else {
            return response.block(timeout);
        }
    }
}
