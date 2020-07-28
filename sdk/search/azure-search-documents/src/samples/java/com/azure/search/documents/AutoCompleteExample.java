// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.search.documents.models.AutocompleteItem;
import com.azure.search.documents.models.AutocompleteMode;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.util.AutocompletePagedResponse;

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

        SearchClient searchClient = new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .indexName("hotels-sample-index")
            .buildClient();

        autoCompleteWithOneTermContext(searchClient);
        autoCompleteWithHighlighting(searchClient);
        autoCompleteWithFilterAndFuzzy(searchClient);
    }

    private static void autoCompleteWithOneTermContext(SearchClient searchClient) {

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(
            AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = searchClient.autocomplete("coffee m",
            "sg", params, Context.NONE);

        System.out.println("Received results with one term context:");
        results.forEach(result -> System.out.println(result.getText()));

        /* Output:
         * Received results with one term context:
         * coffee maker
         */
    }

    private static void autoCompleteWithHighlighting(SearchClient searchClient) {
        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("Address/City eq 'San Diego' or Address/City eq 'Hartford'")
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>");

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = searchClient.autocomplete("co", "sg", params,
            Context.NONE);

        System.out.println("Received results with highlighting:");
        results.forEach(result -> System.out.println(result.getText()));

        /* Output:
         * Received results with highlighting:
         * coffee
         */
    }

    private static void autoCompleteWithFilterAndFuzzy(SearchClient searchClient) {
        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true)
            .setFilter("HotelId ne '6' and Category eq 'Budget'");

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = searchClient.autocomplete("su", "sg", params,
            Context.NONE);

        System.out.println("Received results with filter and fuzzy:");
        results.forEach(result -> System.out.println(result.getText()));

        /* Output:
         * Received results with filter and fuzzy:
         * suite
         */
    }
}
