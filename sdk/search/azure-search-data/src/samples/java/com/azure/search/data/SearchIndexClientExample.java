// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data;

import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.customization.SearchIndexClientBuilder;
import com.azure.search.data.generated.models.SearchResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        String searchServiceName = "";
        String apiKey = "";
        String dnsSuffix = "search.windows.net";
        String indexName = "";
        String apiVersion = "2019-05-06";


        SearchIndexAsyncClient searchClient = new SearchIndexClientBuilder()
            .serviceName(searchServiceName)
            .searchDnsSuffix(dnsSuffix)
            .indexName(indexName)
            .apiVersion(apiVersion)
            .credential(apiKey)
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

        System.out.println("Oh Yeah");

    }

    private static <T> T getDocument(Class<T> toValueType, Map<String, Object> document) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.convertValue(document, toValueType);
    }
}
