// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.communication.common.CommunicationUser;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CommunicationIdentityTests extends CommunicationIdentityClientTestBase {
    private CommunicationIdentityClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createIdentityClientUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        client = getCommunicationIdentityClientBuilderUsingManagedIdentity(httpClient).buildClient();
        assertNotNull(client);

        // Action & Assert
        CommunicationUser communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        assertFalse(communicationUser.getId().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createIdentityClientUsingConnectionString(HttpClient httpClient) {
        // Arrange
        client = getCommunicationIdentityClientUsingConnectionString(httpClient).buildClient();
        assertNotNull(client);

        // Action & Assert
        CommunicationUser communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        assertFalse(communicationUser.getId().isEmpty());
    }

    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUser(HttpClient httpClient) {
        // Arrange
        client = getCommunicationIdentityClient(httpClient).buildClient();

        // Action & Assert
        CommunicationUser communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        assertFalse(communicationUser.getId().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserWithResponse(HttpClient httpClient) {
        // Arrange
        client = getCommunicationIdentityClient(httpClient).buildClient();

        // Action & Assert
        Response<CommunicationUser> response = client.createUserWithResponse(Context.NONE);
        assertNotNull(response.getValue().getId());
        assertFalse(response.getValue().getId().isEmpty());
        assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUser(HttpClient httpClient) {
        // Arrange
        client = getCommunicationIdentityClient(httpClient).buildClient();

        // Action & Assert
        CommunicationUser communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        client.deleteUser(communicationUser);    
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserWithResponse(HttpClient httpClient) {
        // Arrange
        client = getCommunicationIdentityClient(httpClient).buildClient();

        // Action & Assert
        CommunicationUser communicationUser = client.createUser();
        Response<Void> response = client.deleteUserWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeToken(HttpClient httpClient) {
        // Arrange
        client = getCommunicationIdentityClient(httpClient).buildClient();

        // Action & Assert
        CommunicationUser communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken token = client.issueToken(communicationUser, scopes);
        client.revokeTokens(token.getUser(), null);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenWithResponse(HttpClient httpClient) {
        // Arrange
        client = getCommunicationIdentityClient(httpClient).buildClient();

        // Action & Assert
        CommunicationUser communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken token = client.issueToken(communicationUser, scopes);
        Response<Void> response = client.revokeTokensWithResponse(token.getUser(), null, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");    
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void issueToken(HttpClient httpClient) {
        // Arrange
        client = getCommunicationIdentityClient(httpClient).buildClient();

        // Action & Assert
        CommunicationUser communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken issuedToken = client.issueToken(communicationUser, scopes);
        assertNotNull(issuedToken.getToken());
        assertFalse(issuedToken.getToken().isEmpty());
        assertNotNull(issuedToken.getExpiresOn());
        assertFalse(issuedToken.getExpiresOn().toString().isEmpty());
        assertNotNull(issuedToken.getUser());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void issueTokenWithResponse(HttpClient httpClient) {
        // Arrange
        client = getCommunicationIdentityClient(httpClient).buildClient();

        // Action & Assert
        CommunicationUser communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        Response<CommunicationUserToken> issuedTokenResponse = client.issueTokenWithResponse(communicationUser, scopes, Context.NONE);
        CommunicationUserToken issuedToken = issuedTokenResponse.getValue();
        assertEquals(200, issuedTokenResponse.getStatusCode(),  "Expect status code to be 200");
        assertNotNull(issuedToken.getToken());
        assertFalse(issuedToken.getToken().isEmpty());
        assertNotNull(issuedToken.getExpiresOn());
        assertFalse(issuedToken.getExpiresOn().toString().isEmpty());
        assertNotNull(issuedToken.getUser());
    }
}
