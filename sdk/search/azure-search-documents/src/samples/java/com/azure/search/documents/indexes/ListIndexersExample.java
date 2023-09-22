// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.search.documents.indexes.models.SearchIndexer;

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
        SearchIndexerAsyncClient indexerAsyncClient = new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildAsyncClient();

        listIndexers(indexerAsyncClient);
    }

    private static void listIndexers(SearchIndexerAsyncClient indexerAsyncClient) {
        PagedResponse<SearchIndexer> response = indexerAsyncClient.listIndexers(null)
            .byPage().blockFirst();

        if (response != null) {
            System.out.println(String.format("Response code: %s", response.getStatusCode()));

            List<SearchIndexer> indexers = response.getValue();
            System.out.println("Found the following indexers:");
            for (SearchIndexer indexer : indexers) {
                System.out.println(String.format("Indexer name: %s, ETag: %s", indexer.getName(), indexer.getETag()));
            }
        }
    }
}
