// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
import com.azure.communication.identity.models.GetTokenForTeamsUserOptions;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.time.Duration;

import static com.azure.communication.identity.CteTestHelper.skipExchangeAadTeamsTokenTest;
import static com.azure.communication.identity.TokenCustomExpirationTimeHelper.assertTokenExpirationWithinAllowedDeviation;
import static org.junit.jupiter.api.Assertions.*;

public class CommunicationIdentityTests extends CommunicationIdentityClientTestBase {
    private static final String TEST_SUFFIX = "Sync";
    private CommunicationIdentityClient client;

    @Test
    public void createIdentityClientUsingConnectionString() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
        client = setupClient(builder, "createIdentityClientUsingConnectionStringSync");
        assertNotNull(client);

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        assertFalse(communicationUser.getId().isEmpty());
    }


    @Test
    public void createUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserSync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        assertFalse(communicationUser.getId().isEmpty());
    }

    @Test
    public void createUserWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserWithResponseSync");

        // Action & Assert
        Response<CommunicationUserIdentifier> response = client.createUserWithResponse(Context.NONE);
        assertNotNull(response.getValue().getId());
        assertFalse(response.getValue().getId().isEmpty());
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
    }

    @Test
    public void createUserWithResponseNullContext() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserWithResponseSync");

        // Action & Assert
        Response<CommunicationUserIdentifier> response = client.createUserWithResponse(null);
        assertNotNull(response.getValue().getId());
        assertFalse(response.getValue().getId().isEmpty());
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
    }

    @Test
    public void createUserAndToken() {
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

    @Test
    public void createUserAndTokenWithoutScopes() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserAndTokenSync");

        // Action & Assert
        assertThrows(NullPointerException.class, () -> client.createUserAndToken(null));
    }

    @Test
    public void createUserAndTokenWithResponse() {
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void createUserAndTokenWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserAndTokenWithValidCustomExpiration " + testName + TEST_SUFFIX);
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        CommunicationUserIdentifierAndToken result = client.createUserAndToken(scopes, tokenExpiresIn);

        assertNotNull(result.getUser().getId());
        assertNotNull(result.getUserToken());
        assertFalse(result.getUser().getId().isEmpty());
        assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, result.getUserToken().getExpiresAt());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void createUserAndTokenWithResponseWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserAndTokenWithResponseWithValidCustomExpiration " + testName + TEST_SUFFIX);
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        Response<CommunicationUserIdentifierAndToken> response =
            client.createUserAndTokenWithResponse(scopes, tokenExpiresIn, Context.NONE);
        CommunicationUserIdentifierAndToken result = response.getValue();
        assertEquals(201, response.getStatusCode());
        assertNotNull(result.getUser().getId());
        assertNotNull(result.getUserToken());
        assertFalse(result.getUser().getId().isEmpty());
        assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, result.getUserToken().getExpiresAt());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void createUserAndTokenWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserAndTokenWithInvalidCustomExpiration " + testName + TEST_SUFFIX);
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        // Action & Assert
        try {
            client.createUserAndToken(scopes, tokenExpiresIn);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("400"));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void createUserAndTokenWithResponseWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserAndTokenWithResponseWithInvalidCustomExpiration " + testName + TEST_SUFFIX);
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        // Action & Assert
        try {
            client.createUserAndTokenWithResponse(scopes, tokenExpiresIn, Context.NONE);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("400"));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @Test
    public void createUserAndTokenWithOverFlownExpiration() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserAndTokenWithOverFlownExpiration " + TEST_SUFFIX);
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);
        // Action & Assert
        try {
            client.createUserAndToken(scopes, tokenExpiresIn);
        } catch (IllegalArgumentException exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().equals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @Test
    public void createUserAndTokenWithResponseWithOverFlownExpiration() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "createUserAndTokenWithResponseWithOverFlownExpiration " + TEST_SUFFIX);
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);
        // Action & Assert
        try {
            client.createUserAndTokenWithResponse(scopes, tokenExpiresIn, Context.NONE);
        } catch (IllegalArgumentException exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().equals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @Test
    public void createUserAndTokenWithResponseNullContext() {
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

    @Test
    public void deleteUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "deleteUserSync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());
        client.deleteUser(communicationUser);
    }

    @Test
    public void deleteUserWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "deleteUserWithResponseSync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        Response<Void> response = client.deleteUserWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @Test
    public void deleteUserWithResponseWithNullContext() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "deleteUserWithResponseSync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        Response<Void> response = client.deleteUserWithResponse(communicationUser, null);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @Test
    public void revokeToken() {
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

    @Test
    public void revokeTokenWithResponse() {
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

    @Test
    public void revokeTokenWithResponseNullContext() {
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

    @Test
    public void getToken() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        AccessToken issuedToken = client.getToken(communicationUser, scopes);
        verifyTokenNotEmpty(issuedToken);
    }

    @Test
    public void getTokenWithoutUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenSync");
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        // Action & Assert
        assertThrows(NullPointerException.class, () -> client.getToken(null, scopes));
    }

    @Test
    public void getTokenWithoutScopes() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenSync");
        CommunicationUserIdentifier communicationUser = client.createUser();

        // Action & Assert
        assertThrows(NullPointerException.class, () -> client.getToken(communicationUser, null));
    }

    @Test
    public void getTokenWithResponse() {
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void getTokenWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenWithValidCustomExpiration " + testName + TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        AccessToken issuedToken = client.getToken(communicationUser, scopes, tokenExpiresIn);
        verifyTokenNotEmpty(issuedToken);
        assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, issuedToken.getExpiresAt());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void getTokenWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenWithInvalidCustomExpiration " + testName + TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        try {
            client.getToken(communicationUser, scopes, tokenExpiresIn);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("400"));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void getTokenWithResponseWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenWithResponseWithValidCustomExpiration " + testName + TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        Response<AccessToken> issuedTokenResponse = client.getTokenWithResponse(communicationUser, scopes, tokenExpiresIn, Context.NONE);
        assertEquals(200, issuedTokenResponse.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(issuedTokenResponse.getValue());
        assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, issuedTokenResponse.getValue().getExpiresAt());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void getTokenWithResponseWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenWithResponseWithInvalidCustomExpiration " + testName + TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        try {
            client.getTokenWithResponse(communicationUser, scopes, tokenExpiresIn, Context.NONE);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("400"));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @Test
    public void getTokenWithOverflownExpiration() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenWithOverflownExpiration " + TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);
        // Action & Assert
        try {
            client.getToken(communicationUser, scopes, tokenExpiresIn);
        } catch (IllegalArgumentException exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().equals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @Test
    public void getTokenWithResponseWithOverflownExpiration() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenWithResponseWithOverflownExpiration " + TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);
        // Action & Assert
        try {
            client.getTokenWithResponse(communicationUser, scopes, tokenExpiresIn, Context.NONE);
        } catch (IllegalArgumentException exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().equals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @Test
    public void getTokenWithResponseNullContext() {
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
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithValidParams(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithValidParamsSync");
        // Action & Assert
        AccessToken issuedToken = client.getTokenForTeamsUser(options);
        verifyTokenNotEmpty(issuedToken);
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithValidParamsWithResponse(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithValidParamsWithResponseSync");
        // Action & Assert
        Response<AccessToken> response = client.getTokenForTeamsUserWithResponse(options, Context.NONE);
        assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(response.getValue());
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithValidParamsWithResponseNullContext(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithValidParamsWithResponseSync");
        // Action & Assert
        Response<AccessToken> response = client.getTokenForTeamsUserWithResponse(options, null);
        assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(response.getValue());
    }


    @ParameterizedTest(name = "when {3} is null")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getNullParams")
    public void getTokenForTeamsUserWithNullParams(GetTokenForTeamsUserOptions options, String exceptionMessage) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, "getTokenForTeamsUserWithNullSync: when " + exceptionMessage + " is null");
        // Action & Assert
        try {
            AccessToken issuedToken = client.getTokenForTeamsUser(options);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains(exceptionMessage));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getInvalidTokens")
    public void getTokenForTeamsUserWithInvalidToken(String testName, GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, testName + TEST_SUFFIX);
        // Action & Assert
        try {
            AccessToken issuedToken = client.getTokenForTeamsUser(options);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("401"));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getInvalidAppIds")
    public void getTokenForTeamsUserWithInvalidAppId(String testName, GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, testName + TEST_SUFFIX);
        // Action & Assert
        try {
            AccessToken issuedToken = client.getTokenForTeamsUser(options);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("400"));
            return;
        }
        fail("An exception should have been thrown.");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getInvalidUserIds")
    public void getTokenForTeamsUserWithInvalidUserId(String testName, GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        client = setupClient(builder, testName + TEST_SUFFIX);
        // Action & Assert
        try {
            AccessToken issuedToken = client.getTokenForTeamsUser(options);
        } catch (Exception exception) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("400"));
            return;
        }
        fail("An exception should have been thrown.");
    }

}
