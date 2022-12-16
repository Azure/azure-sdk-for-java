// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.SearchStructuredAddressOptions;
import com.azure.maps.search.models.StructuredAddress;

public class SearchStructuredAddressSample {
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

        // Search address structured -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address-structured
        // BEGIN: com.azure.maps.search.sync.search_structured_address
        System.out.println("Search Address Structured:");

        // simple
        client.searchStructuredAddress(new StructuredAddress("US")
            .setPostalCode("98121")
            .setStreetNumber("15127")
            .setStreetName("NE 24th Street")
            .setMunicipality("Redmond")
            .setCountrySubdivision("WA"), null);

        // complete
        client.searchStructuredAddressWithResponse(new StructuredAddress("US")
            .setPostalCode("98121")
            .setStreetNumber("15127")
            .setStreetName("NE 24th Street")
            .setMunicipality("Redmond")
            .setCountrySubdivision("WA"),
            new SearchStructuredAddressOptions()
                    .setTop(2)
                    .setRadiusInMeters(1000),
            null).getStatusCode();
        // END: com.azure.maps.search.sync.search_structured_address

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

        // Search address structured -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address-structured
        // BEGIN: com.azure.maps.search.async.search_structured_address
        System.out.println("Search Address Structured:");

        // simple
        asyncClient.searchStructuredAddress(new StructuredAddress("US")
            .setPostalCode("98121")
            .setStreetNumber("15127")
            .setStreetName("NE 24th Street")
            .setMunicipality("Redmond")
            .setCountrySubdivision("WA"), null);

        // complete
        asyncClient.searchStructuredAddressWithResponse(new StructuredAddress("US")
            .setPostalCode("98121")
            .setStreetNumber("15127")
            .setStreetName("NE 24th Street")
            .setMunicipality("Redmond")
            .setCountrySubdivision("WA"),
            new SearchStructuredAddressOptions()
                    .setTop(2)
                    .setRadiusInMeters(1000)).block().getStatusCode();
        // END: com.azure.maps.search.async.search_structured_address
    }
}
