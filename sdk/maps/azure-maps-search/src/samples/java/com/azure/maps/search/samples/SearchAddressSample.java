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
import com.azure.maps.search.models.SearchAddressOptions;

public class SearchAddressSample {

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

        // Search address -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address
        // BEGIN: com.azure.maps.search.sync.search_address
        System.out.println("Search Address:");

        // simple
        client.searchAddress(
            new SearchAddressOptions("15127 NE 24th Street, Redmond, WA 98052"));

        // options
        client.searchAddress(
            new SearchAddressOptions("1 Main Street")
                .setCoordinates(new GeoPosition(-74.011454, 40.706270))
                .setRadiusInMeters(40000)
                .setTop(5));

        // complete
        client.searchAddressWithResponse(
            new SearchAddressOptions("1 Main Street")
                .setCoordinates(new GeoPosition(-74.011454, 40.706270))
                .setRadiusInMeters(40000)
                .setTop(5), null).getStatusCode();
        // END: com.azure.maps.search.sync.search_address

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

        // Search address -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address
        // BEGIN: com.azure.maps.search.async.search_address
        System.out.println("Search Address:");

        // simple
        asyncClient.searchAddress(
            new SearchAddressOptions("15127 NE 24th Street, Redmond, WA 98052"));

        // options
        asyncClient.searchAddress(
            new SearchAddressOptions("1 Main Street")
                .setCoordinates(new GeoPosition(-74.011454, 40.706270))
                .setRadiusInMeters(40000)
                .setTop(5));

        // complete
        asyncClient.searchAddressWithResponse(
            new SearchAddressOptions("1 Main Street")
                .setCoordinates(new GeoPosition(-74.011454, 40.706270))
                .setRadiusInMeters(40000)
                .setTop(5)).block().getStatusCode();
        // END: com.azure.maps.search.async.search_address
    }
}
