// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.networktraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.RouteType;
import com.azure.communication.networktraversal.models.CommunicationIceServer;
import com.azure.communication.networktraversal.models.GetRelayConfigurationOptions;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneOffset;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;

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
            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            options.setCommunicationUserIdentifier(user);
            Mono<CommunicationRelayConfiguration> relayResponse = asyncClient.getRelayConfiguration(options);

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
            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            options.setCommunicationUserIdentifier(user);
            options.setRouteType(RouteType.ANY);

            Mono<CommunicationRelayConfiguration> relayResponse = asyncClient.getRelayConfiguration(options);

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
            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            options.setCommunicationUserIdentifier(user);

            Mono<CommunicationRelayConfiguration> relayResponse = asyncClient.getRelayConfiguration(options);

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
    public void getRelayConfigWithResponseWithRouteTypeNearest(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createRelayClientUsingManagedIdentityAsync");

        // Action & Assert
        assertNotNull(asyncClient);
        assertNotNull(user.getId());

        if (user != null) {
            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            options.setCommunicationUserIdentifier(user);
            options.setRouteType(RouteType.NEAREST);

            Mono<Response<CommunicationRelayConfiguration>> relayConfig = asyncClient.getRelayConfigurationWithResponse(options, null);

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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRelayConfigWithResponseWithTtl(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createRelayClientUsingManagedIdentityAsync");

        // Action & Assert
        assertNotNull(asyncClient);

        if (user != null) {
            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            int ttl = 5000;
            options.setTtl(ttl);

            Instant now = Instant.now();
            OffsetDateTime requestedTime = now.atOffset(ZoneOffset.UTC).plusSeconds(ttl);

            Mono<Response<CommunicationRelayConfiguration>> relayConfig = asyncClient.getRelayConfigurationWithResponse(options, null);

            StepVerifier.create(relayConfig)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
                assertNotNull(response.getValue().getIceServers());

                if (getTestMode() != TestMode.PLAYBACK) {
                    assertTrue(requestedTime.compareTo(response.getValue().getExpiresOn()) <= 0);
                    OffsetDateTime limitedTime = requestedTime.plusSeconds(10);
                    assertTrue(response.getValue().getExpiresOn().compareTo(limitedTime) < 0);
                }

                for (CommunicationIceServer iceS : response.getValue().getIceServers()) {
                    assertNotNull(iceS.getUrls());
                    assertNotNull(iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                }
            }).verifyComplete();
        }
    }

    private CommunicationRelayAsyncClient setupAsyncClient(CommunicationRelayClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
