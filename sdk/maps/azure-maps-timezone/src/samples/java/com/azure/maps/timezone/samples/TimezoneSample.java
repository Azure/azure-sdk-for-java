// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.timezone.TimezoneAsyncClient;
import com.azure.maps.timezone.TimezoneClient;
import com.azure.maps.timezone.TimezoneClientBuilder;

public class TimezoneSample {
    public TimezoneClient createMapsSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.timezone.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        TimezoneClientBuilder builder = new TimezoneClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        TimezoneClient client = builder.buildClient();
        // END: com.azure.maps.timezone.sync.builder.key.instantiation

        return client;
    }

    public TimezoneClient createMapsSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.timezone.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        TimezoneClientBuilder builder = new TimezoneClientBuilder();
        builder.credential(tokenCredential);
        builder.timezoneClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        TimezoneClient client = builder.buildClient();
        // END: com.azure.maps.timezone.sync.builder.ad.instantiation

        return client;
    }

    public TimezoneAsyncClient createMapsSearchAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.timezone.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        TimezoneClientBuilder builder = new TimezoneClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        TimezoneAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.timezone.async.builder.key.instantiation

        return client;
    }

    public TimezoneAsyncClient createMapsSearchAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.timezone.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        TimezoneClientBuilder builder = new TimezoneClientBuilder();
        builder.credential(tokenCredential);
        builder.timezoneClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        TimezoneAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.timezone.async.builder.ad.instantiation

        return client;
    }
}
