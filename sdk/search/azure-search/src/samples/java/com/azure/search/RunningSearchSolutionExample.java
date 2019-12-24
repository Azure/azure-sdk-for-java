// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteMode;
import com.azure.search.models.AutocompleteOptions;
import com.azure.search.models.GetIndexStatisticsResult;
import com.azure.search.models.IndexerExecutionInfo;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SearchOptions;
import com.azure.search.models.SearchResult;
import com.azure.search.models.SuggestOptions;
import com.azure.search.models.SuggestResult;

import java.util.Iterator;

/**
 * This scenario assumes an existing search solution, with index and an indexer setup (see LifecycleSetupExample)
 * Azure Search Sample Data
 * https://docs.microsoft.com/en-us/samples/azure-samples/azure-search-sample-data/azure-search-sample-data/
 */
public class RunningSearchSolutionExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API admin key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";
    private static final String INDEXER_NAME = "hotels-sample-indexer";
    private static final String SUGGESTER_NAME = "sg";

    public static void main(String[] args) {
        SearchServiceClient serviceClient = createServiceClient();
        SearchIndexClient indexClient = createIndexClient();

        // get index statistics
        GetIndexStatisticsResult indexStatistics = serviceClient.getIndexStatistics(INDEX_NAME);
        System.out.printf("Index %s: Document Count = %d, Storage Size = %d%n", INDEX_NAME, indexStatistics.getDocumentCount(), indexStatistics.getStorageSize());

        // run indexer
        serviceClient.runIndexer(INDEXER_NAME);

        // get indexer status
        IndexerExecutionInfo indexerStatus = serviceClient.getIndexerStatus(INDEXER_NAME);
        System.out.printf("Indexer %s status = %s%n", INDEXER_NAME, indexerStatus.getStatus());

        // run a search query
        searchQuery(indexClient);

        // run an autocomplete query
        autocompleteQuery(indexClient);

        // run a suggest query with fuzzy matching
        suggestQuery(indexClient);

    }

    private static void suggestQuery(SearchIndexClient client) {

        SuggestOptions suggestOptions = new SuggestOptions()
            .setUseFuzzyMatching(true);

        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("vew",
            SUGGESTER_NAME, suggestOptions, new RequestOptions(), Context.NONE);
        Iterator<SuggestPagedResponse> iterator = suggestResult.iterableByPage().iterator();

        System.out.println("Suggest with fuzzy matching:");
        iterator.forEachRemaining(
            r -> r.getValue().forEach(
                res -> System.out.printf("      Found match to: %s, match = %s%n", (String) res.getDocument().get("HotelName"), res.getText())
            )
        );
    }

    private static void autocompleteQuery(SearchIndexClient client) {

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(
            AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client.autocomplete("co",
            SUGGESTER_NAME, params, new RequestOptions(), Context.NONE);

        System.out.println("Autocomplete with one term context results:");
        results.forEach(result -> System.out.println(result.getText()));
    }

    private static void searchQuery(SearchIndexClient client) {

        // search=Resort&searchfields=HotelName&$count=true
        SearchOptions searchOptions = new SearchOptions()
            .setIncludeTotalResultCount(true)
            .setSearchFields("HotelName");
        PagedIterableBase<SearchResult, SearchPagedResponse> searchResults = client.search("Resort",
            searchOptions, new RequestOptions(), Context.NONE);

        System.out.println("Search query results:");
        searchResults.forEach(result -> {
            Document doc = result.getDocument();
            String hotelName = (String) doc.get("HotelName");
            System.out.printf("     Hotel: %s%n", hotelName);
        });
    }

    private static SearchIndexClient createIndexClient() {
        return new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .indexName(INDEX_NAME)
            .buildClient();
    }

    private static SearchServiceClient createServiceClient() {
        return new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new SearchApiKeyCredential(ADMIN_KEY))
            .buildClient();
    }
}
