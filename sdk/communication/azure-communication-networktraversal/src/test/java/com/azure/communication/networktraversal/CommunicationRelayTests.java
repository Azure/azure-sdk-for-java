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
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.List;

public class CommunicationRelayTests extends CommunicationRelayClientTestBase {
    private CommunicationRelayClient client;
    private CommunicationUserIdentifier user;

    @Override
    protected void afterTest() {
        super.afterTest();
    }

    private void setupTest(HttpClient httpClient) {
        CommunicationIdentityClient communicationIdentityClient = createIdentityClientBuilder(httpClient).buildClient();
        user = communicationIdentityClient.createUser();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        try {
            setupTest(httpClient);
            CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
            client = setupClient(builder, "createRelayClientUsingManagedIdentitySync");

            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            options.setCommunicationUserIdentifier(user);

            // Action & Assert
            assertNotNull(client);

            CommunicationRelayConfiguration config = client.getRelayConfiguration(options);
            List<CommunicationIceServer> iceServers = config.getIceServers();

            assertNotNull(config);
            assertNotNull(config.getExpiresOn());

            for (CommunicationIceServer iceS : iceServers) {
                assertNotNull(iceS.getUrls());
                assertNotNull(iceS.getUsername());
                assertNotNull(iceS.getCredential());
                assertNotNull(iceS.getRouteType());
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientWithoutUserIdUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        try {
            setupTest(httpClient);
            CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
            client = setupClient(builder, "createRelayClientUsingManagedIdentitySync");

            // Action & Assert
            assertNotNull(client);
            CommunicationRelayConfiguration config = client.getRelayConfiguration();
            List<CommunicationIceServer> iceServers = config.getIceServers();

            assertNotNull(config);
            assertNotNull(config.getExpiresOn());

            for (CommunicationIceServer iceS : iceServers) {
                assertNotNull(iceS.getUrls());
                assertNotNull(iceS.getUsername());
                assertNotNull(iceS.getCredential());
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientUsingManagedIdentityWithRouteTypeAny(HttpClient httpClient) {
        // Arrange
        try {
            setupTest(httpClient);
            CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
            client = setupClient(builder, "createRelayClientUsingManagedIdentitySync");

            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            options.setCommunicationUserIdentifier(user);
            options.setRouteType(RouteType.ANY);

            // Action & Assert
            assertNotNull(client);

            CommunicationRelayConfiguration config = client.getRelayConfiguration(options);
            List<CommunicationIceServer> iceServers = config.getIceServers();

            assertNotNull(config);
            assertNotNull(config.getExpiresOn());

            for (CommunicationIceServer iceS : iceServers) {
                assertNotNull(iceS.getUrls());
                assertNotNull(iceS.getUsername());
                assertNotNull(iceS.getCredential());
                assertEquals(RouteType.ANY, iceS.getRouteType());
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientUsingConnectionString(HttpClient httpClient) {
        // Arrange
        try {
            setupTest(httpClient);
            CommunicationRelayClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
            client = setupClient(builder, "createIdentityClientUsingConnectionStringSync");
            assertNotNull(client);

            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            options.setCommunicationUserIdentifier(user);

            CommunicationRelayConfiguration config = client.getRelayConfiguration(options);

            // Action & Assert
            List<CommunicationIceServer> iceServers = config.getIceServers();
            assertNotNull(config);
            assertNotNull(config.getExpiresOn());

            for (CommunicationIceServer iceS : iceServers) {
                assertNotNull(iceS.getUrls());
                assertNotNull(iceS.getUsername());
                assertNotNull(iceS.getCredential());
                assertNotNull(iceS.getRouteType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientWithoutUserIdUsingConnectionString(HttpClient httpClient) {
        // Arrange
        try {
            setupTest(httpClient);
            CommunicationRelayClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
            client = setupClient(builder, "createIdentityClientUsingConnectionStringSync");
            assertNotNull(client);
            CommunicationRelayConfiguration config = client.getRelayConfiguration();

            // Action & Assert
            List<CommunicationIceServer> iceServers = config.getIceServers();
            assertNotNull(config);
            assertNotNull(config.getExpiresOn());

            for (CommunicationIceServer iceS : iceServers) {
                assertNotNull(iceS.getUrls());
                assertNotNull(iceS.getUsername());
                assertNotNull(iceS.getCredential());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientUsingConnectionStringWithRouteTypeNearest(HttpClient httpClient) {
        // Arrange
        try {
            setupTest(httpClient);
            CommunicationRelayClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
            client = setupClient(builder, "createIdentityClientUsingConnectionStringSync");

            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            options.setCommunicationUserIdentifier(user);
            options.setRouteType(RouteType.NEAREST);

            CommunicationRelayConfiguration config = client.getRelayConfiguration(options);

            // Action & Assert
            assertNotNull(client);

            List<CommunicationIceServer> iceServers = config.getIceServers();
            assertNotNull(config);
            assertNotNull(config.getExpiresOn());

            for (CommunicationIceServer iceS : iceServers) {
                assertNotNull(iceS.getUrls());
                assertNotNull(iceS.getUsername());
                assertNotNull(iceS.getCredential());
                assertEquals(RouteType.NEAREST, iceS.getRouteType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRelayConfigWithResponseWithRouteTypeNearest(HttpClient httpClient) {
        // Arrange
        try {
            setupTest(httpClient);
            CommunicationRelayClientBuilder builder = createClientBuilder(httpClient);
            client = setupClient(builder, "getRelayConfigWithResponse");
            Response<CommunicationRelayConfiguration> response;

            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            options.setCommunicationUserIdentifier(user);
            options.setRouteType(RouteType.NEAREST);

            // Action & Assert
            response = client.getRelayConfigurationWithResponse(options, Context.NONE);
            List<CommunicationIceServer> iceServers = response.getValue().getIceServers();

            assertNotNull(response.getValue());
            assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
            assertNotNull(response.getValue().getExpiresOn());

            for (CommunicationIceServer iceS : iceServers) {
                assertNotNull(iceS.getUrls());
                assertNotNull(iceS.getUsername());
                assertNotNull(iceS.getCredential());
                assertEquals(RouteType.NEAREST, iceS.getRouteType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRelayConfigWithResponseWithTtl(HttpClient httpClient) {
        // Arrange
        try {
            setupTest(httpClient);
            CommunicationRelayClientBuilder builder = createClientBuilder(httpClient);
            client = setupClient(builder, "getRelayConfigWithResponse");
            Response<CommunicationRelayConfiguration> response;

            GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
            int ttl = 5000;
            options.setTtl(ttl);

            Instant now = Instant.now();
            OffsetDateTime requestedTime = now.atOffset(ZoneOffset.UTC).plusSeconds(ttl);

            // Action & Assert
            response = client.getRelayConfigurationWithResponse(options, Context.NONE);
            List<CommunicationIceServer> iceServers = response.getValue().getIceServers();

            assertNotNull(response.getValue());
            assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
            assertNotNull(response.getValue().getExpiresOn());

            if (getTestMode() != TestMode.PLAYBACK) {
                assertTrue(requestedTime.compareTo(response.getValue().getExpiresOn()) <= 0);
                OffsetDateTime limitedTime = requestedTime.plusSeconds(10);
                assertTrue(response.getValue().getExpiresOn().compareTo(limitedTime) < 0);
            }

            for (CommunicationIceServer iceS : iceServers) {
                assertNotNull(iceS.getUrls());
                assertNotNull(iceS.getUsername());
                assertNotNull(iceS.getCredential());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CommunicationRelayClient setupClient(CommunicationRelayClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
