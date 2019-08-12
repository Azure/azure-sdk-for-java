// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data;

import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.common.SearchPagedResponse;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.customization.SearchIndexClientBuilder;
import com.azure.search.data.generated.models.DocumentSearchResult;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import com.azure.search.data.models.Hotel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Sample demonstrates how to create a SearchIndexClient and issue search API
 */
public class SearchIndexClientExample {

    /**
     * sample
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        String searchServiceName = "learnaisearch";
        String apiKey = "A09A17F3A00BBDD62D04958775D73FE3";
        String dnsSuffix = "search.windows.net";
        String indexName = "lishakur-test";
        String apiVersion = "2019-05-06";


        SearchIndexAsyncClient searchClient = new SearchIndexClientBuilder()
            .serviceName(searchServiceName)
            .searchDnsSuffix(dnsSuffix)
            .indexName(indexName)
            .apiVersion(apiVersion)
            .addPolicy(new SearchPipelinePolicy(apiKey))
            .buildAsyncClient();

        List<SearchResult> results = searchClient
            .search()
            .log()
            .doOnSubscribe(ignoredVal -> System.out.println("Subscribed to paged flux processing items"))
            .doOnNext(item -> System.out.println("Processing item " + item))
            .doOnComplete(() -> System.out.println("Completed processing"))
            .collectList().block();

        List<PagedResponse<SearchResult>> pagedResults = searchClient.search().byPage().collectList().block();

        System.out.println("Oh Yeah");

    }

    private static void searchForAll(SearchIndexClient searchClient) {
        DocumentSearchResult result = searchClient.search("*", new SearchParameters(), new SearchRequestOptions());


        for (SearchResult searchResult : result.results()) {
            Hotel hotel = getDocument(Hotel.class, searchResult.additionalProperties());
            System.out.printf(
                "\t score: %s, id: %s, name: %s\n",
                searchResult.score(),
                hotel.HotelId,
                hotel.HotelName);
        }
    }

    private static <T> T getDocument(Class<T> toValueType, Map<String, Object> document) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.convertValue(document, toValueType);
    }
}
