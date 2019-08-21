// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.common.DocumentResponseConversions;
import com.azure.search.data.common.SearchPagedResponse;
import com.azure.search.data.generated.SearchIndexRestClient;
import com.azure.search.data.generated.implementation.SearchIndexRestClientBuilder;
import com.azure.search.data.generated.models.AutocompleteParameters;
import com.azure.search.data.generated.models.AutocompleteResult;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequest;
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
     * The Http Client to be used.
     */
    private HttpClient httpClient;

    /**
     * The Http Client to be used.
     */
    private List<HttpPipelinePolicy> policies;

    private Integer skip;


    /**
     * The underlying REST client to be used to actually interact with the Search service
     */
    private SearchIndexRestClient restClient;

    /**
     * Package private constructor to be used by {@link SearchIndexClientBuilder}
     */
    SearchIndexAsyncClientImpl(
        String searchServiceName, String searchDnsSuffix, String indexName, String apiVersion,
        HttpClient httpClient,
        List<HttpPipelinePolicy> policies) {
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
        if (httpClient == null) {
            throw new IllegalArgumentException("Invalid httpClient");
        }
        if (policies == null) {
            throw new IllegalArgumentException("Invalid policies");
        }

        this.searchServiceName = searchServiceName;
        this.searchDnsSuffix = searchDnsSuffix;
        this.indexName = indexName;
        this.apiVersion = apiVersion;
        this.httpClient = httpClient;
        this.policies = policies;
        this.skip = 0;

        initialize();
    }

    private void initialize() {
        restClient = new SearchIndexRestClientBuilder()
            .searchServiceName(searchServiceName)
            .indexName(indexName)
            .searchDnsSuffix(searchDnsSuffix)
            .apiVersion(apiVersion)
            .pipeline(new HttpPipelineBuilder()
                .httpClient(httpClient)
                .policies(policies.toArray(new HttpPipelinePolicy[0])).build())
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
        Mono<PagedResponse<SearchResult>> first = restClient.documents()
            .searchPostWithRestResponseAsync(new SearchRequest())
            .map(res -> {
                if (res.value().nextPageParameters() != null) {
                    skip = res.value().nextPageParameters().skip();
                }
                return new SearchPagedResponse(res);
            });
        return new PagedFlux<>(() -> first, this::searchPostNextWithRestResponseAsync);

    }

    @Override
    public PagedFlux<SearchResult> search(String searchText, SearchParameters searchParameters, SearchRequestOptions searchRequestOptions) {
        Mono<PagedResponse<SearchResult>> first = restClient.documents()
            .searchPostWithRestResponseAsync(createSearchRequest(searchText, searchParameters), searchRequestOptions)
            .map(res -> {
                if (res.value().nextPageParameters() != null) {
                    skip = res.value().nextPageParameters().skip();
                }
                return new SearchPagedResponse(res);
            });
        return new PagedFlux(() -> first, nextLink -> searchPostNextWithRestResponseAsync((String) nextLink));
    }

    @Override
    public Mono<Map<String, Object>> getDocument(String key) {
        return restClient
            .documents()
            .getAsync(key)
            .map(DocumentResponseConversions::convertLinkedHashMapToMap)
            .map(DocumentResponseConversions::dropUnnecessaryFields)
            .onErrorMap(DocumentResponseConversions::exceptionMapper)
            .doOnSuccess(s -> System.out.println("Document with key: " + key + " was retrieved succesfully"))
            .doOnError(e -> System.out.println("An error occured in getDocument(key): " + e.getMessage()));
    }

    @Override
    public Mono<Map<String, Object>> getDocument(
        String key, List<String> selectedFields,
        SearchRequestOptions searchRequestOptions) {
        return restClient
            .documents()
            .getAsync(key, selectedFields, searchRequestOptions)
            .map(DocumentResponseConversions::convertLinkedHashMapToMap)
            .map(DocumentResponseConversions::dropUnnecessaryFields)
            .onErrorMap(DocumentResponseConversions::exceptionMapper)
            .doOnSuccess(s -> System.out.println("Document with key: " + key + "and selectedFields: " + selectedFields.toString()  + " was retrieved succesfully"))
            .doOnError(e -> System.out.println("An error occured in getDocument(key, selectedFields, searchRequestOptions): " + e.getMessage()));
    }

    @Override
    public PagedFlux<SuggestResult> suggest(String searchText, String suggesterName) {
        return null;
    }

    @Override
    public PagedFlux<SuggestResult> suggest(
        String searchText,
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
    public Mono<AutocompleteResult> autocomplete(
        String searchText,
        String suggesterName,
        SearchRequestOptions searchRequestOptions,
        AutocompleteParameters autocompleteParameters) {
        return restClient.documents().autocompleteGetAsync(
            searchText,
            suggesterName,
            searchRequestOptions,
            autocompleteParameters);
    }

    /**
     * Search for next page
     *
     * @param nextLink next page link
     * @return {@link Mono}{@code <}{@link PagedResponse}{@code <}{@link SearchResult}{@code >}{@code >} next page
     * response with results
     */
    private Mono<PagedResponse<SearchResult>> searchPostNextWithRestResponseAsync(String nextLink) {
        if (nextLink == null || nextLink.isEmpty()) {
            return Mono.empty();
        }
        if (skip == null) {
            return Mono.empty();
        }
        return restClient.documents()
            .searchPostWithRestResponseAsync(new SearchRequest().skip(skip))
            .map(res -> {
                if (res.value().nextPageParameters() == null || res.value().nextPageParameters().skip() == null) {
                    skip = null;
                } else {
                    skip = res.value().nextPageParameters().skip();
                }
                return new SearchPagedResponse(res);
            });
    }


    /**
     * Create search request from search text and parameters
     *
     * @param searchText search text
     * @param searchParameters search parameters
     * @return SearchRequest
     */
    private SearchRequest createSearchRequest(String searchText, SearchParameters searchParameters) {
        SearchRequest searchRequest = new SearchRequest().searchText(searchText);
        if (searchParameters != null) {
            searchRequest.
                searchMode(searchParameters.searchMode()).
                facets(searchParameters.facets()).
                filter(searchParameters.filter()).
                highlightPostTag(searchParameters.highlightPostTag()).
                highlightPreTag(searchParameters.highlightPreTag()).
                includeTotalResultCount(searchParameters.includeTotalResultCount()).
                minimumCoverage(searchParameters.minimumCoverage()).
                queryType(searchParameters.queryType()).
                scoringParameters(searchParameters.scoringParameters()).
                scoringProfile(searchParameters.scoringProfile()).
                skip(searchParameters.skip()).
                top(searchParameters.top());
            if (searchParameters.highlightFields() != null) {
                searchRequest.highlightFields(String.join(",", searchParameters.highlightFields()));
            }
            if (searchParameters.searchFields() != null) {
                searchRequest.searchFields(String.join(",", searchParameters.searchFields()));
            }
            if (searchParameters.orderBy() != null) {
                searchRequest.orderBy(String.join(",", searchParameters.orderBy()));
            }
            if (searchParameters.select() != null) {
                searchRequest.select(String.join(",", searchParameters.select()));
            }
        }

        return searchRequest;
    }
}
