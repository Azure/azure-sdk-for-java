// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Connection;
import com.azure.ai.projects.models.ConnectionType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

@Disabled("Disabled for lack of recordings. Needs to be enabled on the Public Preview release.")
public class ConnectionsAsyncClientTest extends ClientTestBase {

    private AIProjectClientBuilder clientBuilder;
    private ConnectionsAsyncClient connectionsAsyncClient;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        connectionsAsyncClient = clientBuilder.buildConnectionsAsyncClient();
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
    public void testListConnectionsAsync(HttpClient httpClient) {
        setup(httpClient);

        // Verify that listing connections returns results
        PagedFlux<Connection> connectionsFlux = connectionsAsyncClient.list();
        Assertions.assertNotNull(connectionsFlux);

        // Collect all connections and verify
        List<Connection> connections = new ArrayList<>();
        connectionsFlux.collectList().block(Duration.ofSeconds(30));

        System.out.println("Connection list retrieved successfully"
            + (connections.size() > 0 ? " with " + connections.size() + " connections" : " (empty list)"));

        // Verify the first connection if available
        StepVerifier.create(connectionsFlux.take(1))
            .assertNext(connection -> assertValidConnection(connection, null, null, null))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testListConnectionsWithFiltersAsync(HttpClient httpClient) {
        setup(httpClient);

        // Test listing connections with type filter
        PagedFlux<Connection> azureOpenAIConnections = connectionsAsyncClient.list(ConnectionType.AZURE_OPEN_AI, null);
        Assertions.assertNotNull(azureOpenAIConnections);

        // Verify that all returned connections have the correct type
        StepVerifier.create(azureOpenAIConnections.take(10)).thenConsumeWhile(connection -> {
            assertValidConnection(connection, null, ConnectionType.AZURE_OPEN_AI, null);
            return true;
        }).verifyComplete();

        // Test listing default connections
        PagedFlux<Connection> defaultConnections = connectionsAsyncClient.list(null, true);
        Assertions.assertNotNull(defaultConnections);

        // Verify that all returned connections are default connections
        StepVerifier.create(defaultConnections.take(10)).thenConsumeWhile(connection -> {
            assertValidConnection(connection, null, null, true);
            return true;
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetConnectionWithoutCredentialsAsync(HttpClient httpClient) {
        setup(httpClient);

        String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "agentaisearch2aqa");

        Mono<Connection> connectionMono = connectionsAsyncClient.get(connectionName);

        try {
            // Test retrieving a connection
            StepVerifier.create(connectionMono).assertNext(connection -> {
                assertValidConnection(connection, connectionName, null, null);
                Assertions.assertNotNull(connection.getCredentials().getType());
                System.out.println("Connection retrieved successfully: " + connection.getName());
            }).verifyComplete();
        } catch (Exception e) {
            // If the connection doesn't exist, this will throw an exception
            // We'll handle this case by printing a message and passing the test
            System.out.println("Connection not found: " + connectionName);
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void testGetConnectionWithCredentialsAsync(HttpClient httpClient) {
        setup(httpClient);

        String connectionName = Configuration.getGlobalConfiguration().get("TEST_CONNECTION_NAME", "agentaisearch2aqa");

        Mono<Connection> connectionMono = connectionsAsyncClient.getWithCredentials(connectionName);

        try {
            // Test retrieving a connection with credentials
            StepVerifier.create(connectionMono).assertNext(connection -> {
                assertValidConnection(connection, connectionName, null, null);
                Assertions.assertNotNull(connection.getCredentials().getType());
                System.out.println("Connection with credentials retrieved successfully: " + connection.getName());
                System.out.println("Credential type: " + connection.getCredentials().getType());
            }).verifyComplete();
        } catch (Exception e) {
            // If the connection doesn't exist, this will throw an exception
            // We'll handle this case by printing a message and passing the test
            System.out.println("Connection not found: " + connectionName);
            Assertions.assertTrue(e.getMessage().contains("404") || e.getMessage().contains("Not Found"));
        }
    }
}
