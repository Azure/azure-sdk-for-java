// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.common.DocumentResponseConversions;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.search.data.generated.SearchIndexRestClient;
import com.azure.search.data.generated.implementation.SearchIndexRestClientBuilder;
import com.azure.search.data.generated.models.AutocompleteParameters;
import com.azure.search.data.generated.models.AutocompleteResult;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import com.azure.search.data.generated.models.SuggestParameters;
import com.azure.search.data.generated.models.SuggestResult;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class SearchIndexAsyncClientImpl extends SearchIndexBaseClient implements SearchIndexAsyncClient {

    /**
     * Search Service dns suffix
     */
    private String searchDnsSuffix;

    /**
     * Search REST API Version
     */
    private String apiVersion;

    /**
     * The name of the Azure Search service.
     */
    private String searchServiceName;

    /**
     * The name of the Azure Search index.
     */
    private String indexName;

    /**
     * The underlying REST client to be used to actually interact with the Search service
     */
    private SearchIndexRestClient restClient;

    /**
     * Package private constructor to be used by {@link SearchIndexClientBuilder}
     * @param searchServiceName
     * @param searchDnsSuffix
     * @param indexName
     * @param apiVersion
     * @param policy
     */
    SearchIndexAsyncClientImpl(String searchServiceName, String searchDnsSuffix, String indexName, String apiVersion, SearchPipelinePolicy policy) {
        if (StringUtils.isBlank(searchServiceName)) {
            throw new IllegalArgumentException("Invalid searchServiceName");
        }
        if (StringUtils.isBlank(searchDnsSuffix)) {
            throw new IllegalArgumentException("Invalid searchDnsSuffix");
        }
        if (StringUtils.isBlank(indexName)) {
            throw new IllegalArgumentException("Invalid indexName");
        }
        if (StringUtils.isBlank(apiVersion)) {
            throw new IllegalArgumentException("Invalid apiVersion");
        }
        if (policy == null) {
            throw new IllegalArgumentException("Invalid policy");
        }

        this.searchServiceName = searchServiceName;
        this.searchDnsSuffix = searchDnsSuffix;
        this.indexName = indexName;
        this.apiVersion = apiVersion;

        initialize(policy);
    }

    private void initialize(SearchPipelinePolicy policy) {
        restClient = new SearchIndexRestClientBuilder()
            .searchServiceName(searchServiceName)
            .indexName(indexName)
            .searchDnsSuffix(searchDnsSuffix)
            .apiVersion(apiVersion)
            .pipeline(new HttpPipelineBuilder().policies(policy).build())
            .build();
    }

    @Override
    public String getIndexName() {
        return this.indexName;
    }

    @Override
    public SearchIndexAsyncClientImpl setIndexName(String indexName) {
        this.indexName = indexName;
        restClient.setIndexName(indexName);
        return this;
    }

    @Override
    public String getApiVersion() {
        return this.apiVersion;
    }

    @Override
    public String getSearchDnsSuffix() {
        return this.searchDnsSuffix;
    }

    @Override
    public String getSearchServiceName() {
        return this.searchServiceName;
    }

    @Override
    public Mono<Long> countDocuments() {
        return restClient.documents().countAsync();
    }

    @Override
    public PagedFlux<SearchResult> search() {
        return null; //return restClient.documents().searchGetAsync();
    }

    @Override
    public PagedFlux<SearchResult> search(String searchText,
                                          SearchParameters searchParameters,
                                          SearchRequestOptions searchRequestOptions) {
        return null; //return restClient.documents().searchGetAsync(searchText, searchParameters, searchRequestOptions);
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
