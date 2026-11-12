// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;

public class ListIndexersExample {

    /**
     * This example shows how to list all existing indexers in a Cognitive Search Service.
     * <p>
     * From the Azure portal, get your Azure AI Search service URL and API key,
     * and set the values of these environment variables:
     */

    private static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String ADMIN_KEY = Configuration.getGlobalConfiguration()
        .get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    public static void main(String[] args) {
        SearchIndexerAsyncClient indexerAsyncClient = new SearchIndexerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ADMIN_KEY))
            .buildAsyncClient();

        String prefix = args.length == 0 ? "" : args[0];
        listIndexers(indexerAsyncClient, prefix);
        indexerAsyncClient.listDataSourceConnections(listingOptions(prefix)).mapPage(source -> source.getName())
            .doOnNext(name -> System.out.println("Data source: " + name)).blockLast();
        indexerAsyncClient.listSkillsets(listingOptions(prefix)).mapPage(skillset -> skillset.getName())
            .doOnNext(name -> System.out.println("Skillset: " + name)).blockLast();
    }

    private static RequestOptions listingOptions(String prefix) {
        return new RequestOptions().addQueryParam("search", prefix).addQueryParam("searchType", "prefix")
            .addQueryParam("pageSize", "1").addQueryParam("$select", "name");
    }

    private static void listIndexers(SearchIndexerAsyncClient indexerAsyncClient, String prefix) {
        System.out.println("Found the following indexers:");
        indexerAsyncClient.listIndexers(listingOptions(prefix))
            .concatMap(indexer -> indexerAsyncClient.getIndexerStatus(indexer.getName()).doOnNext(status -> {
                System.out.println("Indexer: " + indexer.getName());
                if (status.getRuntime() != null) {
                    System.out.printf("Runtime: %d used, %s remaining seconds, period %s to %s%n",
                        status.getRuntime().getUsedSeconds(), status.getRuntime().getRemainingSeconds(),
                        status.getRuntime().getBeginningTime(), status.getRuntime().getEndingTime());
                }
            })).blockLast();
    }
}
