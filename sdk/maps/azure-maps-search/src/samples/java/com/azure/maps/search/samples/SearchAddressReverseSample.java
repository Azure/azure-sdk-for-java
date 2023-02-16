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
import com.azure.maps.search.models.GeographicEntityType;
import com.azure.maps.search.models.ReverseSearchAddressOptions;

public class SearchAddressReverseSample {
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

        // Search address reverse -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address-reverse
        // BEGIN: com.azure.maps.search.sync.reverse_search_address
        System.out.println("Search Address Reverse:");

        // simple
        client.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337)));

        client.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337)));

        // options
        client.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337))
                .setIncludeSpeedLimit(true)
                .setEntityType(GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION) // returns only city
        );

        // complete
        client.reverseSearchAddressWithResponse(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337))
                .setIncludeSpeedLimit(true)
                .setEntityType(GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION),
                null).getStatusCode();
        // END: com.azure.maps.search.sync.reverse_search_address

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

        // Search address reverse -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address-reverse
        // BEGIN: com.azure.maps.search.async.reverse_search_address
        System.out.println("Search Address Reverse:");

        // simple
        asyncClient.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337)));

        asyncClient.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337)));

        // options
        asyncClient.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337))
                .setIncludeSpeedLimit(true)
                .setEntityType(GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION) // returns only city
        );

        // complete
        asyncClient.reverseSearchAddressWithResponse(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337))
                .setIncludeSpeedLimit(true)
                .setEntityType(GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION)).block().getStatusCode();
        // END: com.azure.maps.search.async.reverse_search_address
    }
}
