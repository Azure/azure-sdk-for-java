// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.elevation.ElevationAsyncClient;
import com.azure.maps.elevation.ElevationClient;
import com.azure.maps.elevation.ElevationClientBuilder;

public class ElevationSample {
    public ElevationClient createMapsSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.elevation.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a client
        ElevationClient client = new ElevationClientBuilder() 
            .credential(keyCredential)
            .elevationClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();
        // END: com.azure.maps.elevation.sync.builder.key.instantiation

        return client;
    }

    public ElevationClient createMapsSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.elevation.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a client
        ElevationClient client = new ElevationClientBuilder()
            .credential(tokenCredential)
            .elevationClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();
        // END: com.azure.maps.elevation.sync.builder.ad.instantiation

        return client;
    }

    public ElevationAsyncClient createMapsElevationAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.elevation.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a client
        ElevationAsyncClient asyncClient = new ElevationClientBuilder()
            .credential(keyCredential)
            .elevationClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildAsyncClient();
        // END: com.azure.maps.elevation.async.builder.key.instantiation

        return asyncClient;
    }

    public ElevationAsyncClient createMapsElevationAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.elevation.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a client
        ElevationAsyncClient asyncClient = new ElevationClientBuilder()
            .credential(tokenCredential)
            .elevationClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildAsyncClient();

        // END: com.azure.maps.elevation.async.builder.ad.instantiation

        return asyncClient;
    }
}
