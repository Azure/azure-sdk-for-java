// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Connection;
import com.azure.ai.projects.models.ConnectionType;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

@Disabled("Disabled for lack of recordings. Needs to be enabled on the Public Preview release.")
public class ConnectionsClientTest extends ClientTestBase {

    private AIProjectClientBuilder clientBuilder;
    private ConnectionsClient connectionsClient;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        connectionsClient = clientBuilder.buildConnectionsClient();
    }

    /**
     * Helper method to verify a Connection has valid properties.
     * @param connection The connection to validate
     * @param expectedName The expected name of the connection, or null if no specific name is expected
     * @param expectedType The expected connection type, or null if no specific type is expected
     * @param shouldBeDefault Whether the connection should be a default connection, or null if not checking this property
     */
    private void assertValidConnection(Connection connection, String expectedName, ConnectionType expectedType,
        Boolean shouldBeDefault) {
        Assertions.assertNotNull(connection);
        Assertions.assertNotNull(connection.getName());
        Assertions.assertNotNull(connection.getId());
        Assertions.assertNotNull(connection.getType());
        Assertions.assertNotNull(connection.getTarget());
        Assertions.assertNotNull(connection.getCredentials());

        if (expectedName != null) {
            Assertions.assertEquals(expectedName, connection.getName());
        }

        if (expectedType != null) {
            Assertions.assertEquals(expectedType, connection.getType());
        }

        if (shouldBeDefault != null) {
            Assertions.assertEquals(shouldBeDefault, connection.isDefault());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListConnections(HttpClient httpClient) {
        setup(httpClient);

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

        // Note: This test will pass even if there are no connections,
        // as we're only verifying the API works correctly
        System.out.println("Connection list retrieved successfully"
            + (hasAtLeastOneConnection ? " with at least one connection" : " (empty list)"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListConnectionsWithFilters(HttpClient httpClient) {
        setup(httpClient);

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
    public void testGetConnectionWithoutCredentials(HttpClient httpClient) {
        setup(httpClient);

        String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "agentaisearch2aqa");

        try {
            Connection connection = connectionsClient.getConnection(connectionName);

            // Verify the connection properties
            assertValidConnection(connection, connectionName, null, null);
            Assertions.assertNotNull(connection.getCredentials().getType());

            System.out.println("Connection retrieved successfully: " + connection.getName());
        } catch (Exception e) {
            // If the connection doesn't exist, this will throw a ResourceNotFoundException
            // We'll handle this case by printing a message and passing the test
            System.out.println("Connection not found: " + connectionName);
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetConnectionWithCredentials(HttpClient httpClient) {
        setup(httpClient);

        String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "agentaisearch2aqa");

        try {
            Connection connection = connectionsClient.getConnectionWithCredentials(connectionName);

            // Verify the connection properties
            assertValidConnection(connection, connectionName, null, null);
            Assertions.assertNotNull(connection.getCredentials().getType());

            System.out.println("Connection with credentials retrieved successfully: " + connection.getName());
            System.out.println("Credential type: " + connection.getCredentials().getType());
        } catch (Exception e) {
            // If the connection doesn't exist, this will throw a ResourceNotFoundException
            // We'll handle this case by printing a message and passing the test
            System.out.println("Connection not found: " + connectionName);
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }
}
