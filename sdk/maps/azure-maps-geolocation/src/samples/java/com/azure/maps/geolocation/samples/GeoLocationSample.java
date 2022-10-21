// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.geolocation.GeoLocationAsyncClient;
import com.azure.maps.geolocation.GeoLocationClient;
import com.azure.maps.geolocation.GeoLocationClientBuilder;

public class GeoLocationSample {
    public GeoLocationClient createGeoLocationSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.geolocation.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a client
        GeoLocationClient client = new GeoLocationClientBuilder()
            .credential(keyCredential)
            .buildClient();
        // END: com.azure.maps.geolocation.sync.builder.key.instantiation

        return client;
    }

    public GeoLocationClient createGeoLocationSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.geolocation.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a client
        GeoLocationClient client = new GeoLocationClientBuilder()
            .credential(tokenCredential)
            .clientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();
        // END: com.azure.maps.geolocation.sync.builder.ad.instantiation

        return client;
    }

    public GeoLocationAsyncClient createGeoLocationAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.geolocation.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates an async client
        GeoLocationAsyncClient asyncClient = new GeoLocationClientBuilder()
            .credential(keyCredential)
            .buildAsyncClient();
        // END: com.azure.maps.geolocation.async.builder.key.instantiation

        return asyncClient;
    }

    public GeoLocationAsyncClient createGeoLocationAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.geolocation.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates an async client
        GeoLocationAsyncClient asyncClient = new GeoLocationClientBuilder()
            .credential(tokenCredential)
            .buildAsyncClient();
        // END: com.azure.maps.geolocation.async.builder.ad.instantiation

        return asyncClient;
    }
}
