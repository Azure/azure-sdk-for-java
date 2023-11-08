// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.FuzzySearchOptions;

public class BeginFuzzySearchBatchSample {
    public static void main(String[] args) throws IOException {
        MapsSearchClientBuilder builder = new MapsSearchClientBuilder();

        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        // builder.credential(tokenCredential);

        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        MapsSearchClient client = builder.buildClient();

        // Post fuzzy search batch -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-fuzzy-batch
        // This is also a batch API like postSearchAddressBatch(), so the same calling
        // patterns apply.
        // BEGIN: com.azure.maps.search.sync.fuzzy_search_batch
        List<FuzzySearchOptions> fuzzyOptionsList = new ArrayList<>();
        fuzzyOptionsList.add(new FuzzySearchOptions("atm", new GeoPosition(-122.128362, 47.639769))
            .setRadiusInMeters(5000).setTop(5));
        fuzzyOptionsList.add(new FuzzySearchOptions("Statue of Liberty").setTop(2));
        fuzzyOptionsList.add(new FuzzySearchOptions("Starbucks", new GeoPosition(-122.128362, 47.639769))
            .setRadiusInMeters(5000));

        System.out.println("Post Search Fuzzy Batch Async");
        client.beginFuzzySearchBatch(fuzzyOptionsList).getFinalResult();
        // END: com.azure.maps.search.sync.fuzzy_search_batch

        MapsSearchClientBuilder asyncClientbuilder = new MapsSearchClientBuilder();

        // Authenticates using subscription key
        AzureKeyCredential asyncClientKeyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        asyncClientbuilder.credential(asyncClientKeyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();
        // asyncClientbuilder.credential(asyncClientTokenCredential);

        asyncClientbuilder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        asyncClientbuilder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        MapsSearchAsyncClient asyncClient = asyncClientbuilder.buildAsyncClient();

        // Post fuzzy search batch -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-fuzzy-batch
        // This is also a batch API like postSearchAddressBatch(), so the same calling
        // patterns apply.
        // BEGIN: com.azure.maps.search.async.fuzzy_search_batch
        List<FuzzySearchOptions> fuzzySearchBatchOptionsList = new ArrayList<>();
        fuzzySearchBatchOptionsList.add(new FuzzySearchOptions("atm", new GeoPosition(-122.128362, 47.639769))
            .setRadiusInMeters(5000).setTop(5));
        fuzzySearchBatchOptionsList.add(new FuzzySearchOptions("Statue of Liberty").setTop(2));
        fuzzySearchBatchOptionsList.add(new FuzzySearchOptions("Starbucks", new GeoPosition(-122.128362, 47.639769))
            .setRadiusInMeters(5000));

        System.out.println("Post Search Fuzzy Batch Async");
        asyncClient.beginFuzzySearchBatch(fuzzyOptionsList).getSyncPoller().getFinalResult();
        // END: com.azure.maps.search.async.fuzzy_search_batch
    }
}
