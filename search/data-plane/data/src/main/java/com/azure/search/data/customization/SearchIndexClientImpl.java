package com.azure.search.data.customization;

import com.azure.core.http.rest.PagedFlux;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.common.credentials.SearchCredentials;
import com.azure.search.data.generated.Documents;
import com.azure.search.data.generated.SearchIndexRestClient;
import com.azure.search.data.generated.implementation.SearchIndexRestClientBuilder;
import com.azure.search.data.generated.models.*;
import com.microsoft.azure.AzureServiceClient;
import reactor.core.publisher.Mono;

import java.util.List;

public class SearchIndexClientImpl extends AzureServiceClient implements SearchIndexClient {

    /** Client Api Version. */
    private String apiVersion;

    /** The name of the Azure Search service. */
    private String searchServiceName;

    /** The DNS suffix of the Azure Search service. The default is search.windows.net. */
    private String searchDnsSuffix;

    /** The name of the Azure Search index. */
    private String indexName;

    /** Search Service Index Rest Client */
    private SearchIndexRestClient indexRestClient;

    /**
     * Initializes an instance of SearchIndexClient client.
     *
     * @param searchServiceName name of the Azure Search Service
     * @param indexName name of the Azure Search Index
     * @param credentials the management credentials for Azure
     */
    public SearchIndexClientImpl(String searchServiceName, String indexName, String searchDnsSuffix, SearchCredentials credentials) {
        super(String.format("https://%s.%s/indexes('%s')/", searchServiceName, searchDnsSuffix, indexName), credentials);

        this.apiVersion = "2019-05-06";
        this.searchDnsSuffix = "search.windows.net";
        this.searchServiceName = searchServiceName;
        this.indexName = indexName;

        indexRestClient = new SearchIndexRestClientBuilder().searchServiceName(searchServiceName).searchDnsSuffix(searchDnsSuffix).indexName(indexName).build();
    }

    /**
     * Gets the Documents object to access its operations.
     *
     * @return the Documents object.
     */
    private Documents documents() {
        return indexRestClient.documents();
    }

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    @Override
    public String getApiVersion() {
        return this.apiVersion;
    }


    /**
     * Gets The name of the Azure Search service.
     *
     * @return the searchServiceName value.
     */
    @Override
    public String getSearchServiceName() {
        return this.searchServiceName;
    }

    /**
     * Sets The name of the Azure Search service.
     *
     * @param searchServiceName the searchServiceName value.
     * @return the service client itself
     */
    @Override
    public SearchIndexClient setSearchServiceName(String searchServiceName) {

        this.searchServiceName = searchServiceName;
        return this;
    }

    /**
     * Gets The DNS suffix of the Azure Search service. The default is search.windows.net.
     *
     * @return the searchDnsSuffix value.
     */
    @Override
    public String getSearchDnsSuffix() {
        return this.searchDnsSuffix;
    }

    /**
     * Sets The DNS suffix of the Azure Search service. The default is search.windows.net.
     *
     * @param searchDnsSuffix the searchDnsSuffix value.
     * @return the service client itself
     */
    @Override
    public SearchIndexClient setSearchDnsSuffix(String searchDnsSuffix) {
        this.searchDnsSuffix = searchDnsSuffix;
        return this;
    }

    /**
     * Gets The name of the Azure Search index.
     *
     * @return the indexName value.
     */
    @Override
    public String getIndexName() {
        return this.indexName;
    }

    /**
     * Sets The name of the Azure Search index.
     *
     * @param indexName the indexName value.
     * @return the service client itself
     */
    @Override
    public SearchIndexClient setIndexName(String indexName) {
        this.indexName = indexName;
        return this;
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
