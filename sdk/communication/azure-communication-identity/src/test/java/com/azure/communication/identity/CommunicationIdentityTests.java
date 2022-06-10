// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CommunicationIdentityTests extends CommunicationIdentityClientTestBase {
    private CommunicationIdentityClient client;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createIdentityClientUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "createIdentityClientUsingManagedIdentitySync");
        assertNotNull(client);

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        assertFalse(communicationUser.getId().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createIdentityClientUsingConnectionString(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
        client = setupClient(builder, "createIdentityClientUsingConnectionStringSync");
        assertNotNull(client);

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        assertFalse(communicationUser.getId().isEmpty());
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUser(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserSync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        assertFalse(communicationUser.getId().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserWithResponse(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserWithResponseSync");

        // Action & Assert
        Response<CommunicationUserIdentifier> response = client.createUserWithResponse(Context.NONE);
        assertNotNull(response.getValue().getId());
        assertFalse(response.getValue().getId().isEmpty());
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserAndToken(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserAndTokenSync");
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        CommunicationUserIdentifierAndToken result = client.createUserAndToken(scopes);
        assertNotNull(result.getUser().getId());
        assertNotNull(result.getUserToken());
        assertFalse(result.getUser().getId().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserAndTokenWithResponse(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserAndTokenWithResponseSync");
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        // Action & Assert
        Response<CommunicationUserIdentifierAndToken> response =
            client.createUserAndTokenWithResponse(scopes, Context.NONE);
        CommunicationUserIdentifierAndToken result = response.getValue();
        assertEquals(201, response.getStatusCode());
        assertNotNull(result.getUser().getId());
        assertNotNull(result.getUserToken());
        assertFalse(result.getUser().getId().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserAndTokenWithResponseNullContext(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserAndTokenWithResponseSync");
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        // Action & Assert
        Response<CommunicationUserIdentifierAndToken> response =
            client.createUserAndTokenWithResponse(scopes, null);
        CommunicationUserIdentifierAndToken result = response.getValue();
        assertEquals(201, response.getStatusCode());
        assertNotNull(result.getUser().getId());
        assertNotNull(result.getUserToken());
        assertFalse(result.getUser().getId().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUser(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "deleteUserSync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        client.deleteUser(communicationUser);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserWithResponse(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "deleteUserWithResponseSync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        Response<Void> response = client.deleteUserWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserWithResponseWithNullContext(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "deleteUserWithResponseSync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        Response<Void> response = client.deleteUserWithResponse(communicationUser, null);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeToken(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "revokeTokenSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        client.getToken(communicationUser, scopes);

        // Action & Assert
        client.revokeTokens(communicationUser);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenWithResponse(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "revokeTokenWithResponseSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        client.getToken(communicationUser, scopes);

        // Action & Assert
        Response<Void> response = client.revokeTokensWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenWithResponseNullContext(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "revokeTokenWithResponseSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        client.getToken(communicationUser, scopes);

        // Action & Assert
        Response<Void> response = client.revokeTokensWithResponse(communicationUser, null);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getToken(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        AccessToken issuedToken = client.getToken(communicationUser, scopes);
        assertNotNull(issuedToken.getToken());
        assertFalse(issuedToken.getToken().isEmpty());
        assertNotNull(issuedToken.getExpiresAt());
        assertFalse(issuedToken.getExpiresAt().toString().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenWithResponse(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenWithResponseSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        Response<AccessToken> issuedTokenResponse = client.getTokenWithResponse(communicationUser, scopes, Context.NONE);
        AccessToken issuedToken = issuedTokenResponse.getValue();
        assertEquals(200, issuedTokenResponse.getStatusCode(),  "Expect status code to be 200");
        assertNotNull(issuedToken.getToken());
        assertFalse(issuedToken.getToken().isEmpty());
        assertNotNull(issuedToken.getExpiresAt());
        assertFalse(issuedToken.getExpiresAt().toString().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenWithResponseNullContext(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenWithResponseSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        Response<AccessToken> issuedTokenResponse = client.getTokenWithResponse(communicationUser, scopes, null);
        AccessToken issuedToken = issuedTokenResponse.getValue();
        assertEquals(200, issuedTokenResponse.getStatusCode(),  "Expect status code to be 200");
        assertNotNull(issuedToken.getToken());
        assertFalse(issuedToken.getToken().isEmpty());
        assertNotNull(issuedToken.getExpiresAt());
        assertFalse(issuedToken.getExpiresAt().toString().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserWithResponseUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "createUserWithResponseUsingManagedIdentitySync");

        // Action & Assert
        Response<CommunicationUserIdentifier> response = client.createUserWithResponse(Context.NONE);
        assertNotNull(response.getValue().getId());
        assertFalse(response.getValue().getId().isEmpty());
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "deleteUserUsingManagedIdentitySync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        client.deleteUser(communicationUser);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserWithResponseUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "deleteUserWithResponseUsingManagedIdentitySync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        Response<Void> response = client.deleteUserWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenUsingManagedIdentity(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenWithResponseUsingManagedIdentity(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "getTokenUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        AccessToken issuedToken = client.getToken(communicationUser, scopes);
        assertNotNull(issuedToken.getToken());
        assertFalse(issuedToken.getToken().isEmpty());
        assertNotNull(issuedToken.getExpiresAt());
        assertFalse(issuedToken.getExpiresAt().toString().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenWithResponseUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "getTokenWithResponseUsingManagedIdentitySync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        Response<AccessToken> response = client.getTokenWithResponse(communicationUser, scopes, Context.NONE);
        AccessToken issuedToken = response.getValue();
        assertEquals(200, response.getStatusCode(),  "Expect status code to be 200");
        assertNotNull(issuedToken.getToken());
        assertFalse(issuedToken.getToken().isEmpty());
        assertNotNull(issuedToken.getExpiresAt());
        assertFalse(issuedToken.getExpiresAt().toString().isEmpty());
    }

    private CommunicationIdentityClient setupClient(CommunicationIdentityClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
