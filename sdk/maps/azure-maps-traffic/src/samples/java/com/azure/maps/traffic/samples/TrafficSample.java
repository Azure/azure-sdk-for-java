// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.traffic.TrafficAsyncClient;
import com.azure.maps.traffic.TrafficClient;
import com.azure.maps.traffic.TrafficClientBuilder;

public class TrafficSample {
    public TrafficClient createMapsSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.traffic.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        TrafficClientBuilder builder = new TrafficClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        TrafficClient client = builder.buildClient();
        // END: com.azure.maps.traffic.sync.builder.key.instantiation

        return client;
    }

    public TrafficClient createMapsSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.traffic.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        TrafficClientBuilder builder = new TrafficClientBuilder();
        builder.credential(tokenCredential);
        builder.trafficClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        TrafficClient client = builder.buildClient();
        // END: com.azure.maps.traffic.sync.builder.ad.instantiation

        return client;
    }

    public TrafficAsyncClient createMapsTrafficAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.traffic.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        TrafficClientBuilder builder = new TrafficClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        TrafficAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.traffic.async.builder.key.instantiation

        return client;
    }

    public TrafficAsyncClient createMapsTrafficAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.traffic.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        TrafficClientBuilder builder = new TrafficClientBuilder();
        builder.credential(tokenCredential);
        builder.trafficClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        TrafficAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.traffic.async.builder.ad.instantiation

        return client;
    }
}
