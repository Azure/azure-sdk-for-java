// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.Configuration;
import com.azure.search.models.SearchParameters;
import com.azure.search.models.SearchRequest;
import com.azure.search.models.SearchResult;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * This example shows how to handle errors when the Azure Cognitive Search service
 * returns a non-successful response
 */
public class HttpResponseExceptionExample {
    /*
     * From the Azure portal, get your Azure Cognitive Search service name and API key.
     */
    private static final String SEARCH_SERVICE = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_SERVICE");
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
            .serviceName(SEARCH_SERVICE)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildClient();

        try {
            // Perform a search on a non-existent field
            SearchParameters searchParams = new SearchParameters()
                .setFilter("Non_Existent_Field eq 'Luxury'");

            Iterable<SearchResult> results = client.search("hotel", searchParams, null);

            for (SearchResult result : results) {
                // normal results processing
                System.out.printf("Found hotel: %s%n", result.getAdditionalProperties().get("HotelName"));
            }
        }
        catch (HttpResponseException ex) {
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
            .serviceName(SEARCH_SERVICE)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildAsyncClient();

        SearchParameters searchParams = new SearchParameters()
            .setFilter("Non_Existent_Field eq 'Luxury'");

        PagedFlux<SearchResult> results = client.search("hotel", searchParams, null);
        results
            .subscribe(
            foo -> {
                // normal results processing
                System.out.printf("Found hotel: %s%n", foo.getAdditionalProperties().get("HotelName"));
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
            () -> { System.out.println("completed"); });

        /*
        This will block until the above query has completed. This is strongly discouraged for use in production as
        it eliminates the benefits of asynchronous IO. It is used here to ensure the sample runs to completion.
        */
        results.blockLast();
    }
}
