// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.util.Configuration;

/**
 * Get a single document based on its key
 * This sample is based on the hotels-sample index available to install from the portal.
 * See https://docs.microsoft.com/en-us/azure/search/search-get-started-portal
 */
public class GetSingleDocumentExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_API_KEY");

    public static void main(String[] args) {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .serviceEndpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildClient();

        // Retrieve a single document by key
        Document document = client.getDocument("3");

        for (String key : document.keySet()) {
            System.out.println(key + ":" + document.get(key));
        }
    }
}
