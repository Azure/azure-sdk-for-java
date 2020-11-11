// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.util.SuggestPagedResponse;

import java.util.Iterator;
import java.util.List;

/**
 * This example shows how to work with suggestions and search results
 * <p>
 * This sample is based on the hotels-sample index available to install from the portal.
 * See https://docs.microsoft.com/en-us/azure/search/search-get-started-portal
 */
public class SearchSuggestionExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";

    public static void main(String[] args) {
        SearchClient client = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .indexName(INDEX_NAME)
            .buildClient();

        suggestWithHighlights(client);
        suggestWithFuzzySearch(client);
    }

    private static void suggestWithHighlights(SearchClient searchClient) {
        SuggestOptions suggestOptions = new SuggestOptions()
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>")
            .setFilter("Category eq 'Luxury'")
            .setTop(1);

        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResult =
            searchClient.suggest("hotel", "sg", suggestOptions, Context.NONE);
        Iterator<SuggestPagedResponse> iterator = suggestResult.iterableByPage().iterator();

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

    private static void suggestWithFuzzySearch(SearchClient searchClient) {
        SuggestOptions suggestOptions = new SuggestOptions()
            .setUseFuzzyMatching(true);

        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResult =
            searchClient.suggest("hitel", "sg", suggestOptions, Context.NONE);
        Iterator<SuggestPagedResponse> iterator = suggestResult.iterableByPage().iterator();

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
