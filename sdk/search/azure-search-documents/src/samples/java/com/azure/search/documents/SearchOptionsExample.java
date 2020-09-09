// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.util.SearchPagedIterable;

import java.util.stream.Stream;

/**
 * This example shows how to work with {@link SearchOptions} while performing searches
 * <p>
 * This sample is based on the hotels-sample index available to install from the portal.
 * See https://docs.microsoft.com/en-us/azure/search/search-get-started-portal
 * </p>
 */
public class SearchOptionsExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";

    public static void main(String[] args) {
        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .indexName(INDEX_NAME)
            .buildClient();

        searchResultsAsPagedIterable(searchClient);
        searchResultAsStream(searchClient);
        searchResultsCountFromPage(searchClient);
        searchResultsCoverageFromPage(searchClient);
        searchResultsFacets(searchClient);
    }

    private static void searchResultsFacets(SearchClient searchClient) {
        // Each page in the response of the search query holds the facets value
        // Get Facets property from the first page in the response
        SearchPagedIterable results = searchClient.search("*",
            new SearchOptions().setFacets("Rooms/BaseRate,values:5|8|10"), Context.NONE);
        results.getFacets().forEach((k, v) -> {
            v.forEach(result -> {
                System.out.println(k + " :");
                System.out.println("    count: " + result.getCount());
                result.getAdditionalProperties().forEach((f, d) ->
                    System.out.println("    " + f + " : " + d)
                );
            });
        });
    }

    private static void searchResultsCoverageFromPage(SearchClient searchClient) {
        // Each page in the response of the search query holds the coverage value
        // Accessing Coverage property when iterating by page
        SearchPagedIterable results = searchClient.search("*",
            new SearchOptions().setMinimumCoverage(80.0), Context.NONE);

        System.out.println("Coverage = " + results.getCoverage());
    }

    private static void searchResultsCountFromPage(SearchClient searchClient) {
        // Each page in the response of the search query holds the count value
        // Get total search results count
        // Get count property from the first page in the response
        SearchPagedIterable results = searchClient.search("*",
            new SearchOptions().setIncludeTotalCount(true), Context.NONE);

        System.out.println("Count = " + results.getTotalCount());
    }

    private static void searchResultAsStream(SearchClient searchClient) {
        // Converting search results to stream
        SearchPagedIterable results = searchClient.search("*");
        Stream<SearchResult> resultStream = results.stream();
        resultStream.forEach(result ->
            result.getDocument(SearchDocument.class).forEach((field, value) ->
                System.out.println((field + ":" + value)))
        );
    }

    private static void searchResultsAsPagedIterable(SearchClient searchClient) {
        searchClient.search("*").forEach(result ->
            result.getDocument(SearchDocument.class).forEach((field, value) ->
                System.out.println((field + ":" + value)))
        );
    }
}
