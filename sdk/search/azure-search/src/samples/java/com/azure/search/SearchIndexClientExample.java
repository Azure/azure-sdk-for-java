// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedResponse;
import com.azure.search.common.SearchPagedResponse;
import com.azure.search.models.FacetResult;
import com.azure.search.models.SearchParameters;
import com.azure.search.models.SearchRequestOptions;
import com.azure.search.models.SearchResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
        ApiKeyCredentials apiKeyCredentials = new ApiKeyCredentials("<apiKeyCredentials>");
        String searchServiceName = "<searchServiceName>";
        String dnsSuffix = "search.windows.net";
        String indexName = "<indexName>";

        SearchIndexAsyncClient searchClient = new SearchIndexClientBuilder()
            .serviceName(searchServiceName)
            .searchDnsSuffix(dnsSuffix)
            .indexName(indexName)
            .credential(apiKeyCredentials)
            .buildAsyncClient();

        List<SearchResult> results = searchClient
            .search()
            .log()
            .doOnSubscribe(ignoredVal -> System.out.println("Subscribed to paged flux processing items"))
            .doOnNext(item -> System.out.println("Processing item " + item))
            .doOnComplete(() -> System.out.println("Completed processing"))
            .collectList().block();

        Stream<PagedResponse<SearchResult>> pagedResults = searchClient.search()
            .byPage().toStream();


        //Accessing Count property when iterating by page
        searchClient.search("search text",
            new SearchParameters().setIncludeTotalResultCount(true),
            new SearchRequestOptions())
            .byPage()
            .map( page -> ((SearchPagedResponse) page).count())
            .toStream();;

        //Getting just the count property
        Flux<Long> count = searchClient.search("search text",
            new SearchParameters().setIncludeTotalResultCount(true),
            new SearchRequestOptions())
            .byPage()
            .take(1)
            .map(page -> ((SearchPagedResponse) page).count());


        //Accessing Coverage property when iterating by page
        searchClient.search("search text",
            new SearchParameters().setMinimumCoverage(73.5),
            new SearchRequestOptions())
            .byPage()
            .map( page -> ((SearchPagedResponse) page).coverage())
            .toStream();;

        //Getting just the Coverage property
        Flux<Double> coverage = searchClient.search("search text",
            new SearchParameters().setMinimumCoverage(73.5),
            new SearchRequestOptions())
            .byPage()
            .take(1)
            .map(page -> ((SearchPagedResponse) page).coverage());

        //Accessing Facets property when iterating by page
        searchClient.search("search text",
            new SearchParameters().setFacets(Arrays.asList(
                "Rooms/BaseRate,values:5|8|10",
                "LastRenovationDate,values:2000-01-01T00:00:00Z")),
            new SearchRequestOptions())
            .byPage()
            .map( page -> ((SearchPagedResponse) page).facets())
            .toStream();;

        //Getting just the Facets property
        Flux<Map<String, List<FacetResult>>>  facets = searchClient.search("search text",
            new SearchParameters().setFacets(Arrays.asList(
                "Rooms/BaseRate,values:5|8|10",
                "LastRenovationDate,values:2000-01-01T00:00:00Z")),
            new SearchRequestOptions())
            .byPage()
            .take(1)
            .map(page -> ((SearchPagedResponse) page).facets());


        System.out.println("Oh Yeah");

    }

    private static <T> T getDocument(Class<T> toValueType, Map<String, Object> document) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.convertValue(document, toValueType);
    }
}
