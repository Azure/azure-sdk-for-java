// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.util.Configuration;

/**
 * This example shows how to get a single document based on its key
 */
public class GetSingleDocumentExample {
    /*
     * From the Azure portal, get your Azure Cognitive Search service name and API key.
     */
    private static final String SEARCH_SERVICE = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_SERVICE");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_API_KEY");

    public static void main(String[] args) {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .serviceName(SEARCH_SERVICE)
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
