// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Configuration;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteMode;
import com.azure.search.models.AutocompleteOptions;

import java.util.Iterator;

public class AutoCompleteExample {

    /*
      From the Azure portal, get your Azure Cognitive Search service URL and API key,
      and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_SEARCH_API_KEY");

    public static void main(String[] args) {
        /*
            This sample is based on the hotels-sample index available to install from the portal.
            See [instructions here](https://docs.microsoft.com/en-us/azure/search/search-get-started-portal)
         */
        SearchIndexClient searchClient = new SearchIndexClientBuilder()
            .serviceEndpoint(ENDPOINT)
            .credential(new ApiKeyCredentials(API_KEY))
            .indexName("hotels-sample")
            .buildClient();

        autoCompleteWithOneTermContext(searchClient);
        autoCompleteWithHighlighting(searchClient);
        autoCompleteWithFilterAndFuzzy(searchClient);
    }

    private static void autoCompleteWithOneTermContext(SearchIndexClient searchClient) {

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(
            AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        PagedIterable<AutocompleteItem> results = searchClient.autocomplete("coffee m",
            "sg", params, null);

        Iterator<PagedResponse<AutocompleteItem>> iterator = results.iterableByPage().iterator();

        System.out.println("Received results with one term context:");
        iterator.forEachRemaining(
            r -> r.getValue().forEach(
                res -> System.out.println(res.getText())
            )
        );

        /* Output:
         * Received results with one term context:
         * coffee maker
         */
    }

    private static void autoCompleteWithHighlighting(SearchIndexClient searchClient) {
        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("Address/City eq 'San Diego' or Address/City eq 'Hartford'")
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>");

        PagedIterable<AutocompleteItem> results = searchClient.autocomplete("co", "sg", params, null);

        Iterator<PagedResponse<AutocompleteItem>> iterator = results.iterableByPage().iterator();

        System.out.println("Received results with highlighting:");
        iterator.forEachRemaining(
            r -> r.getValue().forEach(
                res -> System.out.println(res.getText())
            )
        );

        /* Output:
         * Received results with highlighting:
         * coffee
         */
    }

    private static void autoCompleteWithFilterAndFuzzy(SearchIndexClient searchClient) {
        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true)
            .setFilter("HotelId ne '6' and Category eq 'Budget'");

        PagedIterable<AutocompleteItem> results = searchClient.autocomplete("su", "sg", params, null);

        Iterator<PagedResponse<AutocompleteItem>> iterator = results.iterableByPage().iterator();

        System.out.println("Received results with filter and fuzzy:");
        iterator.forEachRemaining(
            r -> r.getValue().forEach(
                res -> System.out.println(res.getText())
            )
        );

        /* Output:
         * Received results with filter and fuzzy:
         * suite
         */
    }
}
