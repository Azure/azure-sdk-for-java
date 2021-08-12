// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.networktraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.communication.identity.CommunicationIdentityAsyncClient;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.CommunicationIceServer;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class CommunicationRelayAsyncTests extends CommunicationRelayClientTestBase {
    private CommunicationRelayAsyncClient asyncClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientUsingManagedIdentity(HttpClient httpClient) {

        // Arrange
        CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createRelayClientUsingManagedIdentitySync");
        assertNotNull(asyncClient);
        
        CommunicationIdentityClientBuilder identityBuilder = createIdentityClientBuilder(httpClient);
        CommunicationIdentityAsyncClient communicationIdentityClient = setupIdentityAsyncClient(identityBuilder, "createRelayClientUsingManagedIdentity");

        // Action & Assert
        Mono<CommunicationUserIdentifier> response = communicationIdentityClient.createUser();
        CommunicationUserIdentifier user = response.block();
        
        StepVerifier.create(response)
        .assertNext(item -> {
            assertNotNull(item.getId());
        }).verifyComplete();
        
        if (user != null) {
            Mono<CommunicationRelayConfiguration> relayResponse = asyncClient.getRelayConfiguration(user);

            StepVerifier.create(relayResponse)
            .assertNext(relayConfig -> {
                assertNotNull(relayConfig.getIceServers());
                for (CommunicationIceServer iceS : relayConfig.getIceServers()) {
                    System.out.println("Urls:" + iceS.getUrls());
                    assertNotNull(iceS.getUsername());
                    System.out.println("Username: " + iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                    System.out.println("Credential: " + iceS.getCredential());
                }
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientUsingConnectionString(HttpClient httpClient) {
        
        // Arrange
        CommunicationRelayClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "createIdentityClientUsingConnectionStringSync");
        assertNotNull(asyncClient);
        
        String connectionString = System.getenv("COMMUNICATION_LIVETEST_DYNAMIC_CONNECTION_STRING");
        CommunicationIdentityAsyncClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        
        Mono<CommunicationUserIdentifier> response = communicationIdentityClient.createUser();
        CommunicationUserIdentifier user = response.block();
        
        
        // Action & Assert
        StepVerifier.create(response)
        .assertNext(item -> {
            assertNotNull(item.getId());
        }).verifyComplete();
        
        if (user != null) {
            Mono<CommunicationRelayConfiguration> relayResponse = asyncClient.getRelayConfiguration(user);

            StepVerifier.create(relayResponse)
            .assertNext(relayConfig -> {
                assertNotNull(relayConfig.getIceServers());
                for (CommunicationIceServer iceS : relayConfig.getIceServers()) {
                    assertNotNull(iceS.getUrls());
                    System.out.println("Urls:" + iceS.getUrls());
                    assertNotNull(iceS.getUsername());
                    System.out.println("Username: " + iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                    System.out.println("Credential: " + iceS.getCredential());
                }
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRelayConfigWithResponse(HttpClient httpClient) {
        
        // Arrange
        CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createRelayClientUsingManagedIdentitySync");
        assertNotNull(asyncClient);
        
        CommunicationIdentityClientBuilder identityBuilder = createIdentityClientBuilder(httpClient);
        CommunicationIdentityAsyncClient communicationIdentityClient = setupIdentityAsyncClient(identityBuilder, "createRelayClientUsingManagedIdentity");
        
        // Action & Assert
        Mono<CommunicationUserIdentifier> responseUser = communicationIdentityClient.createUser();
        CommunicationUserIdentifier user = responseUser.block();
        
        StepVerifier.create(responseUser)
        .assertNext(item -> {
            assertNotNull(item.getId());
        }).verifyComplete();
        
        if (user != null) {
            Mono<Response<CommunicationRelayConfiguration>> relayConfig = asyncClient.getRelayConfigurationWithResponse(user);

            StepVerifier.create(relayConfig)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
                assertNotNull(response.getValue().getIceServers());
                for (CommunicationIceServer iceS : response.getValue().getIceServers()) {
                    assertNotNull(iceS.getUrls());
                    System.out.println("Urls: " + iceS.getUrls());
                    assertNotNull(iceS.getUsername());
                    System.out.println("Username: " + iceS.getUsername());
                    assertNotNull(iceS.getCredential());
                    System.out.println("Credential: " + iceS.getCredential());
                }
            }).verifyComplete();
        }
    }

    private CommunicationRelayAsyncClient setupAsyncClient(CommunicationRelayClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    private CommunicationIdentityAsyncClient setupIdentityAsyncClient(CommunicationIdentityClientBuilder builder, String testName) {
        return addLoggingPolicyIdentity(builder, testName).buildAsyncClient();
    }
}
