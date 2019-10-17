// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.search.models.SearchParameters;

/**
 * An example that demonstrates how to read exceptions when a
 * Search Service returns a non successful response (3xx/4xx/5xx).
 *
 * This sample makes search request to the service,
 * applying a "filter" expression containing an invalid index field.
 *
 * NOTE: It is assumed you have access to an Azure Search service
 * and have created an index called "hotels"
 */
public class HttpResponseExceptionExample {
    public static void main(String[] args) {
        searchSync();
        searchAsync();
    }

    /**
     * Handling errors with a sync client
     */
    private static void searchSync() {
        SearchIndexClient searchClient = getSearchClient();
        try {
            SearchParameters searchParams = new SearchParameters()
                .setFilter("Non_Existent_Field eq 'Luxury'");

            searchClient.search("hotel", searchParams, null);
        }
        catch (HttpResponseException ex) {
            HttpResponse response = ex.getResponse();
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Message: " + response.getBodyAsString().block());
        }
    }

    private static SearchIndexClient getSearchClient() {
        String apiKey = "<apiKey>";
        String searchServiceName = "<searchServiceName>";

        String dnsSuffix = "search.windows.net";
        String indexName = "hotels";

        ApiKeyCredentials apiKeyCredentials = new ApiKeyCredentials(apiKey);
        return new SearchIndexClientBuilder()
            .serviceName(searchServiceName)
            .searchDnsSuffix(dnsSuffix)
            .indexName(indexName)
            .credential(apiKeyCredentials)
            .buildClient();
    }

    /**
     * Handling errors with an async client
     */
    private static void searchAsync() {
        SearchIndexAsyncClient client = getSearchAsyncClient();
        SearchParameters searchParams = new SearchParameters()
            .setFilter("Non_Existent_Field eq 'Luxury'");

        client.search("hotel", searchParams, null)
            .doOnError(e -> {
                HttpResponse response = ((HttpResponseException) e).getResponse();
                System.out.println("Status Code: " + response.getStatusCode());
                System.out.println("Message: " + response.getBodyAsString().block());
            });
    }

    private static SearchIndexAsyncClient getSearchAsyncClient() {
        String apiKey = "<apiKey>";
        String searchServiceName = "<searchServiceName>";

        String dnsSuffix = "search.windows.net";
        String indexName = "hotels";

        ApiKeyCredentials apiKeyCredentials = new ApiKeyCredentials(apiKey);
        return new SearchIndexClientBuilder()
            .serviceName(searchServiceName)
            .searchDnsSuffix(dnsSuffix)
            .indexName(indexName)
            .credential(apiKeyCredentials)
            .buildAsyncClient();
    }
}
