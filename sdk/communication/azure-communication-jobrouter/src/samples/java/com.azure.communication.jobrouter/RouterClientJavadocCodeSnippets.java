// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

public final class RouterClientJavadocCodeSnippets {

    public JobRouterClient createRouterClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.jobrouterclient.instantiation
        // Initialize the jobrouter client builder
        final JobRouterClientBuilder builder = new JobRouterClientBuilder();
        // Build the jobrouter client
        JobRouterClient jobRouterClient = builder.buildClient();

        // END: com.azure.communication.jobrouter.jobrouterclient.instantiation
        return jobRouterClient;
    }

    public JobRouterAsyncClient createRouterAsyncClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.jobrouterasyncclient.instantiation
        // Initialize the jobrouter client builder
        final JobRouterClientBuilder builder = new JobRouterClientBuilder();
        // Build the jobrouter client
        JobRouterAsyncClient jobRouterAsyncClient = builder.buildAsyncClient();

        // END: com.azure.communication.jobrouter.jobrouterasyncclient.instantiation
        return jobRouterAsyncClient;
    }

    public JobRouterAdministrationClient createRouterAdminClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.jobrouteradministrationclient.instantiation
        // Initialize the jobrouter administration client builder
        final JobRouterAdministrationClientBuilder builder = new JobRouterAdministrationClientBuilder();
        // Build the jobrouter administration client
        JobRouterAdministrationClient jobRouterAdministrationClient = builder
            .connectionString(connectionString)
            .buildClient();

        // END: com.azure.communication.jobrouter.jobrouteradministrationclient.instantiation
        return jobRouterAdministrationClient;
    }

    public JobRouterAdministrationAsyncClient createRouterAdminAsyncClient() {
        String connectionString = "endpoint=https://<RESOURCE_NAME>.communication.azure.com;accesskey=<ACCESS_KEY>";

        // BEGIN: com.azure.communication.jobrouter.jobrouteradministrationasyncclient.instantiation
        // Initialize the jobrouter administration client builder
        final JobRouterAdministrationClientBuilder builder = new JobRouterAdministrationClientBuilder();
        // Build the jobrouter administration client
        JobRouterAdministrationAsyncClient jobrouterAdministrationClient = builder
            .connectionString(connectionString)
            .buildAsyncClient();

        // END: com.azure.communication.jobrouter.jobrouteradministrationasyncclient.instantiation
        return jobrouterAdministrationClient;
    }
}
