// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.models.GeoPosition;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.OperatingHoursRange;
import com.azure.maps.search.models.SearchPointOfInterestOptions;

public class SearchPointOfInterestSample {
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

        // Search POI -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-poi
        // BEGIN: com.azure.maps.search.sync.get_search_poi
        System.out.println("Search Points of Interest:");

        // coordinates
        client.searchPointOfInterest(
            new SearchPointOfInterestOptions("pizza", new GeoPosition(-121.97483, 36.98844)));

        // options
        client.searchPointOfInterest(
            new SearchPointOfInterestOptions("pizza", new GeoPosition(-121.97483, 36.98844))
                .setTop(10)
                .setOperatingHours(OperatingHoursRange.NEXT_SEVEN_DAYS));

        // with response
        client.searchPointOfInterestWithResponse(
            new SearchPointOfInterestOptions("pizza", new GeoPosition(-121.97483, 36.98844))
                .setTop(10)
                .setOperatingHours(OperatingHoursRange.NEXT_SEVEN_DAYS),
            null).getStatusCode();
        // END: com.azure.maps.search.sync.get_search_poi

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

        // Search POI -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-poi
        // BEGIN: com.azure.maps.search.async.get_search_poi
        System.out.println("Search Points of Interest:");

        // coordinates
        asyncClient.searchPointOfInterest(
            new SearchPointOfInterestOptions("pizza", new GeoPosition(-121.97483, 36.98844)));

        // options
        asyncClient.searchPointOfInterest(
            new SearchPointOfInterestOptions("pizza", new GeoPosition(-121.97483, 36.98844))
                .setTop(10)
                .setOperatingHours(OperatingHoursRange.NEXT_SEVEN_DAYS));

        // with response
        asyncClient.searchPointOfInterestWithResponse(
            new SearchPointOfInterestOptions("pizza", new GeoPosition(-121.97483, 36.98844))
                .setTop(10)
                .setOperatingHours(OperatingHoursRange.NEXT_SEVEN_DAYS)).block().getStatusCode();
        // END: com.azure.maps.search.async.get_search_poi
    }
}
