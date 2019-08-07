package com.azure.search.data.customization;

import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.SearchIndexClientBuilder;

/**
 * Fluent SearchIndexClientBuilder for instantiating a {@link SearchIndexClientImpl} or a {@link SearchIndexASyncClientImpl}
 * using {@link SearchIndexClientBuilder#buildClient()} or {@link SearchIndexClientBuilder#buildAsyncClient()}
 *
 * <p>
 * The following information must be provided on this builder:
 *
 * <ul>
 * <li>the search service name through {@code .serviceName()}
 * <li>the index name through {@code .indexName()}
 * <li>the credentials through {@code .credentials()}
 * </ul>
 *
 * <p>
 * Once all the configurations are set on this builder, call {@code .buildClient()} to create a
 * {@link SearchIndexClientBuilder}
 */
public class SearchIndexClientBuilderImpl implements SearchIndexClientBuilder {

    private String apiVersion;
    private String serviceName;
    private String indexName;
    private SearchPipelinePolicy policy;
    private String searchDnsSuffix;

    public SearchIndexClientBuilderImpl() {
        searchDnsSuffix = "search.windows.net";
    }

    /**
     * Sets the api version to work against
     *
     * @param apiVersion api version
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilderImpl apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * Sets search service name
     *
     * @param serviceName name of the service
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilderImpl serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Sets the index name
     *
     * @param indexName name of the index
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilderImpl indexName(String indexName) {
        this.indexName = indexName;
        return this;
    }

    /**
     * Set the authentication policy (api-key)
     *
     * @param policy value of api-key
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilderImpl policy(SearchPipelinePolicy policy) {
        this.policy = policy;
        return this;
    }

    /**
     * Set search service dns suffix
     *
     * @param searchDnsSuffix search service dns suffix
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilderImpl searchDnsSuffix(String searchDnsSuffix) {
        this.searchDnsSuffix = searchDnsSuffix;
        return this;
    }

    /**
     * @return a {@link SearchIndexClientBuilder} created from the configurations in this builder.
     */
    public SearchIndexClientImpl buildClient() {
        return new SearchIndexClientImpl(serviceName, searchDnsSuffix, indexName, apiVersion, policy);
    }

    /**
     * @return a {@link SearchIndexClientBuilder} created from the configurations in this builder.
     */
    public SearchIndexASyncClientImpl buildAsyncClient() {
        return new SearchIndexASyncClientImpl(serviceName, searchDnsSuffix, indexName, apiVersion, policy);
    }
}
