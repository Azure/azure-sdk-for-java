// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Connection;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ConnectionsAsyncSample {

    private static ConnectionsAsyncClient connectionsAsyncClient
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildConnectionsAsyncClient();

    public static void main(String[] args) {
        // Using block() to wait for the async operations to complete in the sample
        listConnections().blockLast();
        getConnectionWithoutCredentials().block();
        getConnectionWithCredentials().block();
    }

    public static Flux<Connection> listConnections() {
        // BEGIN:com.azure.ai.projects.ConnectionsAsyncSample.listConnections

        return connectionsAsyncClient.listConnections()
            .doOnNext(connection -> System.out.printf("Connection name: %s%n", connection.getName()));

        // END:com.azure.ai.projects.ConnectionsAsyncSample.listConnections
    }

    public static Mono<Connection> getConnectionWithoutCredentials() {
        // BEGIN:com.azure.ai.projects.ConnectionsAsyncSample.getConnectionWithoutCredentials

        String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "");
        return connectionsAsyncClient.getConnection(connectionName)
            .doOnNext(connection -> System.out.printf("Connection name: %s%n", connection.getName()));

        // END:com.azure.ai.projects.ConnectionsAsyncSample.getConnectionWithoutCredentials
    }

    public static Mono<Connection> getConnectionWithCredentials() {
        // BEGIN:com.azure.ai.projects.ConnectionsAsyncSample.getConnectionWithCredentials

        String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "");
        return connectionsAsyncClient.getConnectionWithCredentials(connectionName)
            .doOnNext(connection -> {
                System.out.printf("Connection name: %s%n", connection.getName());
                System.out.printf("Connection credentials: %s%n", connection.getCredentials().getType());
            });

        // END:com.azure.ai.projects.ConnectionsAsyncSample.getConnectionWithCredentials
    }
}
