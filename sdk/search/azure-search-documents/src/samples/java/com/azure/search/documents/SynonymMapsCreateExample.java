// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.Index;
import com.azure.search.documents.models.SynonymMap;

import java.util.Arrays;
import java.util.Collections;

/**
 * This example shows how to create an index with a synonym map
 * See https://docs.microsoft.com/en-us/azure/search/search-get-started-portal
 */
public class SynonymMapsCreateExample {

    /**
     * From the Azure portal, get your Azure Cognitive Search service URL and API key,
     * and set the values of these environment variables:
     */
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ENDPOINT");
    private static final String API_ADMIN_KEY = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SEARCH_ADMIN_KEY");

    public static void main(String[] args) {
        SearchServiceClient serviceClient = new SearchServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_ADMIN_KEY))
            .buildClient();

        String synonymMapName = "desc-synonymmap";

        System.out.println("Create synonym map...\n");
        createSynonymMap(serviceClient, synonymMapName);

        System.out.println("Create index and assign synonym to it...\n");
        assignSynonymMapToIndex(synonymMapName);

        System.out.println("Complete....\n");

        // Clean up resources
        serviceClient.deleteSynonymMap(synonymMapName);
    }

    private static void createSynonymMap(SearchServiceClient serviceClient, String synonymMapName) {
        SynonymMap synonymMap = new SynonymMap()
            .setName(synonymMapName)
            .setSynonyms("hotel, motel\ninternet,wifi\nfive star=>luxury\neconomy,inexpensive=>budget");
        serviceClient.createSynonymMap(synonymMap);
    }

    private static void assignSynonymMapToIndex(String synonymMapName) {
        Index index = new Index()
            .setName("hotels")
            .setFields(Arrays.asList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(true),
                new Field()
                    .setName("HotelName")
                    .setType(DataType.EDM_STRING)
                    .setSynonymMaps(Collections.singletonList(synonymMapName))
            ));
    }
}
