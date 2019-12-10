// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.search.models.Indexer;
import com.azure.search.models.RequestOptions;

import java.util.List;

public class ListIndexersExample {

    /**
     * This example shows how to list all existing indexers in a Cognitive Search Service.
     * <p>
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */

    private static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration()
        .get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    public static void main(String[] args) {
        SearchServiceAsyncClient searchServiceClient = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .buildAsyncClient();

        listIndexers(searchServiceClient);
    }

    private static void listIndexers(SearchServiceAsyncClient searchServiceClient) {
        PagedResponse<Indexer> response = searchServiceClient.listIndexers("*",
            new RequestOptions()).byPage().blockFirst();

        if (response != null) {
            System.out.println(String.format("Response code: %s", response.getStatusCode()));

            List<Indexer> indexers = response.getValue();
            System.out.println("Found the following indexers:");
            for (Indexer indexer : indexers) {
                System.out.println(String.format("Indexer name: %s, ETag: %s", indexer.getName(), indexer.getETag()));
            }
        }
    }
}
