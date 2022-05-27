// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.util.SearchPagedFlux;
import com.azure.search.documents.util.SearchPagedResponse;

import java.util.List;
import java.util.stream.Stream;

/**
 * This example shows how to work with {@link SearchOptions} while performing searches
 * <p>
 * This sample is based on the hotels-sample index available to install from the portal. See <a
 * href="https://docs.microsoft.com/azure/search/search-get-started-portal">Search getting started portal</a>
 * </p>
 */
public class SearchOptionsAsyncExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key, and set the values of these
     * environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";

    public static void main(String[] args) {
        SearchAsyncClient searchClient = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .indexName(INDEX_NAME)
            .buildAsyncClient();

        searchResultsAsList(searchClient);
        searchResultAsStreamOfPagedResponse(searchClient);
        searchResultsCountFormPage(searchClient);
        searchResultsCoverageFromPage(searchClient);
        searchResultsFacetsFromPage(searchClient);
    }

    private static void searchResultsFacetsFromPage(SearchAsyncClient searchClient) {
        // Each page in the response of the search query holds the facets value
        // Get Facets property from the first page in the response
        SearchPagedFlux results = searchClient.search("*",
            new SearchOptions().setFacets("Rooms/BaseRate,values:5|8|10"));

        results.getFacets()
            .doOnNext(facetResults -> facetResults.forEach((key, value) -> value.forEach(result -> {
                System.out.println(key + " :");
                System.out.println("    count: " + result.getCount());
                result.getAdditionalProperties().forEach((f, d) -> System.out.println("    " + f + " : " + d));
            })))
            .block();
    }

    private static void searchResultsCoverageFromPage(SearchAsyncClient searchClient) {
        // Each page in the response of the search query holds the coverage value
        // Get Coverage property from the first page in the response
        SearchPagedFlux results = searchClient.search("*",
            new SearchOptions().setMinimumCoverage(80.0));

        System.out.println("Coverage = " + results.getCoverage().block());
    }

    private static void searchResultsCountFormPage(SearchAsyncClient searchClient) {
        // Each page in the response of the search query holds the count value
        // Get total search results count
        // Get count property from the first page in the response
        SearchPagedFlux results = searchClient.search("*",
            new SearchOptions().setIncludeTotalCount(true));

        System.out.println("Count = " + results.getTotalCount().block());
    }


    private static void searchResultAsStreamOfPagedResponse(SearchAsyncClient searchClient) {
        // Converting search results to stream
        Stream<SearchPagedResponse> streamResponse = searchClient.search("*")
            .byPage().toStream();

        streamResponse.forEach(searchPagedResponse -> searchPagedResponse.getElements().forEach(result ->
            result.getDocument(SearchDocument.class).forEach((field, value) ->
                System.out.println((field + ":" + value)))));
    }

    private static void searchResultsAsList(SearchAsyncClient searchClient) {
        // Converting search results to list
        List<SearchResult> searchResults = searchClient.search("*")
            .log()
            .doOnSubscribe(ignoredVal -> System.out.println("Subscribed to paged flux processing items"))
            .doOnNext(result ->
                result.getDocument(SearchDocument.class).forEach((field, value) ->
                    System.out.println((field + ":" + value)))
            )
            .doOnComplete(() -> System.out.println("Completed processing"))
            .collectList().block();
    }
}
