// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.networktraversal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.communication.common.CommunicationUserIdentifier;
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
            
            // Action & Assert
            assertNotNull(client);
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
        try {
            setupTest(httpClient);
            CommunicationRelayClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
            client = setupClient(builder, "createIdentityClientUsingConnectionStringSync");
            assertNotNull(client);
            CommunicationRelayConfiguration config = client.getRelayConfiguration(user);
            
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
        try {
            setupTest(httpClient);
            CommunicationRelayClientBuilder builder = createClientBuilder(httpClient);
            client = setupClient(builder, "getRelayConfigWithResponse");
            Response<CommunicationRelayConfiguration> response;
        
            // Action & Assert
            response = client.getRelayConfigurationWithResponse(user, Context.NONE);
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
}
