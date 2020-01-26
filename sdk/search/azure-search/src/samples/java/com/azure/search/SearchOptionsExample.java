// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SearchOptions;
import com.azure.search.models.SearchResult;

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
        SearchIndexClient searchClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(API_KEY))
            .indexName(INDEX_NAME)
            .buildClient();

        searchResultsAsPagedIterable(searchClient);
        searchResultAsStream(searchClient);
        searchResultsCountFromStream(searchClient);
        searchResultsCountFromPage(searchClient);
        searchResultsCoverage(searchClient);
        searchResultsCoverageFromStream(searchClient);
        searchResultsFacetsFromStream(searchClient);
        searchResultsFacets(searchClient);
    }

    private static void searchResultsFacets(SearchIndexClient searchClient) {
        // Each page in the response of the search query holds the facets value
        // Get Facets property from the first page in the response
        PagedIterableBase<SearchResult, SearchPagedResponse> results = searchClient.search("*",
            new SearchOptions().setFacets("Rooms/BaseRate,values:5|8|10"), new RequestOptions(), Context.NONE);

        results.iterableByPage().forEach(page ->
            page.getFacets().forEach((k, v) -> {
                v.forEach(result -> {
                    System.out.println(k + " :");
                    System.out.println("    count: " + result.getCount());
                    result.getDocument().forEach((f, d) ->
                        System.out.println("    " + f + " : " + d)
                    );
                });
            })
        );
    }

    private static void searchResultsFacetsFromStream(SearchIndexClient searchClient) {
        // Each page in the response of the search query holds the facets value
        // Accessing Facets property with stream
        PagedIterableBase<SearchResult, SearchPagedResponse> results = searchClient.search("*",
            new SearchOptions().setFacets("Rooms/BaseRate,values:5|8|10"), new RequestOptions(), Context.NONE);

        Stream<SearchPagedResponse> searchPagedResponseStream = results.streamByPage();
        searchPagedResponseStream.forEach(page ->
            page.getFacets().forEach((k, v) -> {
                v.forEach(result -> {
                    System.out.println(k + " :");
                    System.out.println("    count: " + result.getCount());
                    result.getDocument().forEach((f, d) ->
                        System.out.println("    " + f + " : " + d)
                    );
                });
            })
        );
    }

    private static void searchResultsCoverageFromStream(SearchIndexClient searchClient) {
        // Each page in the response of the search query holds the coverage value
        // Get Coverage property from the first page in the response
        PagedIterableBase<SearchResult, SearchPagedResponse> results = searchClient.search("*",
            new SearchOptions().setMinimumCoverage(80.0), new RequestOptions(), Context.NONE);

        results.streamByPage().forEach(searchPagedResponse ->
            System.out.println("Coverage = " + searchPagedResponse.getCoverage())
        );
    }

    private static void searchResultsCoverage(SearchIndexClient searchClient) {
        // Each page in the response of the search query holds the coverage value
        // Accessing Coverage property when iterating by page
        PagedIterableBase<SearchResult, SearchPagedResponse> results = searchClient.search("*",
            new SearchOptions().setMinimumCoverage(80.0), new RequestOptions(), Context.NONE);

        results.iterableByPage().forEach(page ->
            System.out.println("Coverage = " + page.getCoverage())
        );
    }

    private static void searchResultsCountFromPage(SearchIndexClient searchClient) {
        // Each page in the response of the search query holds the count value
        // Get total search results count
        // Get count property from the first page in the response
        PagedIterableBase<SearchResult, SearchPagedResponse> results = searchClient.search("*",
            new SearchOptions().setIncludeTotalResultCount(true), new RequestOptions(), Context.NONE);

        Iterable<SearchPagedResponse> searchPagedResponses = results.iterableByPage();
        searchPagedResponses.forEach(page ->
            System.out.println("Count = " + page.getCount())
        );
    }

    private static void searchResultsCountFromStream(SearchIndexClient searchClient) {
        // Each page in the response of the search query holds the count value
        // Get total search results count by accessing the SearchPagedResponse
        // Access Count property when iterating by page
        PagedIterableBase<SearchResult, SearchPagedResponse> results = searchClient.search("*",
            new SearchOptions().setIncludeTotalResultCount(true), new RequestOptions(), Context.NONE);

        Stream<SearchPagedResponse> searchPagedResponseStream = results.streamByPage();
        searchPagedResponseStream.forEach(page ->
            System.out.println("Count = " + page.getCount())
        );

    }

    private static void searchResultAsStream(SearchIndexClient searchClient) {
        // Converting search results to stream
        PagedIterableBase<SearchResult, SearchPagedResponse> results = searchClient.search("*");
        Stream<SearchResult> resultStream = results.stream();
        resultStream.forEach(result ->
            result.getDocument().forEach((field, value) -> System.out.println((field + ":" + value)))
        );
    }

    private static void searchResultsAsPagedIterable(SearchIndexClient searchClient) {
        PagedIterableBase<SearchResult, SearchPagedResponse> results = searchClient.search("*");
        results.forEach(result ->
            result.getDocument().forEach((field, value) -> System.out.println((field + ":" + value)))
        );
    }
}
