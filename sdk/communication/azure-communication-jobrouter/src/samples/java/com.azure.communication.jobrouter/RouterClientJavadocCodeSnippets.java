// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

public final class RouterClientJavadocCodeSnippets {

    public RouterClient createRouterClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.routerclient.instantiation
        // Initialize the router client builder
        final RouterClientBuilder builder = new RouterClientBuilder()
            .connectionString(connectionString);
        // Build the router client
        RouterClient routerClient = builder.buildClient();

        // END: com.azure.communication.jobrouter.routerclient.instantiation
        return routerClient;
    }

    public RouterAsyncClient createRouterAsyncClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.routerasyncclient.instantiation
        // Initialize the router client builder
        final RouterClientBuilder builder = new RouterClientBuilder()
            .connectionString(connectionString);
        // Build the router client
        RouterAsyncClient routerAsyncClient = builder.buildAsyncClient();

        // END: com.azure.communication.jobrouter.routerasyncclient.instantiation
        return routerAsyncClient;
    }
}
