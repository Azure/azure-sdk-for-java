// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

public final class RouterClientJavadocCodeSnippets {

    public JobRouterClient createRouterClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.routerclient.instantiation
        // Initialize the router client builder
        final JobRouterClientBuilder builder = new JobRouterClientBuilder()
            .connectionString(connectionString);
        // Build the router client
        JobRouterClient jobRouterClient = builder.buildClient();

        // END: com.azure.communication.jobrouter.routerclient.instantiation
        return jobRouterClient;
    }

    public JobRouterAsyncClient createRouterAsyncClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.routerasyncclient.instantiation
        // Initialize the router client builder
        final JobRouterClientBuilder builder = new JobRouterClientBuilder()
            .connectionString(connectionString);
        // Build the router client
        JobRouterAsyncClient jobRouterAsyncClient = builder.buildAsyncClient();

        // END: com.azure.communication.jobrouter.routerasyncclient.instantiation
        return jobRouterAsyncClient;
    }

    public JobRouterAdministrationClient createRouterAdminClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.routeradministrationclient.instantiation
        // Initialize the router administration client builder
        final JobRouterAdministrationClientBuilder builder = new JobRouterAdministrationClientBuilder()
            .connectionString(connectionString);
        // Build the router administration client
        JobRouterAdministrationClient jobRouterAdministrationClient = builder.buildClient();

        // END: com.azure.communication.jobrouter.routeradministrationclient.instantiation
        return jobRouterAdministrationClient;
    }

    public JobRouterAdministrationAsyncClient createRouterAdminAsyncClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.routeradministrationasyncclient.instantiation
        // Initialize the router administration client builder
        final JobRouterAdministrationClientBuilder builder = new JobRouterAdministrationClientBuilder()
            .connectionString(connectionString);
        // Build the router administration client
        JobRouterAdministrationAsyncClient routerAdministrationClient = builder.buildAsyncClient();

        // END: com.azure.communication.jobrouter.routeradministrationasyncclient.instantiation
        return routerAdministrationClient;
    }
}
