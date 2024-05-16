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
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.ReverseSearchAddressOptions;


public class BeginReverseSearchAddressBatchSample {
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

        // Search address reverse batch -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-address-reverse-batch
        // This is also a batch API like searchAddressBatch(), so the same calling
        // patterns apply.
        // BEGIN: com.azure.maps.search.sync.reverse_search_address_batch
        List<ReverseSearchAddressOptions> reverseOptionsList = new ArrayList<>();
        reverseOptionsList.add(new ReverseSearchAddressOptions(new GeoPosition(2.294911, 48.858561)));
        reverseOptionsList.add(
            new ReverseSearchAddressOptions(new GeoPosition(-122.127896, 47.639765))
                .setRadiusInMeters(5000)
        );
        reverseOptionsList.add(new ReverseSearchAddressOptions(new GeoPosition(-122.348170, 47.621028)));

        System.out.println("Reverse Search Address Batch Async");
        BatchReverseSearchResult br1 =
            client.beginReverseSearchAddressBatch(reverseOptionsList).getFinalResult();
        // END: com.azure.maps.search.sync.reverse_search_address_batch

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

        // Search address reverse batch -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-address-reverse-batch
        // This is also a batch API like searchAddressBatch(), so the same calling
        // patterns apply.
        // BEGIN: com.azure.maps.search.async.reverse_search_address_batch
        List<ReverseSearchAddressOptions> list2 = new ArrayList<>();
        list2.add(new ReverseSearchAddressOptions(new GeoPosition(2.294911, 48.858561)));
        list2.add(
            new ReverseSearchAddressOptions(new GeoPosition(-122.127896, 47.639765))
                .setRadiusInMeters(5000)
        );
        list2.add(new ReverseSearchAddressOptions(new GeoPosition(-122.348170, 47.621028)));

        System.out.println("Reverse Search Address Batch Async");
        BatchReverseSearchResult batchReverseSearchResult =
            asyncClient.beginReverseSearchAddressBatch(list2).getSyncPoller().getFinalResult();
        // END: com.azure.maps.search.async.reverse_search_address_batch
    }
}
