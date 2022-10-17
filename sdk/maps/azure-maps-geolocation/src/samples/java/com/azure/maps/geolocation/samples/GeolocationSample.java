// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.geolocation.GeolocationAsyncClient;
import com.azure.maps.geolocation.GeolocationClient;
import com.azure.maps.geolocation.GeolocationClientBuilder;

public class GeolocationSample {
    public GeolocationClient createGeoLocationSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.geolocation.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a client
        GeolocationClient client = new GeolocationClientBuilder()
            .credential(keyCredential)
            .buildClient();
        // END: com.azure.maps.geolocation.sync.builder.key.instantiation

        return client;
    }

    public GeolocationClient createGeoLocationSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.geolocation.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a client
        GeolocationClient client = new GeolocationClientBuilder()
            .credential(tokenCredential)
            .clientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();
        // END: com.azure.maps.geolocation.sync.builder.ad.instantiation

        return client;
    }

    public GeolocationAsyncClient createGeoLocationAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.geolocation.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates an async client
        GeolocationAsyncClient asyncClient = new GeolocationClientBuilder()
            .credential(keyCredential)
            .buildAsyncClient();
        // END: com.azure.maps.geolocation.async.builder.key.instantiation

        return asyncClient;
    }

    public GeolocationAsyncClient createGeoLocationAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.geolocation.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates an async client
        GeolocationAsyncClient asyncClient = new GeolocationClientBuilder()
            .credential(tokenCredential)
            .buildAsyncClient();
        // END: com.azure.maps.geolocation.async.builder.ad.instantiation

        return asyncClient;
    }
}
