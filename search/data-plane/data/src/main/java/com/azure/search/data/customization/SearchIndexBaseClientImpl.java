package com.azure.search.data.customization;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.generated.SearchIndexRestClient;
import com.azure.search.data.generated.implementation.SearchIndexRestClientBuilder;
import org.apache.commons.lang3.StringUtils;

public abstract class SearchIndexBaseClientImpl {

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
    protected SearchIndexRestClient restClient;

    public SearchIndexBaseClientImpl(String searchServiceName, String searchDnsSuffix, String indexName, String apiVersion, SearchPipelinePolicy policy) {
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

        restClient = new SearchIndexRestClientBuilder()
                .searchServiceName(searchServiceName)
                .indexName(indexName)
                .searchDnsSuffix(searchDnsSuffix)
                .apiVersion(apiVersion)
                .pipeline(new HttpPipelineBuilder().policies(policy).build())
                .build();
    }

    /**
     * Gets The name of the Azure Search index.
     *
     * @return the indexName value.
     */
    public String getIndexName() {
        return restClient.getIndexName();
    }

    public String getApiVersion() {
        return restClient.getApiVersion();
    }

    public String getSearchDnsSuffix() {
        return restClient.getSearchDnsSuffix();
    }

    public String getSearchServiceName() {
        return restClient.getSearchServiceName();
    }

    protected void setApiVersionInternal(String apiVersion) {
        restClient = restClient.setApiVersion(apiVersion);
    }

    protected void setSearchServiceNameInternal(String searchServiceName) {
        restClient = restClient.setSearchServiceName(searchServiceName);
    }

    protected void setSearchDnsSuffixInternal(String searchDnsSuffix) {
        restClient = restClient.setSearchDnsSuffix(searchDnsSuffix);
    }

    protected void setIndexNameInternal(String indexName) {
        restClient = restClient.setIndexName(indexName);
    }

}
