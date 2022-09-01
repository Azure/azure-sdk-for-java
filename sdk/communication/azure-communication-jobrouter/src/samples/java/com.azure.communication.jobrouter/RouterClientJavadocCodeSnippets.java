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

    public RouterAdministrationClient createRouterAdminClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.routeradministrationclient.instantiation
        // Initialize the router administration client builder
        final RouterAdministrationClientBuilder builder = new RouterAdministrationClientBuilder()
            .connectionString(connectionString);
        // Build the router administration client
        RouterAdministrationClient routerAdministrationClient = builder.buildClient();

        // END: com.azure.communication.jobrouter.routeradministrationclient.instantiation
        return routerAdministrationClient;
    }

    public RouterAdministrationAsyncClient createRouterAdminAsyncClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.routeradministrationasyncclient.instantiation
        // Initialize the router administration client builder
        final RouterAdministrationClientBuilder builder = new RouterAdministrationClientBuilder()
            .connectionString(connectionString);
        // Build the router administration client
        RouterAdministrationAsyncClient routerAdministrationClient = builder.buildAsyncClient();

        // END: com.azure.communication.jobrouter.routeradministrationasyncclient.instantiation
        return routerAdministrationClient;
    }
}
