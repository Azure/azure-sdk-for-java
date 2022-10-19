// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.render.MapsRenderAsyncClient;
import com.azure.maps.render.MapsRenderClient;
import com.azure.maps.render.MapsRenderClientBuilder;

public class RenderSamples {
    public MapsRenderClient createRenderSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.render.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        MapsRenderClientBuilder builder = new MapsRenderClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        MapsRenderClient client = builder.buildClient();
        // END: com.azure.maps.render.sync.builder.key.instantiation

        return client;
    }

    public MapsRenderClient createRenderSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.render.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        MapsRenderClientBuilder builder = new MapsRenderClientBuilder();
        builder.credential(tokenCredential);
        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        MapsRenderClient client = builder.buildClient();
        // END: com.azure.maps.render.sync.builder.ad.instantiation

        return client;
    }

    public MapsRenderAsyncClient createRenderSearchAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.render.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        MapsRenderClientBuilder builder = new MapsRenderClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        MapsRenderAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.render.async.builder.key.instantiation

        return client;
    }

    public MapsRenderAsyncClient createRenderSearchAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.render.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        MapsRenderClientBuilder builder = new MapsRenderClientBuilder();
        builder.credential(tokenCredential);
        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        MapsRenderAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.render.async.builder.ad.instantiation

        return client;
    }
}
