// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;
import java.util.Arrays;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.SearchNearbyPointsOfInterestOptions;

public class SearchNearbyPointsOfInterestSample {
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

        // Search nearby -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-nearby
        // BEGIN: com.azure.maps.search.sync.search_nearby
        System.out.println("Search Nearby Points of Interest:");

        // options
        client.searchNearbyPointsOfInterest(
            new SearchNearbyPointsOfInterestOptions(new GeoPosition(-74.011454, 40.706270))
                .setCountryFilter(Arrays.asList("US"))
                .setTop(10));

        // response
        client.searchNearbyPointsOfInterestWithResponse(
            new SearchNearbyPointsOfInterestOptions(new GeoPosition(-74.011454, 40.706270))
                .setCountryFilter(Arrays.asList("US"))
                .setTop(10),
            null).getStatusCode();
        // END: com.azure.maps.search.sync.search_nearby

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

        // Search nearby -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-nearby
        // BEGIN: com.azure.maps.search.async.search_nearby
        System.out.println("Search Nearby Points of Interest:");

        // options
        asyncClient.searchNearbyPointsOfInterest(
            new SearchNearbyPointsOfInterestOptions(new GeoPosition(-74.011454, 40.706270))
                .setCountryFilter(Arrays.asList("US"))
                .setTop(10));

        // response
        asyncClient.searchNearbyPointsOfInterestWithResponse(
            new SearchNearbyPointsOfInterestOptions(new GeoPosition(-74.011454, 40.706270))
                .setCountryFilter(Arrays.asList("US"))
                .setTop(10)).block().getStatusCode();
        // END: com.azure.maps.search.async.search_nearby

    }
}
