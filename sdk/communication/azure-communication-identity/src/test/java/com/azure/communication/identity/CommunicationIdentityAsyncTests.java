// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
import com.azure.communication.identity.models.GetTokenForTeamsUserOptions;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static com.azure.communication.identity.CteTestHelper.skipExchangeAadTeamsTokenTest;
import static com.azure.communication.identity.TokenCustomExpirationTimeHelper.assertTokenExpirationWithinAllowedDeviation;
import static org.junit.jupiter.api.Assertions.*;

public class CommunicationIdentityAsyncTests extends CommunicationIdentityClientTestBase {

    private CommunicationIdentityAsyncClient asyncClient;
    private CommunicationIdentityClientBuilder builder;

    @Override
    public void beforeTest() {
        super.beforeTest();
        builder = createClientBuilder(buildAsyncAssertingClient(httpClient));
    }

    @Test
    public void createAsyncIdentityClientUsingConnectionString() {
        // Arrange
        CommunicationIdentityClientBuilder builder
            = createClientBuilderUsingConnectionString(buildAsyncAssertingClient(httpClient));
        asyncClient = setupAsyncClient(builder, "createAsyncIdentityClientUsingConnectionString");
        assertNotNull(asyncClient);

        // Action & Assert
        Mono<CommunicationUserIdentifier> response = asyncClient.createUser();
        StepVerifier.create(response).assertNext(this::verifyUserNotEmpty).verifyComplete();
    }

    @Test
    public void createUser() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUser");

        // Action & Assert
        Mono<CommunicationUserIdentifier> response = asyncClient.createUser();
        StepVerifier.create(response).assertNext(this::verifyUserNotEmpty).verifyComplete();
    }

    @Test
    public void createUserWithResponse() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserWithResponse");

        // Action & Assert
        Mono<Response<CommunicationUserIdentifier>> response = asyncClient.createUserWithResponse();
        StepVerifier.create(response).assertNext(item -> {
            assertEquals(201, item.getStatusCode(), "Expect status code to be 201");
            verifyUserNotEmpty(item.getValue());
        }).verifyComplete();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenScopeTestHelper#getTokenScopes")
    public void createUserAndToken(String testName, List<CommunicationTokenScope> scopes) {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWith" + testName);

        // Action & Assert
        Mono<CommunicationUserIdentifierAndToken> createUserAndToken = asyncClient.createUserAndToken(scopes);
        StepVerifier.create(createUserAndToken).assertNext(result -> {
            verifyUserNotEmpty(result.getUser());
            verifyTokenNotEmpty(result.getUserToken());
        }).verifyComplete();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void createUserAndTokenWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWithValidCustomExpiration " + testName);

        // Action & Assert
        Mono<CommunicationUserIdentifierAndToken> createUserAndToken
            = asyncClient.createUserAndToken(SCOPES, tokenExpiresIn);
        StepVerifier.create(createUserAndToken).assertNext(result -> {
            verifyUserNotEmpty(result.getUser());
            verifyTokenNotEmpty(result.getUserToken());
            assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, result.getUserToken().getExpiresAt());
        }).verifyComplete();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void createUserAndTokenWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWithInvalidCustomExpiration " + testName);

        // Action & Assert
        Mono<CommunicationUserIdentifierAndToken> createUserAndToken
            = asyncClient.createUserAndToken(SCOPES, tokenExpiresIn);
        StepVerifier.create(createUserAndToken).verifyErrorSatisfies(throwable -> {
            assertNotNull(throwable.getMessage());
            assertTrue(throwable.getMessage().contains("400"));
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void createUserAndTokenWithResponseWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWithResponseWithValidCustomExpiration " + testName);

        // Action & Assert
        Mono<Response<CommunicationUserIdentifierAndToken>> createUserAndToken
            = asyncClient.createUserAndTokenWithResponse(SCOPES, tokenExpiresIn);
        StepVerifier.create(createUserAndToken).assertNext(result -> {
            assertEquals(201, result.getStatusCode(), "Expect status code to be 201");
            verifyUserNotEmpty(result.getValue().getUser());
            verifyTokenNotEmpty(result.getValue().getUserToken());
            assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn,
                result.getValue().getUserToken().getExpiresAt());
        }).verifyComplete();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void createUserAndTokenWithResponseWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        asyncClient
            = setupAsyncClient(builder, "createUserAndTokenWithResponseWithInvalidCustomExpiration " + testName);

        // Action & Assert
        Mono<Response<CommunicationUserIdentifierAndToken>> createUserAndToken
            = asyncClient.createUserAndTokenWithResponse(SCOPES, tokenExpiresIn);
        StepVerifier.create(createUserAndToken).verifyErrorSatisfies(throwable -> {
            assertNotNull(throwable.getMessage());
            assertTrue(throwable.getMessage().contains("400"));
        });
    }

    @Test
    public void createUserAndTokenWithOverflownCustomExpiration() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWithOverflownCustomExpiration");
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);

        // Action & Assert
        Mono<CommunicationUserIdentifierAndToken> createUserAndToken
            = asyncClient.createUserAndToken(SCOPES, tokenExpiresIn);
        StepVerifier.create(createUserAndToken).verifyErrorSatisfies(throwable -> {
            assertTrue(throwable instanceof IllegalArgumentException);
            assertNotNull(throwable.getMessage());
            assertEquals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE, throwable.getMessage());
        });
    }

    @Test
    public void createUserAndTokenWithResponseWithOverflownCustomExpiration() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWithResponseWithOverflownCustomExpiration");
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);

        // Action & Assert
        Mono<Response<CommunicationUserIdentifierAndToken>> createUserAndToken
            = asyncClient.createUserAndTokenWithResponse(SCOPES, tokenExpiresIn);
        StepVerifier.create(createUserAndToken).verifyErrorSatisfies(throwable -> {
            assertTrue(throwable instanceof IllegalArgumentException);
            assertNotNull(throwable.getMessage());
            assertEquals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE, throwable.getMessage());
        });
    }

    @Test
    public void createUserAndTokenWithResponse() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWithResponse");

        // Action & Assert
        Mono<Response<CommunicationUserIdentifierAndToken>> createUserAndToken
            = asyncClient.createUserAndTokenWithResponse(SCOPES);
        StepVerifier.create(createUserAndToken).assertNext(result -> {
            assertEquals(201, result.getStatusCode(), "Expect status code to be 201");
            verifyUserNotEmpty(result.getValue().getUser());
            verifyTokenNotEmpty(result.getValue().getUserToken());
        }).verifyComplete();
    }

    @Test
    public void createUserAndTokenNullScopes() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserAndTokenNullScopes");

        // Action & Assert
        StepVerifier.create(asyncClient.createUserAndToken(null)).verifyError(NullPointerException.class);
    }

    @Test
    public void createUserAndTokenWithResponseNullScopes() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWithResponseNullScopes");

        // Action & Assert
        StepVerifier.create(asyncClient.createUserAndTokenWithResponse(null)).verifyError(NullPointerException.class);
    }

    @Test
    public void deleteUser() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "deleteUser");

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap(communicationUser -> {
            return asyncClient.deleteUser(communicationUser);
        })).verifyComplete();
    }

    @Test
    public void deleteUserWithResponse() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "deleteUserWithResponse");

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap(communicationUser -> {
            return asyncClient.deleteUserWithResponse(communicationUser);
        })).assertNext(item -> {
            assertEquals(204, item.getStatusCode(), "Expect status code to be 204");
        }).verifyComplete();
    }

    @Test
    public void deleteUserWithNullUser() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "deleteUserWithNullUser");

        // Action & Assert
        StepVerifier.create(asyncClient.deleteUser(null)).verifyError(NullPointerException.class);
    }

    @Test
    public void deleteUserWithResponseWithNullUser() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "deleteUserWithResponseWithNullUser");

        // Action & Assert
        StepVerifier.create(asyncClient.deleteUserWithResponse(null)).verifyError(NullPointerException.class);
    }

    @Test
    public void revokeToken() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "revokeToken");

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap((CommunicationUserIdentifier communicationUser) -> {
            return asyncClient.getToken(communicationUser, SCOPES).flatMap((AccessToken communicationUserToken) -> {
                return asyncClient.revokeTokens(communicationUser);
            });
        })).verifyComplete();
    }

    @Test
    public void revokeTokenWithResponse() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "revokeTokenWithResponse");

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap((CommunicationUserIdentifier communicationUser) -> {
            return asyncClient.getToken(communicationUser, SCOPES).flatMap((AccessToken communicationUserToken) -> {
                return asyncClient.revokeTokensWithResponse(communicationUser);
            });
        })).assertNext(item -> {
            assertEquals(204, item.getStatusCode(), "Expect status code to be 204");
        }).verifyComplete();
    }

    @Test
    public void revokeTokenWithNullUser() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "revokeTokenWithNullUser");

        // Action & Assert
        StepVerifier.create(asyncClient.revokeTokens(null)).verifyError(NullPointerException.class);
    }

    @Test
    public void revokeTokenWithResponseWithNullUser() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "revokeTokenWithResponseWithNullUser");

        // Action & Assert
        StepVerifier.create(asyncClient.revokeTokensWithResponse(null)).verifyError(NullPointerException.class);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenScopeTestHelper#getTokenScopes")
    public void getToken(String testName, List<CommunicationTokenScope> scopes) {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWith" + testName);

        // Action & Assert
        StepVerifier
            .create(
                asyncClient.createUser().flatMap(communicationUser -> asyncClient.getToken(communicationUser, scopes)))
            .assertNext(this::verifyTokenNotEmpty)
            .verifyComplete();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void getTokenWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithValidCustomExpiration " + testName);

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap(communicationUser -> {
            return asyncClient.getToken(communicationUser, SCOPES, tokenExpiresIn);
        })).assertNext(issuedToken -> {
            verifyTokenNotEmpty(issuedToken);
            assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, issuedToken.getExpiresAt());
        }).verifyComplete();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void getTokenWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithInvalidCustomExpiration " + testName);

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap(communicationUser -> {
            return asyncClient.getToken(communicationUser, SCOPES, tokenExpiresIn);
        })).verifyErrorSatisfies(throwable -> {
            assertNotNull(throwable.getMessage());
            assertTrue(throwable.getMessage().contains("400"));
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getValidExpirationTimes")
    public void getTokenWithResponseWithValidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithResponseWithValidCustomExpiration " + testName);

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap(communicationUser -> {
            return asyncClient.getTokenWithResponse(communicationUser, SCOPES, tokenExpiresIn);
        })).assertNext(issuedToken -> {
            verifyTokenNotEmpty(issuedToken.getValue());
            assertEquals(issuedToken.getStatusCode(), 200);
            assertTokenExpirationWithinAllowedDeviation(tokenExpiresIn, issuedToken.getValue().getExpiresAt());
        }).verifyComplete();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.TokenCustomExpirationTimeHelper#getInvalidExpirationTimes")
    public void getTokenWithResponseWithInvalidCustomExpiration(String testName, Duration tokenExpiresIn) {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithResponseWithInvalidCustomExpiration " + testName);

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap(communicationUser -> {
            return asyncClient.getTokenWithResponse(communicationUser, SCOPES, tokenExpiresIn);
        })).verifyErrorSatisfies(throwable -> {
            assertNotNull(throwable.getMessage());
            assertTrue(throwable.getMessage().contains("400"));
        });
    }

    @Test
    public void getTokenWithOverflownCustomExpiration() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithOverflownCustomExpiration");
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap(communicationUser -> {
            return asyncClient.getToken(communicationUser, SCOPES, tokenExpiresIn);
        })).verifyErrorSatisfies(throwable -> {
            assertTrue(throwable instanceof IllegalArgumentException);
            assertNotNull(throwable.getMessage());
            assertEquals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE, throwable.getMessage());
        });
    }

    @Test
    public void getTokenWithResponseWithOverflownCustomExpiration() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithResponseWithOverflownCustomExpiration");
        Duration tokenExpiresIn = Duration.ofDays(Integer.MAX_VALUE);

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap(communicationUser -> {
            return asyncClient.getTokenWithResponse(communicationUser, SCOPES, tokenExpiresIn);
        })).verifyErrorSatisfies(throwable -> {
            assertTrue(throwable instanceof IllegalArgumentException);
            assertNotNull(throwable.getMessage());
            assertEquals(CommunicationIdentityClientUtils.TOKEN_EXPIRATION_OVERFLOW_MESSAGE, throwable.getMessage());
        });
    }

    @Test
    public void getTokenWithResponse() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithResponse");

        // Action & Assert
        StepVerifier.create(asyncClient.createUser().flatMap(communicationUser -> {
            return asyncClient.getTokenWithResponse(communicationUser, SCOPES);
        })).assertNext(issuedToken -> {
            verifyTokenNotEmpty(issuedToken.getValue());
            assertEquals(issuedToken.getStatusCode(), 200);
        }).verifyComplete();
    }

    @Test
    public void getTokenWithNullUser() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithNullUser");

        // Action & Assert
        StepVerifier.create(asyncClient.getToken(null, SCOPES)).verifyError(NullPointerException.class);
    }

    @Test
    public void getTokenWithNullScope() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithNullScope");

        // Action & Assert
        StepVerifier.create(asyncClient.getToken(new CommunicationUserIdentifier("testUser"), null))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void getTokenWithResponseWithNullUser() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithResponseWithNullUser");

        // Action & Assert
        StepVerifier.create(asyncClient.getTokenWithResponse(null, SCOPES)).verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithValidParams(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenForTeamsUserWithValidParams");

        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(options);
        StepVerifier.create(response).assertNext(this::verifyTokenNotEmpty).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithValidParamsWithResponse(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenForTeamsUserWithValidParamsWithResponse");

        // Action & Assert
        Mono<Response<AccessToken>> response = asyncClient.getTokenForTeamsUserWithResponse(options);
        StepVerifier.create(response).assertNext(issuedTokenResponse -> {
            verifyTokenNotEmpty(issuedTokenResponse.getValue());
            assertEquals(200, issuedTokenResponse.getStatusCode(), "Expect status code to be 201");
        }).verifyComplete();
    }

    @ParameterizedTest(name = "when {1} is null")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getNullParams")
    public void getTokenForTeamsUserWithNullParams(GetTokenForTeamsUserOptions options, String exceptionMessage) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenForTeamsUserWithNull:when " + exceptionMessage + " is null");

        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(options);
        StepVerifier.create(response).verifyErrorSatisfies(throwable -> {
            assertNotNull(throwable.getMessage());
            assertTrue(throwable.getMessage().contains(exceptionMessage));
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getInvalidTokens")
    public void getTokenForTeamsUserWithInvalidToken(String testName, GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        asyncClient = setupAsyncClient(builder, testName);

        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(options);
        StepVerifier.create(response).verifyErrorSatisfies(throwable -> {
            assertNotNull(throwable.getMessage());
            assertTrue(throwable.getMessage().contains("401"));
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getInvalidAppIds")
    public void getTokenForTeamsUserWithInvalidAppId(String testName, GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        asyncClient = setupAsyncClient(builder, testName);

        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(options);
        StepVerifier.create(response).verifyErrorSatisfies(throwable -> {
            assertNotNull(throwable.getMessage());
            assertTrue(throwable.getMessage().contains("400"));
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getInvalidUserIds")
    public void getTokenForTeamsUserWithInvalidUserId(String testName, GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        asyncClient = setupAsyncClient(builder, testName);

        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(options);
        StepVerifier.create(response).verifyErrorSatisfies(throwable -> {
            assertNotNull(throwable.getMessage());
            assertTrue(throwable.getMessage().contains("400"));
        });
    }
}
