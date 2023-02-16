// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.samples;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.route.MapsRouteAsyncClient;
import com.azure.maps.route.MapsRouteClient;
import com.azure.maps.route.MapsRouteClientBuilder;

public class RouteSample {
    public MapsRouteClient createRouteSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.route.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        MapsRouteClientBuilder builder = new MapsRouteClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        MapsRouteClient client = builder.buildClient();
        // END: com.azure.maps.route.sync.builder.key.instantiation

        return client;
    }

    public MapsRouteClient createRouteSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.route.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        MapsRouteClientBuilder builder = new MapsRouteClientBuilder();
        builder.credential(tokenCredential);
        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        MapsRouteClient client = builder.buildClient();
        // END: com.azure.maps.route.sync.builder.ad.instantiation

        return client;
    }

    public MapsRouteAsyncClient createRouteAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.route.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        MapsRouteClientBuilder builder = new MapsRouteClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        MapsRouteAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.route.async.builder.key.instantiation

        return client;
    }

    public MapsRouteAsyncClient createRouteAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.route.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        MapsRouteClientBuilder builder = new MapsRouteClientBuilder();
        builder.credential(tokenCredential);
        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        MapsRouteAsyncClient client = builder.buildAsyncClient();
        // END: com.azure.maps.route.async.builder.ad.instantiation

        return client;
    }
}
