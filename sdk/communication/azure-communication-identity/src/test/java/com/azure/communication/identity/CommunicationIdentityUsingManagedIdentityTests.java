// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static com.azure.communication.identity.CteTestHelper.skipExchangeAadTeamsTokenTest;

public class CommunicationIdentityUsingManagedIdentityTests extends CommunicationIdentityClientTestBase {
    private CommunicationIdentityClient client;

    @Test
    public void createIdentityClient() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "createIdentityClientUsingManagedIdentitySync");
        assertNotNull(client);

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        assertFalse(communicationUser.getId().isEmpty());
    }

    @Test
    public void createUserWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "createUserWithResponseUsingManagedIdentitySync");

        // Action & Assert
        Response<CommunicationUserIdentifier> response = client.createUserWithResponse(Context.NONE);
        assertNotNull(response.getValue().getId());
        assertFalse(response.getValue().getId().isEmpty());
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
    }

    @Test
    public void deleteUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "deleteUserUsingManagedIdentitySync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        client.deleteUser(communicationUser);
    }

    @Test
    public void deleteUserWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "deleteUserWithResponseUsingManagedIdentitySync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        Response<Void> response = client.deleteUserWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @Test
    public void revokeToken() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "revokeTokenUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        client.getToken(communicationUser, scopes);

        // Action & Assert
        client.revokeTokens(communicationUser);
    }

    @Test
    public void revokeTokenWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "revokeTokenWithResponseUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        client.getToken(communicationUser, scopes);

        // Action & Assert
        Response<Void> response = client.revokeTokensWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @Test
    public void getToken() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "getTokenUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        AccessToken issuedToken = client.getToken(communicationUser, scopes);
        verifyTokenNotEmpty(issuedToken);
    }

    @Test
    public void getTokenWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "getTokenWithResponseUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        Response<AccessToken> response = client.getTokenWithResponse(communicationUser, scopes, Context.NONE);
        assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(response.getValue());
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUser(String teamsUserAadToken, String appId, String userId) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }

        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserUsingManagedIdentitySync");
        // Action & Assert
        AccessToken issuedToken = client.getTokenForTeamsUser(teamsUserAadToken, appId, userId);
        verifyTokenNotEmpty(issuedToken);
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithResponse(String teamsUserAadToken, String appId, String userId) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithResponseUsingManagedIdentitySync");
        // Action & Assert
        Response<AccessToken> response = client.getTokenForTeamsUserWithResponse(teamsUserAadToken, appId, userId, Context.NONE);
        assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(response.getValue());
    }

}
