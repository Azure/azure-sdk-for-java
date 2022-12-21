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
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressOptions;

public class ReverseSearchCrossStreetAddressSample {
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

        // Search address reverse cross street -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address-reverse-cross-street
        // BEGIN: com.azure.maps.search.sync.search_reverse_cross_street_address
        System.out.println("Revere Search Cross Street Address:");

        // options
        client.reverseSearchCrossStreetAddress(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337)));

        // options
        client.reverseSearchCrossStreetAddress(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337))
                .setTop(2)
                .setHeading(5));

        // complete
        client.reverseSearchCrossStreetAddressWithResponse(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337))
                .setTop(2)
                .setHeading(5),
            null).getStatusCode();
        // END: com.azure.maps.search.sync.search_reverse_cross_street_address

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

        // Search address reverse cross street -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address-reverse-cross-street
        // BEGIN: com.azure.maps.search.async.search_reverse_cross_street_address
        System.out.println("Revere Search Cross Street Address:");

        // options
        asyncClient.reverseSearchCrossStreetAddress(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337)));

        // options
        asyncClient.reverseSearchCrossStreetAddress(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337))
                .setTop(2)
                .setHeading(5));

        // complete
        asyncClient.reverseSearchCrossStreetAddressWithResponse(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337))
                .setTop(2)
                .setHeading(5)).block().getStatusCode();
        // END: com.azure.maps.search.async.search_reverse_cross_street_address
    }
}
