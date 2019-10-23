// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.search.models.SuggestOptions;
import com.azure.search.models.SuggestResult;

import java.util.Iterator;
import java.util.List;

/**
 * This example shows how to work with suggestions and search results
 */
public class SearchSuggestionExample {
    /*
      From the Azure portal, get your Azure Cognitive Search service URL and API key,
      and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_API_KEY");

    public static void main(String[] args) {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .serviceEndpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels")
            .buildClient();

        SearchSuggestionHighlight(client);
        SearchSuggestionFuzzy(client);
    }

    private static void SearchSuggestionHighlight(SearchIndexClient searchClient) {
        SuggestOptions suggestOptions = new SuggestOptions()
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>")
            .setFilter("Category eq 'Luxury'")
            .setTop(1);

        PagedIterable<SuggestResult> suggestResult =
            searchClient.suggest("hotel", "sg", suggestOptions, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        List<SuggestResult> response = iterator.next().getValue();
        System.out.println("Received results with highlight:");
        response.forEach(r -> System.out.println(r.getText()));

        /* Output:
          Received results with highlight:
          Best <b>hotel</b> in town if you like luxury <b>hotels</b>. They have an amazing infinity pool, a spa, and
          a really helpful concierge. The location is perfect -- right downtown, close to all the tourist
          attractions. We highly recommend this <b>hotel</b>.
         */
    }

    private static void SearchSuggestionFuzzy(SearchIndexClient searchClient) {
        SuggestOptions suggestOptions = new SuggestOptions()
            .setUseFuzzyMatching(true);

        PagedIterable<SuggestResult> suggestResult =
            searchClient.suggest("hitel", "sg", suggestOptions, null);
        Iterator<PagedResponse<SuggestResult>> iterator = suggestResult.iterableByPage().iterator();

        List<SuggestResult> response = iterator.next().getValue();
        System.out.println("Received results with fuzzy option:");
        response.forEach(r -> System.out.println(r.getText()));

        /* Output:
          Received results with fuzzy option:
          Countryside Hotel
          Pretty good hotel
          Another good hotel
          Very popular hotel in town
          Cheapest hotel in town. Infact, a motel.
         */
    }
}
