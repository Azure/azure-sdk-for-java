// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteMode;
import com.azure.search.models.AutocompleteOptions;
import com.azure.search.models.RequestOptions;

import java.util.UUID;

/**
 * This sample is based on the hotels-sample index available to install from the portal.
 * See https://docs.microsoft.com/en-us/azure/search/search-get-started-portal
 */
public class AutoCompleteExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String API_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_API_KEY");

    public static void main(String[] args) {

        SearchIndexClient searchClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(API_KEY))
            .indexName("hotels-sample-index")
            .buildClient();

        autoCompleteWithOneTermContext(searchClient);
        autoCompleteWithHighlighting(searchClient);
        autoCompleteWithFilterAndFuzzy(searchClient);
    }

    private static void autoCompleteWithOneTermContext(SearchIndexClient searchClient) {

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(
            AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = searchClient.autocomplete("coffee m",
            "sg", params, requestOptions, Context.NONE);

        System.out.println("Received results with one term context:");
        results.forEach(result -> System.out.println(result.getText()));

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

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = searchClient.autocomplete("co", "sg", params,
            requestOptions, Context.NONE);

        System.out.println("Received results with highlighting:");
        results.forEach(result -> System.out.println(result.getText()));

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

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = searchClient.autocomplete("su", "sg", params,
            requestOptions, Context.NONE);

        System.out.println("Received results with filter and fuzzy:");
        results.forEach(result -> System.out.println(result.getText()));

        /* Output:
         * Received results with filter and fuzzy:
         * suite
         */
    }
}
