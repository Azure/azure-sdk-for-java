package com.azure.search.data.customization;

import com.azure.search.data.common.credentials.SearchCredentials;

public class SearchIndexClientImplAsync extends SearchIndexClientImpl{

    /**
     * Initializes an instance of SearchIndexClient client.
     *
     * @param searchServiceName name of the Azure Search Service
     * @param indexName         name of the Azure Search Index
     * @param searchDnsSuffix
     * @param credentials       the management credentials for Azure
     */
    public SearchIndexClientImplAsync(String searchServiceName, String indexName, String searchDnsSuffix, SearchCredentials credentials) {
        super(searchServiceName, indexName, searchDnsSuffix, credentials);
    }
}
