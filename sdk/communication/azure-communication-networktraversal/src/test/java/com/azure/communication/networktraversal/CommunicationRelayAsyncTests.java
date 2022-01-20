// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.networktraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.RouteType;
import com.azure.communication.networktraversal.models.CommunicationIceServer;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class CommunicationRelayAsyncTests extends CommunicationRelayClientTestBase {
    private CommunicationRelayAsyncClient asyncClient;
    private CommunicationUserIdentifier user;

    private void setupTest(HttpClient httpClient) {
        CommunicationIdentityClient communicationIdentityClient = createIdentityClientBuilder(httpClient).buildClient();
        user = communicationIdentityClient.createUser();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createRelayClientUsingManagedIdentityAsync");

        // Action & Assert
        assertNotNull(asyncClient);
        assertNotNull(user.getId());

        if (user != null) {
            Mono<CommunicationRelayConfiguration> relayResponse = asyncClient.getRelayConfiguration(user);

            StepVerifier.create(relayResponse)
            .assertNext(relayConfig -> {
                assertNotNull(relayConfig.getIceServers());
                for (CommunicationIceServer iceS : relayConfig.getIceServers()) {
                    assertNotNull(iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                }
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientWithoutUserIdUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createRelayClientUsingManagedIdentityAsync");

        // Action & Assert
        assertNotNull(asyncClient);

        if (user != null) {
            Mono<CommunicationRelayConfiguration> relayResponse = asyncClient.getRelayConfiguration();

            StepVerifier.create(relayResponse)
            .assertNext(relayConfig -> {
                assertNotNull(relayConfig.getIceServers());
                for (CommunicationIceServer iceS : relayConfig.getIceServers()) {
                    assertNotNull(iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                }
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientUsingManagedIdentityWithRouteTypeAny(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createRelayClientUsingManagedIdentityAsync");

        // Action & Assert
        assertNotNull(asyncClient);
        assertNotNull(user.getId());

        if (user != null) {
            Mono<CommunicationRelayConfiguration> relayResponse = asyncClient.getRelayConfiguration(user, RouteType.ANY);

            StepVerifier.create(relayResponse)
            .assertNext(relayConfig -> {
                assertNotNull(relayConfig.getIceServers());
                for (CommunicationIceServer iceS : relayConfig.getIceServers()) {
                    assertNotNull(iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                    assertEquals(RouteType.ANY, iceS.getRouteType());
                }
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientUsingConnectionString(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CommunicationRelayClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "createIdentityClientUsingConnectionStringAsync");

        // Action & Assert
        assertNotNull(asyncClient);
        assertNotNull(user.getId());
        if (user != null) {
            Mono<CommunicationRelayConfiguration> relayResponse = asyncClient.getRelayConfiguration(user);

            StepVerifier.create(relayResponse)
            .assertNext(relayConfig -> {
                assertNotNull(relayConfig.getIceServers());
                for (CommunicationIceServer iceS : relayConfig.getIceServers()) {
                    assertNotNull(iceS.getUrls());
                    assertNotNull(iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                }
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientWithoutUserIdUsingConnectionString(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CommunicationRelayClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "createIdentityClientUsingConnectionStringAsync");

        // Action & Assert
        assertNotNull(asyncClient);
        if (user != null) {
            Mono<CommunicationRelayConfiguration> relayResponse = asyncClient.getRelayConfiguration();

            StepVerifier.create(relayResponse)
            .assertNext(relayConfig -> {
                assertNotNull(relayConfig.getIceServers());
                for (CommunicationIceServer iceS : relayConfig.getIceServers()) {
                    assertNotNull(iceS.getUrls());
                    assertNotNull(iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                }
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRelayConfigWithoutUserIdWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createRelayClientUsingManagedIdentityAsync");

        // Action & Assert
        assertNotNull(asyncClient);

        if (user != null) {
            Mono<Response<CommunicationRelayConfiguration>> relayConfig = asyncClient.getRelayConfigurationWithResponse();

            StepVerifier.create(relayConfig)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
                assertNotNull(response.getValue().getIceServers());
                for (CommunicationIceServer iceS : response.getValue().getIceServers()) {
                    assertNotNull(iceS.getUrls());
                    assertNotNull(iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                }
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRelayConfigWithResponseWithRouteTypeNearest(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createRelayClientUsingManagedIdentityAsync");

        // Action & Assert
        assertNotNull(asyncClient);
        assertNotNull(user.getId());

        if (user != null) {
            Mono<Response<CommunicationRelayConfiguration>> relayConfig = asyncClient.getRelayConfigurationWithResponse(user, RouteType.NEAREST, null);

            StepVerifier.create(relayConfig)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
                assertNotNull(response.getValue().getIceServers());
                for (CommunicationIceServer iceS : response.getValue().getIceServers()) {
                    assertNotNull(iceS.getUrls());
                    assertNotNull(iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                    assertEquals(RouteType.NEAREST, iceS.getRouteType());
                }
            }).verifyComplete();
        }
    }

    private CommunicationRelayAsyncClient setupAsyncClient(CommunicationRelayClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
