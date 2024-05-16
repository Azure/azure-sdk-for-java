// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.search.MapsSearchAsyncClient;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;

public class SearchSample {
    public MapsSearchClient createMapsSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.search.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        MapsSearchClientBuilder builder = new MapsSearchClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        MapsSearchClient client = builder.buildClient();
        // END: com.azure.maps.search.sync.builder.key.instantiation

        return client;
    }

    public MapsSearchClient createMapsSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.search.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        MapsSearchClientBuilder builder = new MapsSearchClientBuilder();
        builder.credential(tokenCredential);
        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        MapsSearchClient client = builder.buildClient();
        // END: com.azure.maps.search.sync.builder.ad.instantiation

        return client;
    }

    public MapsSearchAsyncClient createMapsSearchAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.search.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        MapsSearchClientBuilder builder = new MapsSearchClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        MapsSearchAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.search.async.builder.key.instantiation

        return client;
    }

    public MapsSearchAsyncClient createMapsSearchAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.search.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        MapsSearchClientBuilder builder = new MapsSearchClientBuilder();
        builder.credential(tokenCredential);
        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        MapsSearchAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.search.async.builder.ad.instantiation

        return client;
    }
}
