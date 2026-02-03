// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Connection;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ConnectionsSample {

    private static ConnectionsClient connectionsClient
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("AZURE_AI_PROJECTS_ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildConnectionsClient();

    public static void main(String[] args) {

//        listConnections();
//        getConnectionWithoutCredentials();
//        getConnectionWithCredentials();
    }

    public static void listConnections() {
        // BEGIN:com.azure.ai.projects.ConnectionsSample.listConnections
        PagedIterable<Connection> connections = connectionsClient.listConnections();
        for (Connection connection : connections) {
            System.out.println("Connection name: " + connection.getName());
            System.out.println("Connection type: " + connection.getType());
            System.out.println("Connection credential type: " + connection.getCredentials().getType());
            System.out.println("-------------------------------------------------");
        }
        // END:com.azure.ai.projects.ConnectionsSample.listConnections
    }

    public static void getConnectionWithoutCredentials() {
        // BEGIN:com.azure.ai.projects.ConnectionsSample.getConnectionWithoutCredentials

        String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "");
        Connection connection = connectionsClient.getConnection(connectionName);

        System.out.printf("Connection name: %s%n", connection.getName());

        // END:com.azure.ai.projects.ConnectionsSample.getConnectionWithoutCredentials
    }

    public static void getConnectionWithCredentials() {
        // BEGIN:com.azure.ai.projects.ConnectionsSample.getConnectionWithCredentials

        String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "");
        Connection connection = connectionsClient.getConnectionWithCredentials(connectionName);

        System.out.printf("Connection name: %s%n", connection.getName());
        System.out.printf("Connection credentials: %s%n", connection.getCredentials().getType());

        // END:com.azure.ai.projects.ConnectionsSample.getConnectionWithCredentials
    }
}
