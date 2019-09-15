// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.customization.SearchIndexClientBuilder;
import com.azure.search.data.generated.models.SuggestParameters;
import com.azure.search.data.generated.models.SuggestResult;

import java.util.Iterator;
import java.util.List;

public class SearchSuggestionExample {

    public static void main(String[] args) {
        SearchIndexClient searchClient = getSearchClient();

        SearchSuggestionHighlight(searchClient);
        SearchSuggestionFuzzy(searchClient);
    }

    private static void SearchSuggestionHighlight(SearchIndexClient searchClient) {
        SuggestParameters suggestParams = new SuggestParameters()
            .highlightPreTag("<b>")
            .highlightPostTag("</b>")
            .filter("Category eq 'Luxury'")
            .top(1);

        PagedIterable<SuggestResult> suggestResult =
            searchClient.suggest("hotel", "sg", suggestParams, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        List<SuggestResult> response = iterator.next().value();
        System.out.println("Received results with highlight:");
        response.forEach(r -> System.out.println(r.text()));

        /** Output:
         * Received results with highlight:
         * Best <b>hotel</b> in town if you like luxury <b>hotels</b>. They have an amazing infinity pool, a spa, and
         * a really helpful concierge. The location is perfect -- right downtown, close to all the tourist
         * attractions. We highly recommend this <b>hotel</b>.
         **/
    }

    private static void SearchSuggestionFuzzy(SearchIndexClient searchClient) {
        SuggestParameters suggestParams = new SuggestParameters()
            .useFuzzyMatching(true);

        PagedIterable<SuggestResult> suggestResult =
            searchClient.suggest("hitel", "sg", suggestParams, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        List<SuggestResult> response = iterator.next().value();
        System.out.println("Received results with fuzzy option:");
        response.forEach(r -> System.out.println(r.text()));

        /** Output:
         * Received results with fuzzy option:
         * Countryside Hotel
         * Pretty good hotel
         * Another good hotel
         * Very popular hotel in town
         * Cheapest hotel in town. Infact, a motel.
         **/
    }

    private static SearchIndexClient getSearchClient() {
        String searchServiceName = "<searchServiceName>";
        String apiKey = "<apiKey>";
        String dnsSuffix = "search.windows.net";
        String indexName = "hotels";
        String apiVersion = "2019-05-06";

        return new SearchIndexClientBuilder()
            .serviceName(searchServiceName)
            .searchDnsSuffix(dnsSuffix)
            .indexName(indexName)
            .apiVersion(apiVersion)
            .addPolicy(new SearchPipelinePolicy(apiKey))
            .buildClient();
    }

}
