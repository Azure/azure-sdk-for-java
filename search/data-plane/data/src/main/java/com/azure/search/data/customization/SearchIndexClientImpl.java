package com.azure.search.data.customization;

import com.azure.core.http.rest.PagedFlux;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.generated.models.*;
import reactor.core.publisher.Mono;

import java.util.List;

public class SearchIndexClientImpl implements SearchIndexClient {
    @Override
    public String getApiVersion() {
        return null;
    }

    @Override
    public SearchIndexClient setApiVersion(String apiVersion) {
        return null;
    }

    @Override
    public String getSearchServiceName() {
        return null;
    }

    @Override
    public SearchIndexClient setSearchServiceName(String searchServiceName) {
        return null;
    }

    @Override
    public String getSearchDnsSuffix() {
        return null;
    }

    @Override
    public SearchIndexClient setSearchDnsSuffix(String searchDnsSuffix) {
        return null;
    }

    @Override
    public String getIndexName() {
        return null;
    }

    @Override
    public SearchIndexClient setIndexName(String indexName) {
        return null;
    }

    @Override
    public Mono<Long> countDocuments() {
        return null;
    }

    @Override
    public PagedFlux<SearchResult> search() {
        return null;
    }

    @Override
    public PagedFlux<SearchResult> search(String searchText, SearchParameters searchParameters, SearchRequestOptions searchRequestOptions) {
        return null;
    }

    @Override
    public Mono<Object> getDocument(String key) {
        return null;
    }

    @Override
    public Mono<Object> getDocument(String key, List<String> selectedFields, SearchRequestOptions searchRequestOptions) {
        return null;
    }

    @Override
    public PagedFlux<SuggestResult> suggest(String searchText, String suggesterName) {
        return null;
    }

    @Override
    public PagedFlux<SuggestResult> suggest(String searchText, String suggesterName, SuggestParameters suggestParameters, SearchRequestOptions searchRequestOptions) {
        return null;
    }

    @Override
    public Mono<DocumentIndexResult> index(IndexBatch batch) {
        return null;
    }

    @Override
    public Mono<AutocompleteResult> autocomplete(String searchText, String suggesterName) {
        return null;
    }

    @Override
    public Mono<AutocompleteResult> autocomplete(String searchText, String suggesterName, SearchRequestOptions searchRequestOptions, AutocompleteParameters autocompleteParameters) {
        return null;
    }
}
