// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.SearchIndexStatistics;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;
import com.azure.search.documents.models.AutocompleteItem;
import com.azure.search.documents.models.AutocompleteMode;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SuggestOptions;
import com.azure.search.documents.models.SuggestResult;
import com.azure.search.documents.util.AutocompletePagedResponse;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SuggestPagedResponse;

import java.util.Iterator;

/**
 * This scenario assumes an existing search solution, with index and an indexer setup (see LifecycleSetupExample)
 * <a href="https://github.com/Azure-Samples/azure-search-sample-data">Azure Cognitive Search Sample Data</a>.
 */
public class RunningSearchSolutionExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API admin key, and set the values of these
     * environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    private static final String INDEX_NAME = "hotels-sample-index";
    private static final String INDEXER_NAME = "hotels-sample-indexer";
    private static final String SUGGESTER_NAME = "sg";

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = createIndexClient();
        SearchIndexerClient searchIndexerClient = createIndexerClient();
        SearchClient indexClient = createSearchClient();

        // get index statistics
        SearchIndexStatistics indexStatistics = searchIndexClient.getIndexStatistics(INDEX_NAME);
        System.out.printf("Index %s: Document Count = %d, Storage Size = %d%n", INDEX_NAME, indexStatistics.getDocumentCount(), indexStatistics.getStorageSize());

        // run indexer
        searchIndexerClient.runIndexer(INDEXER_NAME);

        // get indexer status
        SearchIndexerStatus indexerStatus = searchIndexerClient.getIndexerStatus(INDEXER_NAME);
        System.out.printf("Indexer %s status = %s%n", INDEXER_NAME, indexerStatus.getStatus());

        // run a search query
        searchQuery(indexClient);

        // run an autocomplete query
        autocompleteQuery(indexClient);

        // run a suggest query with fuzzy matching
        suggestQuery(indexClient);

    }

    private static void suggestQuery(SearchClient client) {

        SuggestOptions suggestOptions = new SuggestOptions()
            .setUseFuzzyMatching(true);

        PagedIterableBase<SuggestResult, SuggestPagedResponse> suggestResult = client.suggest("vew",
            SUGGESTER_NAME, suggestOptions, Context.NONE);
        Iterator<SuggestPagedResponse> iterator = suggestResult.iterableByPage().iterator();

        System.out.println("Suggest with fuzzy matching:");
        iterator.forEachRemaining(
            r -> r.getValue().forEach(
                res -> System.out.printf("      Found match to: %s, match = %s%n", (String) res
                    .getDocument(SearchDocument.class).get("HotelName"), res.getText())
            )
        );
    }

    private static void autocompleteQuery(SearchClient client) {

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(
            AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client.autocomplete("co",
            SUGGESTER_NAME, params, Context.NONE);

        System.out.println("Autocomplete with one term context results:");
        results.forEach(result -> System.out.println(result.getText()));
    }

    private static void searchQuery(SearchClient client) {

        // search=Resort&searchfields=HotelName&$count=true
        SearchOptions searchOptions = new SearchOptions()
            .setIncludeTotalCount(true)
            .setSearchFields("HotelName");
        SearchPagedIterable searchResults = client.search("Resort", searchOptions, Context.NONE);

        System.out.println("Search query results:");
        searchResults.forEach(result -> {
            SearchDocument doc = result.getDocument(SearchDocument.class);
            String hotelName = (String) doc.get("HotelName");
            System.out.printf("     Hotel: %s%n", hotelName);
        });
    }

    private static SearchClient createSearchClient() {
        return new SearchClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .indexName(INDEX_NAME)
            .buildClient();
    }

    private static SearchIndexClient createIndexClient() {
        return new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();
    }

    private static SearchIndexerClient createIndexerClient() {
        return new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildClient();
    }
}
