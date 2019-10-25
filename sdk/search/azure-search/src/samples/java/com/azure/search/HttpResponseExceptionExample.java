// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFluxBase;
import com.azure.core.util.Configuration;
import com.azure.search.common.SearchPagedResponse;
import com.azure.search.models.SearchOptions;
import com.azure.search.models.SearchResult;

/**
 * This example shows how to handle errors when the Azure Cognitive Search service
 * returns a non-successful response
 * <p>
 * This sample is based on the hotels-sample index available to install from the portal.
 * See https://docs.microsoft.com/en-us/azure/search/search-get-started-portal
 * </p>
 */
public class HttpResponseExceptionExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_API_KEY");

    public static void main(String[] args) {
        handleErrorsWithSyncClient();
        handleErrorsWithAsyncClient();
    }

    /**
     * With the sync client, HttpResponseExceptions are raised on failure
     */
    private static void handleErrorsWithSyncClient() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildClient();

        try {
            // Perform a search on a non-existent field
            SearchOptions searchOptions = new SearchOptions()
                .setFilter("Non_Existent_Field eq 'Luxury'");

            Iterable<SearchResult> results = client.search("hotel", searchOptions, null);

            for (SearchResult result : results) {
                // normal results processing
                System.out.printf("Found hotel: %s%n", result.getDocument().get("HotelName"));
            }
        } catch (HttpResponseException ex) {
            // The exception contains the HTTP status code and the detailed message
            // returned from the search service
            HttpResponse response = ex.getResponse();
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Message: " + response.getBodyAsString().block());
        }
    }

    /**
     * With the async client, errors need to be handled when subscribing to the stream
     */
    private static void handleErrorsWithAsyncClient() {
        SearchIndexAsyncClient client = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildAsyncClient();

        SearchOptions searchOptions = new SearchOptions()
            .setFilter("Non_Existent_Field eq 'Luxury'");

        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search("hotel", searchOptions, null);
        results
            .subscribe(
                foo -> {
                    // normal results processing
                    System.out.printf("Found hotel: %s%n", foo.getDocument().get("HotelName"));
                },
                err -> {
                    if (err instanceof HttpResponseException) {
                        // The exception contains the HTTP status code and the detailed message
                        // returned from the search service
                        HttpResponse response = ((HttpResponseException) err).getResponse();
                        response.getBodyAsString()
                            .subscribe(body -> {
                                System.out.println("Status Code: " + response.getStatusCode());
                                System.out.println("Message: " + body);
                            });
                    } else {
                        // Allow other types of errors to throw
                        throw new RuntimeException(err);
                    }
                },
                () -> System.out.println("completed"));

        /*
        This will block until the above query has completed. This is strongly discouraged for use in production as
        it eliminates the benefits of asynchronous IO. It is used here to ensure the sample runs to completion.
        */
        results.blockLast();
    }
}
