package com.azure.search.data.customization;

import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.common.credentials.SearchCredentials;

/**
 * The Fluent client builder.
 */
public class SearchIndexClientBuilder {

    private SearchCredentials credentials;
    private String serviceName;
    private String indexName;
    private String searchDnsSuffix;

    public SearchIndexClientBuilder() {
    }

    /**
     * Sets the credentials used to authorize requests sent to the service
     * @param credentials authorization credential
     * @return the updated StorageClientBuilder object
     */
    public SearchIndexClientBuilder credentials(SearchCredentials credentials) {
        this.credentials = credentials;
        return this;
    }

    /**
     * Sets search service name
     * @param serviceName name of the service
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Sets the index name
     * @param indexName name of the index
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder indexName(String indexName) {
        this.indexName = indexName;
        return this;
    }

    /**
     * Sets the service dns suffix
     * @param searchDnsSuffix service dns suffix
     * @return the updated SearchIndexClientBuilder object
     */
    public SearchIndexClientBuilder searchDnsSuffix(String searchDnsSuffix) {
        this.searchDnsSuffix = searchDnsSuffix;
        return this;
    }
    /**
     * @return a {@link SearchIndexClientBuilder} created from the configurations in this builder.
     */
    public SearchIndexClient buildClient()
    {
        return new SearchIndexClientImpl(serviceName, searchDnsSuffix, indexName, credentials);
    }

    /**
     * @return a {@link SearchIndexClientBuilder} created from the configurations in this builder.
     */
    public SearchIndexClient buildAsyncClient()
    {
        return new SearchIndexClientImplAsync(serviceName, searchDnsSuffix, indexName, credentials);
    }
}


