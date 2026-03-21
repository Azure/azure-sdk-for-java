// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Connection;
import com.azure.ai.projects.models.ConnectionType;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;
import java.time.Duration;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class ConnectionsAsyncClientTest extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListConnectionsAsync(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsAsyncClient connectionsAsyncClient = getConnectionsAsyncClient(httpClient, serviceVersion);

        // Verify that listing connections returns results
        PagedFlux<Connection> connectionsFlux = connectionsAsyncClient.listConnections();
        Assertions.assertNotNull(connectionsFlux);

        // Verify the first connection if available
        StepVerifier.create(connectionsFlux.take(1))
            .assertNext(connection -> assertValidConnection(connection, null, null, null))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListConnectionsWithFiltersAsync(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsAsyncClient connectionsAsyncClient = getConnectionsAsyncClient(httpClient, serviceVersion);

        // Test listing connections with type filter
        PagedFlux<Connection> azureOpenAIConnections
            = connectionsAsyncClient.listConnections(ConnectionType.AZURE_OPEN_AI, null);
        Assertions.assertNotNull(azureOpenAIConnections);

        // Verify that all returned connections have the correct type
        StepVerifier.create(azureOpenAIConnections.take(10)).thenConsumeWhile(connection -> {
            assertValidConnection(connection, null, ConnectionType.AZURE_OPEN_AI, null);
            return true;
        }).verifyComplete();

        // Test listing default connections
        PagedFlux<Connection> defaultConnections = connectionsAsyncClient.listConnections(null, true);
        Assertions.assertNotNull(defaultConnections);

        // Verify that all returned connections are default connections
        StepVerifier.create(defaultConnections.take(10)).thenConsumeWhile(connection -> {
            assertValidConnection(connection, null, null, true);
            return true;
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetConnectionWithoutCredentialsAsync(HttpClient httpClient,
        AIProjectsServiceVersion serviceVersion) {
        ConnectionsAsyncClient connectionsAsyncClient = getConnectionsAsyncClient(httpClient, serviceVersion);

        // Discover a real connection name from the list
        String connectionName
            = connectionsAsyncClient.listConnections().next().map(Connection::getName).block(Duration.ofSeconds(20));
        Assertions.assertNotNull(connectionName, "Expected at least one connection");

        StepVerifier.create(connectionsAsyncClient.getConnection(connectionName)).assertNext(connection -> {
            assertValidConnection(connection, connectionName, null, null);
            Assertions.assertNotNull(connection.getCredentials().getType());
            System.out.println("Connection retrieved successfully: " + connection.getName());
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetConnectionWithCredentialsAsync(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsAsyncClient connectionsAsyncClient = getConnectionsAsyncClient(httpClient, serviceVersion);

        // Discover a real connection name from the list
        String connectionName
            = connectionsAsyncClient.listConnections().next().map(Connection::getName).block(Duration.ofSeconds(20));
        Assertions.assertNotNull(connectionName, "Expected at least one connection");

        StepVerifier.create(connectionsAsyncClient.getConnectionWithCredentials(connectionName))
            .assertNext(connection -> {
                assertValidConnection(connection, connectionName, null, null);
                Assertions.assertNotNull(connection.getCredentials().getType());
                System.out.println("Connection with credentials retrieved successfully: " + connection.getName());
                System.out.println("Credential type: " + connection.getCredentials().getType());
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDefaultConnectionAsync(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsAsyncClient connectionsAsyncClient = getConnectionsAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(connectionsAsyncClient.getDefaultConnection(ConnectionType.AZURE_OPEN_AI, false))
            .assertNext(connection -> {
                assertValidConnection(connection, null, ConnectionType.AZURE_OPEN_AI, null);
                Assertions.assertNotNull(connection.getCredentials().getType());
                System.out.println("Default connection retrieved: " + connection.getName());
            })
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetDefaultConnectionNotFoundAsync(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        ConnectionsAsyncClient connectionsAsyncClient = getConnectionsAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(connectionsAsyncClient.getDefaultConnection(ConnectionType.COSMOS_DB, false))
            .expectError(ResourceNotFoundException.class)
            .verify();
    }
}
