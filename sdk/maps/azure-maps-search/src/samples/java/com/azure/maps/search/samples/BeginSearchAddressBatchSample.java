// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.polling.SyncPoller;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.SearchAddressOptions;

public class BeginSearchAddressBatchSample {
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

        // Search address batch sync -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-address-batch
        // This call posts addresses for search using the Synchronous Batch API.
        // All results will be available when the call returns. A maximum of 100
        // addresses can be searched this way.
        // BEGIN: com.azure.maps.search.sync.search_address_batch
        List<SearchAddressOptions> optionsList = new ArrayList<>();
        optionsList.add(new SearchAddressOptions("400 Broad St, Seattle, WA 98109").setTop(3));
        optionsList.add(new SearchAddressOptions("One, Microsoft Way, Redmond, WA 98052").setTop(3));
        optionsList.add(new SearchAddressOptions("350 5th Ave, New York, NY 10118").setTop(3));
        optionsList.add(new SearchAddressOptions("1 Main Street")
            .setCountryFilter(Arrays.asList("GB", "US", "AU")).setTop(3));

        // Search address batch async -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-address-batch
        // This call posts addresses for search using the Asynchronous Batch API.
        // SyncPoller will do the polling automatically and you can retrieve the result
        // with getFinalResult()
        System.out.println("Search Address Batch Async");
        client.beginSearchAddressBatch(optionsList).getFinalResult();
        SyncPoller<BatchSearchResult, BatchSearchResult> poller = client.beginSearchAddressBatch(optionsList);
        BatchSearchResult result = poller.getFinalResult();
        // END: com.azure.maps.search.sync.search_address_batch

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

        // Search address batch async -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-address-batch
        // This call posts addresses for search using the Asynchronous Batch API.
        // All results will be available when the call returns. A maximum of 100
        // addresses can be searched this way.
        // BEGIN: com.azure.maps.search.async.search_address_batch
        List<SearchAddressOptions> list = new ArrayList<>();
        list.add(new SearchAddressOptions("400 Broad St, Seattle, WA 98109").setTop(3));
        list.add(new SearchAddressOptions("One, Microsoft Way, Redmond, WA 98052").setTop(3));
        list.add(new SearchAddressOptions("350 5th Ave, New York, NY 10118").setTop(3));
        list.add(new SearchAddressOptions("1 Main Street")
            .setCountryFilter(Arrays.asList("GB", "US", "AU")).setTop(3));

        // Search address batch async -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-address-batch
        // This call posts addresses for search using the Asynchronous Batch API.
        // SyncPoller will do the polling automatically and you can retrieve the result
        // with getFinalResult()
        System.out.println("Search Address Batch Async");
        asyncClient.beginSearchAddressBatch(list).blockFirst().getFinalResult();
        SyncPoller<BatchSearchResult, BatchSearchResult> bp2 = asyncClient.beginSearchAddressBatch(list).getSyncPoller();
        BatchSearchResult batchResult2 = bp2.getFinalResult();
        // END: com.azure.maps.search.async.search_address_batch
    }
}
