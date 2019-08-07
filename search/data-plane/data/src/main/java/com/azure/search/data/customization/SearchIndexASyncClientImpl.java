package com.azure.search.data.customization;

import com.azure.search.data.common.DocumentResponseConversions;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.search.data.SearchIndexASyncClient;
import com.azure.search.data.generated.models.*;
import reactor.core.publisher.Mono;

import java.util.*;

public class SearchIndexASyncClientImpl extends SearchIndexBaseClientImpl implements SearchIndexASyncClient {

    public SearchIndexASyncClientImpl(String searchServiceName, String searchDnsSuffix, String indexName, String apiVersion, SearchPipelinePolicy policy) {
        super(searchServiceName, searchDnsSuffix, indexName, apiVersion, policy);
    }

    @Override
    public SearchIndexASyncClient setIndexName(String indexName) {
        this.setIndexNameInternal(indexName);
        return this;
    }

    @Override
    public Mono<Long> countDocuments() {
        return restClient.documents().countAsync();
    }

    @Override
    public PagedFlux<SearchResult> search() {
        return null;
    }

    @Override

    public PagedFlux<SearchResult> search(String searchText,
                                          SearchParameters searchParameters,
                                          SearchRequestOptions searchRequestOptions) {
        // trigger an async get request against the rest client
        return null;//restClient.documents().searchGetAsync(searchText, searchParameters, searchRequestOptions);
    }

    @Override
    public Mono<Map<String, Object>> getDocument(String key) {
        return restClient.documents().getAsync(key).map(DocumentResponseConversions::convertLinkedHashMapToMap);
    }

    @Override
    public Mono<Map<String, Object>> getDocument(String key, List<String> selectedFields,
                                                 SearchRequestOptions searchRequestOptions) {
        return restClient
            .documents()
            .getAsync(key, selectedFields, searchRequestOptions)
            .map(DocumentResponseConversions::convertLinkedHashMapToMap);
    }



    @Override
    public PagedFlux<SuggestResult> suggest(String searchText, String suggesterName) {
        return null;
    }

    @Override
    public PagedFlux<SuggestResult> suggest(String searchText,
                                            String suggesterName,
                                            SuggestParameters suggestParameters,
                                            SearchRequestOptions searchRequestOptions) {
        return null;
    }

    @Override
    public Mono<DocumentIndexResult> index(IndexBatch batch) {
        return restClient.documents().indexAsync(batch);
    }

    @Override
    public Mono<AutocompleteResult> autocomplete(String searchText, String suggesterName) {
        return restClient.documents().autocompleteGetAsync(searchText, suggesterName);
    }

    @Override
    public Mono<AutocompleteResult> autocomplete(String searchText,
                                                 String suggesterName,
                                                 SearchRequestOptions searchRequestOptions,
                                                 AutocompleteParameters autocompleteParameters) {
        return restClient.documents().autocompleteGetAsync(searchText,
            suggesterName,
            searchRequestOptions,
            autocompleteParameters);
    }
}
