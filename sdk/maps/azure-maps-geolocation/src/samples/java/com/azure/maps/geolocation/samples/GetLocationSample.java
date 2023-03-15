// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation.samples;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.maps.geolocation.GeolocationAsyncClient;
import com.azure.maps.geolocation.GeolocationClient;
import com.azure.maps.geolocation.GeolocationClientBuilder;

public class GetLocationSample {
    public static void main(String[] args) throws IOException {
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a client
        GeolocationClient client = new GeolocationClientBuilder()
            .credential(keyCredential)
            .clientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();

        // Get Location -
        // https://docs.microsoft.com/en-us/rest/api/maps/geolocation/get-ip-to-location
        // This service will return the ISO country code for the provided IP address.
        // Developers can use this information to block or alter certain content based on geographical
        // locations where the application is being viewed from.
        System.out.println("Get Location Sync Client");
        // BEGIN: com.azure.maps.geolocation.sync.get_ip_to_location
        client.getLocation("131.107.0.89");
        // END: com.azure.maps.geolocation.sync.get_ip_to_location

        // Authenticates using subscription key
        AzureKeyCredential asyncClientKeyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        // DefaultAzureCredential asyncClientTokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a async client
        GeolocationAsyncClient asyncClient = new GeolocationClientBuilder()
            .credential(asyncClientKeyCredential)
            .buildAsyncClient();

        // Get Location -
        // https://docs.microsoft.com/en-us/rest/api/maps/geolocation/get-ip-to-location
        // This service will return the ISO country code for the provided IP address.
        // Developers can use this information to block or alter certain content based on geographical
        // locations where the application is being viewed from.
        System.out.println("Get Location Async Client");
        // BEGIN: com.azure.maps.geolocation.async.get_ip_to_location
        asyncClient.getLocation("131.107.0.89");
        // END: com.azure.maps.geolocation.async.get_ip_to_location
    }
}
