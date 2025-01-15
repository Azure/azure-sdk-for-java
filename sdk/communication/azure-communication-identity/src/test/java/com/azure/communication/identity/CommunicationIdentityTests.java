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

import java.time.Duration;
import java.util.List;

import static com.azure.communication.identity.CteTestHelper.skipExchangeAadTeamsTokenTest;
import static com.azure.communication.identity.TokenCustomExpirationTimeHelper.assertTokenExpirationWithinAllowedDeviation;
import static org.junit.jupiter.api.Assertions.*;

public class CommunicationIdentityTests extends CommunicationIdentityClientTestBase {

    private CommunicationIdentityClient client;
    private CommunicationIdentityClientBuilder builder;

    @Override
    public void beforeTest() {
        super.beforeTest();
        builder = createClientBuilder(buildSyncAssertingClient(httpClient));
    }

    @Test
    public void createIdentityClientUsingConnectionString() {
        // Arrange
        CommunicationIdentityClientBuilder builder
            = createClientBuilderUsingConnectionString(buildSyncAssertingClient(httpClient));
        client = setupClient(builder, "createIdentityClientUsingConnectionStringSync");
        assertNotNull(client);

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);
    }

    @Test
    public void createUser() {
        // Arrange
        client = setupClient(builder, "createUserSync");

        // Action & Assert
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);
    }

    @Test
    public void createUserWithResponse() {
        // Arrange
        client = setupClient(builder, "createUserWithResponseSync");

        // Action & Assert
        Response<CommunicationUserIdentifier> response = client.createUserWithResponse(Context.NONE);
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
        verifyUserNotEmpty(response.getValue());
    }

    @Test
    public void createUserWithResponseNullContext() {
        // Arrange
        client = setupClient(builder, "createUserWithResponseSync");

        // Action & Assert
        Response<CommunicationUserIdentifier> response = client.createUserWithResponse(null);
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
        verifyUserNotEmpty(response.getValue());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenScopeTestHelper#getTokenScopes")
    public void createUserAndToken(String testName, List<CommunicationTokenScope> scopes) {
        // Arrange
        client = setupClient(builder, "createUserAndTokenWith" + testName + SYNC_TEST_SUFFIX);

        // Action & Assert
        CommunicationUserIdentifierAndToken result = client.createUserAndToken(scopes);
        verifyUserNotEmpty(result.getUser());
        verifyTokenNotEmpty(result.getUserToken());
    }

    @Test
    public void createUserAndTokenWithoutScopes() {
        // Arrange
        client = setupClient(builder, "createUserAndTokenSync");

        // Action & Assert
        assertThrows(NullPointerException.class, () -> client.createUserAndToken(null));
    }

    @Test
    public void createUserAndTokenWithResponse() {
        // Arrange
        client = setupClient(builder, "createUserAndTokenWithResponseSync");

        // Action & Assert
        Response<CommunicationUserIdentifierAndToken> response
            = client.createUserAndTokenWithResponse(SCOPES, Context.NONE);
        CommunicationUserIdentifierAndToken result = response.getValue();
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
        verifyUserNotEmpty(result.getUser());
        verifyTokenNotEmpty(result.getUserToken());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void createUserAndTokenWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        client = setupClient(builder, "createUserAndTokenWithValidCustomExpiration " + testName + SYNC_TEST_SUFFIX);

        // Action & Assert
        CommunicationUserIdentifierAndToken result = client.createUserAndToken(SCOPES, tokenExpiresIn);
        verifyUserNotEmpty(result.getUser());
        verifyTokenNotEmpty(result.getUserToken());
        assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, result.getUserToken().getExpiresAt());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void createUserAndTokenWithResponseWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        client = setupClient(builder,
            "createUserAndTokenWithResponseWithValidCustomExpiration " + testName + SYNC_TEST_SUFFIX);

        // Action & Assert
        Response<CommunicationUserIdentifierAndToken> response
            = client.createUserAndTokenWithResponse(SCOPES, tokenExpiresIn, Context.NONE);
        CommunicationUserIdentifierAndToken result = response.getValue();
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
        verifyUserNotEmpty(result.getUser());
        verifyTokenNotEmpty(result.getUserToken());
        assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, result.getUserToken().getExpiresAt());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void createUserAndTokenWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        client = setupClient(builder, "createUserAndTokenWithInvalidCustomExpiration " + testName + SYNC_TEST_SUFFIX);

        // Action & Assert
        try {
            client.createUserAndToken(SCOPES, tokenExpiresIn);
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
        client = setupClient(builder,
            "createUserAndTokenWithResponseWithInvalidCustomExpiration " + testName + SYNC_TEST_SUFFIX);

        // Action & Assert
        try {
            client.createUserAndTokenWithResponse(SCOPES, tokenExpiresIn, Context.NONE);
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
        client = setupClient(builder, "createUserAndTokenWithOverFlownExpiration " + SYNC_TEST_SUFFIX);
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);

        // Action & Assert
        try {
            client.createUserAndToken(SCOPES, tokenExpiresIn);
        } catch (IllegalArgumentException exception) {
            assertNotNull(exception.getMessage());
            assertEquals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE, exception.getMessage());
            return;
        }
        fail("An exception should have been thrown.");
    }

    @Test
    public void createUserAndTokenWithResponseWithOverFlownExpiration() {
        // Arrange
        client = setupClient(builder, "createUserAndTokenWithResponseWithOverFlownExpiration " + SYNC_TEST_SUFFIX);
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);

        // Action & Assert
        try {
            client.createUserAndTokenWithResponse(SCOPES, tokenExpiresIn, Context.NONE);
        } catch (IllegalArgumentException exception) {
            assertNotNull(exception.getMessage());
            assertEquals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE, exception.getMessage());
            return;
        }
        fail("An exception should have been thrown.");
    }

    @Test
    public void createUserAndTokenWithResponseNullContext() {
        // Arrange
        client = setupClient(builder, "createUserAndTokenWithResponseSync");

        // Action & Assert
        Response<CommunicationUserIdentifierAndToken> response = client.createUserAndTokenWithResponse(SCOPES, null);
        CommunicationUserIdentifierAndToken result = response.getValue();
        assertEquals(201, response.getStatusCode(), "Expect status code to be 201");
        verifyUserNotEmpty(result.getUser());
        verifyTokenNotEmpty(result.getUserToken());
    }

    @Test
    public void deleteUser() {
        // Arrange
        client = setupClient(builder, "deleteUserSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        assertNotNull(communicationUser.getId());

        // Action & Assert
        client.deleteUser(communicationUser);
    }

    @Test
    public void deleteUserWithResponse() {
        // Arrange
        client = setupClient(builder, "deleteUserWithResponseSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        Response<Void> response = client.deleteUserWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @Test
    public void deleteUserWithResponseWithNullContext() {
        // Arrange
        client = setupClient(builder, "deleteUserWithResponseSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        Response<Void> response = client.deleteUserWithResponse(communicationUser, null);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @Test
    public void revokeToken() {
        // Arrange
        client = setupClient(builder, "revokeTokenSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);
        AccessToken issuedToken = client.getToken(communicationUser, SCOPES);
        verifyTokenNotEmpty(issuedToken);

        // Action & Assert
        client.revokeTokens(communicationUser);
    }

    @Test
    public void revokeTokenWithResponse() {
        // Arrange
        client = setupClient(builder, "revokeTokenWithResponseSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        AccessToken issuedToken = client.getToken(communicationUser, SCOPES);
        verifyTokenNotEmpty(issuedToken);

        // Action & Assert
        Response<Void> response = client.revokeTokensWithResponse(communicationUser, Context.NONE);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @Test
    public void revokeTokenWithResponseNullContext() {
        // Arrange
        client = setupClient(builder, "revokeTokenWithResponseSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);
        AccessToken issuedToken = client.getToken(communicationUser, SCOPES);
        verifyTokenNotEmpty(issuedToken);

        // Action & Assert
        Response<Void> response = client.revokeTokensWithResponse(communicationUser, null);
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenScopeTestHelper#getTokenScopes")
    public void getToken(String testName, List<CommunicationTokenScope> scopes) {
        // Arrange
        client = setupClient(builder, "getTokenWith" + testName + SYNC_TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        AccessToken issuedToken = client.getToken(communicationUser, scopes);
        verifyTokenNotEmpty(issuedToken);
    }

    @Test
    public void getTokenWithoutUser() {
        // Arrange
        client = setupClient(builder, "getTokenSync");

        // Action & Assert
        assertThrows(NullPointerException.class, () -> client.getToken(null, SCOPES));
    }

    @Test
    public void getTokenWithoutScopes() {
        // Arrange
        client = setupClient(builder, "getTokenSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        assertThrows(NullPointerException.class, () -> client.getToken(communicationUser, null));
    }

    @Test
    public void getTokenWithResponse() {
        // Arrange
        client = setupClient(builder, "getTokenWithResponseSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        Response<AccessToken> issuedTokenResponse
            = client.getTokenWithResponse(communicationUser, SCOPES, Context.NONE);
        assertEquals(200, issuedTokenResponse.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(issuedTokenResponse.getValue());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void getTokenWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        client = setupClient(builder, "getTokenWithValidCustomExpiration " + testName + SYNC_TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        AccessToken issuedToken = client.getToken(communicationUser, SCOPES, tokenExpiresIn);
        verifyTokenNotEmpty(issuedToken);
        assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, issuedToken.getExpiresAt());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void getTokenWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        client = setupClient(builder, "getTokenWithInvalidCustomExpiration " + testName + SYNC_TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        try {
            client.getToken(communicationUser, SCOPES, tokenExpiresIn);
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
        client = setupClient(builder, "getTokenWithResponseWithValidCustomExpiration " + testName + SYNC_TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        Response<AccessToken> issuedTokenResponse
            = client.getTokenWithResponse(communicationUser, SCOPES, tokenExpiresIn, Context.NONE);
        assertEquals(200, issuedTokenResponse.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(issuedTokenResponse.getValue());
        assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, issuedTokenResponse.getValue().getExpiresAt());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void getTokenWithResponseWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        client = setupClient(builder, "getTokenWithResponseWithInvalidCustomExpiration " + testName + SYNC_TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        try {
            client.getTokenWithResponse(communicationUser, SCOPES, tokenExpiresIn, Context.NONE);
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
        client = setupClient(builder, "getTokenWithOverflownExpiration " + SYNC_TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);

        // Action & Assert
        try {
            client.getToken(communicationUser, SCOPES, tokenExpiresIn);
        } catch (IllegalArgumentException exception) {
            assertNotNull(exception.getMessage());
            assertEquals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE, exception.getMessage());
            return;
        }
        fail("An exception should have been thrown.");
    }

    @Test
    public void getTokenWithResponseWithOverflownExpiration() {
        // Arrange
        client = setupClient(builder, "getTokenWithResponseWithOverflownExpiration " + SYNC_TEST_SUFFIX);
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);

        // Action & Assert
        try {
            client.getTokenWithResponse(communicationUser, SCOPES, tokenExpiresIn, Context.NONE);
        } catch (IllegalArgumentException exception) {
            assertNotNull(exception.getMessage());
            assertEquals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE, exception.getMessage());
            return;
        }
        fail("An exception should have been thrown.");
    }

    @Test
    public void getTokenWithResponseNullContext() {
        // Arrange
        client = setupClient(builder, "getTokenWithResponseSync");
        CommunicationUserIdentifier communicationUser = client.createUser();
        verifyUserNotEmpty(communicationUser);

        // Action & Assert
        Response<AccessToken> issuedTokenResponse = client.getTokenWithResponse(communicationUser, SCOPES, null);
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
        client = setupClient(builder, "getTokenForTeamsUserWithValidParamsWithResponseSync");

        // Action & Assert
        Response<AccessToken> response = client.getTokenForTeamsUserWithResponse(options, null);
        assertEquals(200, response.getStatusCode(), "Expect status code to be 200");
        verifyTokenNotEmpty(response.getValue());
    }

    @ParameterizedTest(name = "when {1} is null")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getNullParams")
    public void getTokenForTeamsUserWithNullParams(GetTokenForTeamsUserOptions options, String exceptionMessage) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
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
        client = setupClient(builder, testName + SYNC_TEST_SUFFIX);

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
        client = setupClient(builder, testName + SYNC_TEST_SUFFIX);

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
        client = setupClient(builder, testName + SYNC_TEST_SUFFIX);

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
