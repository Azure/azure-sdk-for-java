// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data;

import com.azure.search.data.common.credentials.ApiKeyCredentials;
import com.azure.search.data.customization.SearchIndexClientBuilder;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;

import java.util.Arrays;
import java.util.List;

public class GenericDocumentSearchExample {

    public static void main(String[] args) {
        SearchWithMultipleResults();
    }

    public static void SearchWithMultipleResults(){
        SearchIndexAsyncClient searchClient = getSearchClient();
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.filter("geo.distance(Location,geography'POINT(-122.121513 47.673988)') le 15"); // items having a geo-location distance which is less than 5 km from Redmond
        searchParameters.facets(Arrays.asList("Tags,sort:value"));

        List<SearchResult> results = searchClient
            .search("luxury hotel", searchParameters, new SearchRequestOptions())
            .log()
            .doOnSubscribe(ignoredVal -> System.out.println("Subscribed to paged flux processing items"))
            .doOnNext(item ->
                System.out.println(
                    "Found Hotel: " + item.additionalProperties().get("HotelName")
                        + " (Rating:"+ item.additionalProperties().get("Rating") + ")"))
            .doOnComplete(() -> System.out.println("Completed processing"))
            .doOnError(err -> System.out.println("error:"+ err))
            .collectList().block();
        System.out.println("Done");

        /** Output:
         * [main] INFO reactor.Flux.Paged.1 - onSubscribe(MonoFlatMapMany.FlatMapManyMain)
         * Subscribed to paged flux processing items
         * [main] INFO reactor.Flux.Paged.1 - request(unbounded)
         * [reactor-http-nio-4] INFO reactor.Flux.Paged.1 - onNext(com.azure.search.data.generated.models.SearchResult@768ff74)
         * Found Hotel: Days Hotel (Rating:4.2
         * [reactor-http-nio-4] INFO reactor.Flux.Paged.1 - onNext(com.azure.search.data.generated.models.SearchResult@44c99434)
         * Found Hotel: Suites At Bellevue Square (Rating:4.0
         * [reactor-http-nio-4] INFO reactor.Flux.Paged.1 - onComplete()
         * Completed processing
         * Done
         **/
    }

    private static SearchIndexAsyncClient getSearchClient() {
        ApiKeyCredentials apiKeyCredentials = new ApiKeyCredentials("<apiKeyCredentials>");
        String searchServiceName = "<searchServiceName>";
        String dnsSuffix = "search.windows.net";
        String indexName = "<indexName>";

        return new SearchIndexClientBuilder()
            .serviceName(searchServiceName)
            .searchDnsSuffix(dnsSuffix)
            .indexName(indexName)
            .credential(apiKeyCredentials)
            .buildAsyncClient();
    }
}
