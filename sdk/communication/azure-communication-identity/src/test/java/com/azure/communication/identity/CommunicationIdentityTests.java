// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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
        verifyTokenNotEmpty(issuedToken);
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
        assertEquals(200, issuedTokenResponse.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(issuedTokenResponse.getValue());
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
        assertEquals(200, issuedTokenResponse.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(issuedTokenResponse.getValue());
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
        verifyTokenNotEmpty(issuedToken);
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
        assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(response.getValue());
    }

    private CommunicationIdentityClient setupClient(CommunicationIdentityClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenForTeamsUserWithEmptyToken(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithEmptyTokenSync");
        // Action & Assert
        try {
            AccessToken issuedToken = client.getTokenForTeamsUser("", COMMUNICATION_CLIENT_ID, COMMUNICATION_OBJECT_ID);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("401"));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @ParameterizedTest(name = "when {4} is null")
    @MethodSource("getNullParamsForGetTokenForTeamsUser")
    public void getTokenForTeamsUserWithNullParams(HttpClient httpClient, String teamsUserAadToken, String appId, String userId, String exceptionMessage) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithNullSync");
        // Action & Assert
        try {
            AccessToken issuedToken = client.getTokenForTeamsUser(teamsUserAadToken, appId, userId);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains(exceptionMessage));
            return;
        }
        fail("An exception should have been thrown.");
    }

    /**
     * Generates various option types for testing getTokenForTeamsUserWithNullParams() method.
     *
     * @return A stream of options to parameterized test.
     */
    private static Stream<Arguments> getNullParamsForGetTokenForTeamsUser() {
        List<Arguments> argumentsList = new ArrayList<>();
        List<String[]> params = new ArrayList<String[]>() {{
            add(new String[]{null, null, null, "token"});
            add(new String[]{null, COMMUNICATION_CLIENT_ID, COMMUNICATION_OBJECT_ID, "token"});
        }};
        try {
            String teamsUserAadToken = generateTeamsUserAadToken();
            params.add(new String[]{teamsUserAadToken, null, COMMUNICATION_OBJECT_ID, "appId"});
            params.add(new String[]{teamsUserAadToken, COMMUNICATION_CLIENT_ID, null, "userId"});
        } catch (Exception e) {
        }
        getHttpClients()
                .forEach(httpClient -> params.stream()
                        .forEach(param -> argumentsList.add(Arguments.of(httpClient, param[0], param[1], param[2], param[3]))));
        return argumentsList.stream();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenForTeamsUserWithInvalidToken(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithInvalidTokenSync");
        // Action & Assert
        try {
            AccessToken issuedToken = client.getTokenForTeamsUser("invalid", COMMUNICATION_CLIENT_ID, COMMUNICATION_OBJECT_ID);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("401"));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenForTeamsUserWithExpiredToken(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithExpiredTokenSync");
        // Action & Assert
        try {
            AccessToken issuedToken = client.getTokenForTeamsUser(COMMUNICATION_EXPIRED_TEAMS_TOKEN, COMMUNICATION_CLIENT_ID, COMMUNICATION_OBJECT_ID);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("401"));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenForTeamsUserWithValidToken(HttpClient httpClient) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }

        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithValidTokenSync");
        // Action & Assert
        try {
            String teamsUserAadToken = generateTeamsUserAadToken();
            AccessToken issuedToken = client.getTokenForTeamsUser(teamsUserAadToken, COMMUNICATION_CLIENT_ID, COMMUNICATION_OBJECT_ID);
            verifyTokenNotEmpty(issuedToken);
        } catch (Exception exception) {
            fail("Could not generate teams token");
        }

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenForTeamsUserWithValidTokenWithResponse(HttpClient httpClient) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }

        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithValidTokenWithResponseSync");
        // Action & Assert
        try {
            String teamsUserAadToken = generateTeamsUserAadToken();
            Response<AccessToken> response = client.getTokenForTeamsUserWithResponse(teamsUserAadToken, COMMUNICATION_CLIENT_ID, COMMUNICATION_OBJECT_ID, Context.NONE);
            assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
            verifyTokenNotEmpty(response.getValue());
        } catch (Exception exception) {
            fail("Could not generate teams token");
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenForTeamsUserUsingManagedIdentity(HttpClient httpClient) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }

        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserUsingManagedIdentitySync");
        // Action & Assert
        try {
            String teamsUserAadToken = generateTeamsUserAadToken();
            AccessToken issuedToken = client.getTokenForTeamsUser(teamsUserAadToken, COMMUNICATION_CLIENT_ID, COMMUNICATION_OBJECT_ID);
            verifyTokenNotEmpty(issuedToken);
        } catch (Exception e) {
            fail("Could not generate teams token");
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenForTeamsUserWithResponseUsingManagedIdentity(HttpClient httpClient) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }

        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithResponseUsingManagedIdentitySync");
        // Action & Assert
        try {
            String teamsUserAadToken = generateTeamsUserAadToken();
            Response<AccessToken> response = client.getTokenForTeamsUserWithResponse(teamsUserAadToken, COMMUNICATION_CLIENT_ID, COMMUNICATION_OBJECT_ID, Context.NONE);
            assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
            verifyTokenNotEmpty(response.getValue());
        } catch (Exception e) {
            fail("Could not generate teams token");
        }
    }

}
