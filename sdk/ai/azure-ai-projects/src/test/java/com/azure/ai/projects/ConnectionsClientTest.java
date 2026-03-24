// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Connection;
import com.azure.ai.projects.models.ConnectionType;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class ConnectionsClientTest extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListConnections(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsClient connectionsClient = getConnectionsClient(httpClient, serviceVersion);

        // Verify that listing connections returns results
        Iterable<Connection> connections = connectionsClient.listConnections();
        Assertions.assertNotNull(connections);

        // Verify that at least one connection can be retrieved if available
        boolean hasAtLeastOneConnection = false;
        for (Connection connection : connections) {
            hasAtLeastOneConnection = true;
            assertValidConnection(connection, null, null, null);
            break;
        }

        System.out.println("Connection list retrieved successfully"
            + (hasAtLeastOneConnection ? " with at least one connection" : " (empty list)"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListConnectionsWithFilters(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsClient connectionsClient = getConnectionsClient(httpClient, serviceVersion);

        // Test listing connections with type filter
        Iterable<Connection> azureOpenAIConnections
            = connectionsClient.listConnections(ConnectionType.AZURE_OPEN_AI, null);
        Assertions.assertNotNull(azureOpenAIConnections);

        // Verify that all returned connections have the correct type
        azureOpenAIConnections.forEach(connection -> {
            assertValidConnection(connection, null, ConnectionType.AZURE_OPEN_AI, null);
        });

        // Test listing default connections
        Iterable<Connection> defaultConnections = connectionsClient.listConnections(null, true);
        Assertions.assertNotNull(defaultConnections);

        // Verify that all returned connections are default connections
        defaultConnections.forEach(connection -> {
            assertValidConnection(connection, null, null, true);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetConnectionWithoutCredentials(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsClient connectionsClient = getConnectionsClient(httpClient, serviceVersion);

        // Discover a real connection name from the list
        String connectionName = null;
        for (Connection c : connectionsClient.listConnections()) {
            connectionName = c.getName();
            break;
        }
        Assertions.assertNotNull(connectionName, "Expected at least one connection to test getConnection");

        Connection connection = connectionsClient.getConnection(connectionName);

        // Verify the connection properties
        assertValidConnection(connection, connectionName, null, null);
        Assertions.assertNotNull(connection.getCredentials().getType());

        System.out.println("Connection retrieved successfully: " + connection.getName());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetConnectionWithCredentials(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsClient connectionsClient = getConnectionsClient(httpClient, serviceVersion);

        // Discover a real connection name from the list
        String connectionName = null;
        for (Connection c : connectionsClient.listConnections()) {
            connectionName = c.getName();
            break;
        }
        Assertions.assertNotNull(connectionName,
            "Expected at least one connection to test getConnectionWithCredentials");

        Connection connection = connectionsClient.getConnectionWithCredentials(connectionName);

        // Verify the connection properties
        assertValidConnection(connection, connectionName, null, null);
        Assertions.assertNotNull(connection.getCredentials().getType());

        System.out.println("Connection with credentials retrieved successfully: " + connection.getName());
        System.out.println("Credential type: " + connection.getCredentials().getType());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDefaultConnection(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsClient connectionsClient = getConnectionsClient(httpClient, serviceVersion);

        Connection connection = connectionsClient.getDefaultConnection(ConnectionType.AZURE_OPEN_AI, false);

        assertValidConnection(connection, null, ConnectionType.AZURE_OPEN_AI, null);
        Assertions.assertNotNull(connection.getCredentials().getType());

        System.out.println("Default connection retrieved: " + connection.getName());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDefaultConnectionNotFound(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsClient connectionsClient = getConnectionsClient(httpClient, serviceVersion);

        Assertions.assertThrows(IllegalStateException.class,
            () -> connectionsClient.getDefaultConnection(ConnectionType.COSMOS_DB, false));
    }
}
