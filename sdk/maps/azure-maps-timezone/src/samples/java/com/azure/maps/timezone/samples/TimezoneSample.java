// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.timezone.TimeZoneAsyncClient;
import com.azure.maps.timezone.TimeZoneClient;
import com.azure.maps.timezone.TimeZoneClientBuilder;

public class TimezoneSample {
    public TimeZoneClient createMapsSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.timezone.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a client
        TimeZoneClient client = new TimeZoneClientBuilder() 
            .credential(keyCredential)
            .timezoneClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();
        // END: com.azure.maps.timezone.sync.builder.key.instantiation

        return client;
    }

    public TimeZoneClient createMapsSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.timezone.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a client
        TimeZoneClient client = new TimeZoneClientBuilder()
            .credential(tokenCredential)
            .timezoneClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();
        // END: com.azure.maps.timezone.sync.builder.ad.instantiation

        return client;
    }

    public TimeZoneAsyncClient createMapsSearchAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.timezone.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a client
        TimeZoneAsyncClient asyncClient = new TimeZoneClientBuilder()
            .credential(keyCredential)
            .timezoneClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildAsyncClient();

        // END: com.azure.maps.timezone.async.builder.key.instantiation

        return asyncClient;
    }

    public TimeZoneAsyncClient createMapsSearchAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.timezone.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a client
        TimeZoneAsyncClient asyncClient = new TimeZoneClientBuilder()
            .credential(tokenCredential)
            .timezoneClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildAsyncClient();

        // END: com.azure.maps.timezone.async.builder.ad.instantiation

        return asyncClient;
    }
}
