// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.util.Configuration;
import com.azure.search.models.SearchOptions;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SearchResult;
import reactor.core.publisher.Flux;

/**
 * This example shows how to perform basic searches using the Azure Cognitive Search SDK for Java
 *
 * This sample is based on the hotels-sample index available to install from the portal.
 * See https://docs.microsoft.com/en-us/azure/search/search-get-started-portal
 */
public class SearchForDynamicDocumentsExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_API_KEY");

    public static void main(String[] args) {
        searchWithSyncClient();
        searchWithAsyncClient();
    }

    /**
     * Minimal search with {@link SearchIndexClient}
     * Search for luxury hotels print all results to the console
     */
    private static void searchWithSyncClient() {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .serviceEndpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildClient();

        // Perform a text-based search
        for (SearchResult result : client.search("luxury hotel", new SearchOptions(), new RequestOptions())) {

            // Each result is a dynamic Map
            Document doc = result.getDocument();
            String hotelName = (String) doc.get("HotelName");
            Integer rating = (Integer) doc.get("Rating");

            System.out.printf("%s: %d%n", hotelName, rating);
        }
    }

    /**
     * Additional search options and results processing using {@link SearchIndexAsyncClient}
     * Search for the top 5 rated luxury hotels near Redmond and print all results to the console
     */
    private static void searchWithAsyncClient() {
        SearchIndexAsyncClient client = new SearchIndexClientBuilder()
            .serviceEndpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildAsyncClient();

        // Add additional options for the search
        SearchOptions parameters = new SearchOptions()
            .setFilter("geo.distance(Location,geography'POINT(-122.121513 47.673988)') le 5")  // items having a geo-location distance which is less than 5 km from Redmond
            .setFacets("Tags,sort:value")
            .setOrderBy("Rating")
            .setTop(5)
            .setIncludeTotalResultCount(true);

        // Perform a search and subscribe to the results and log additional information
        Flux<SearchResult> results = client.search("hotel", parameters, new RequestOptions())
            .log()
            .doOnSubscribe(__ -> System.out.println("Subscribed to PagedFlux results"));

        // Subscribe and process all results across all pages in the response
        results.subscribe(
            result -> {
                Document doc = result.getDocument();
                String hotelName = (String) doc.get("HotelName");
                Integer rating = (Integer) doc.get("Rating");

                System.out.printf("%s: %d%n", hotelName, rating);
            },
            err -> System.out.printf("error: %s%n", err),
            () -> System.out.println("Completed processing"));

        /*
        This will block until the above query has completed. This is strongly discouraged for use in production as
        it eliminates the benefits of asynchronous IO. It is used here to ensure the sample runs to completion.
        */
        results.blockLast();
    }
}
