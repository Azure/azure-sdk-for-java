// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.networktraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.CommunicationIceServer;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.List;

public class CommunicationRelayTests extends CommunicationRelayClientTestBase {
    private CommunicationRelayClient client;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRelayClientUsingManagedIdentity(HttpClient httpClient) {

        // Arrange
        try {
            CommunicationRelayClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
            client = setupClient(builder, "createRelayClientUsingManagedIdentitySync");
            assertNotNull(client);
            
            CommunicationIdentityClientBuilder identityBuilder = createClientIdentityBuilderUsingManagedIdentity(httpClient);
            CommunicationIdentityClient communicationIdentityClient = setupIdentityClient(identityBuilder, "createIdentityClientUsingManagedIdentity");
            assertNotNull(communicationIdentityClient);
            
            CommunicationUserIdentifier user = communicationIdentityClient.createUser();
            
            // Action & Assert
            CommunicationRelayConfiguration config = client.getRelayConfiguration(user);
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
    public void createRelayClientUsingConnectionString(HttpClient httpClient) {

        // Arrange
        CommunicationRelayClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
        client = setupClient(builder, "createIdentityClientUsingConnectionStringSync");
        assertNotNull(client);
        
        CommunicationIdentityClientBuilder identityBuilder = createIdentityClientBuilderUsingConnectionString(httpClient);
        CommunicationIdentityClient communicationIdentityClient = setupIdentityClient(identityBuilder, "createIdentityClientUsingConnectionString");
        assertNotNull(communicationIdentityClient);

        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        CommunicationRelayConfiguration config;
        
        
        // Action & Assert
        try {
            config = client.getRelayConfiguration(user);
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
    public void getRelayConfigWithResponse(HttpClient httpClient) {
        // Arrange
        CommunicationRelayClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getRelayConfigWithResponse");

        CommunicationIdentityClientBuilder identityBuilder = createIdentityClientBuilder(httpClient);
        CommunicationIdentityClient communicationIdentityClient = setupIdentityClient(identityBuilder, "getRelayConfigWithResponse");

        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        Response<CommunicationRelayConfiguration> response;
        
        
        // Action & Assert
        try {
            response = client.getRelayConfigurationWithResponse(user, Context.NONE);
            // Action & Assert
            List<CommunicationIceServer> iceServers = response.getValue().getIceServers();

            assertNotNull(response.getValue());
            assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
            assertNotNull(response.getValue().getExpiresOn());

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

    private CommunicationIdentityClient setupIdentityClient(CommunicationIdentityClientBuilder builder, String testName) {
        return addLoggingPolicyIdentity(builder, testName).buildClient();
    }
}
