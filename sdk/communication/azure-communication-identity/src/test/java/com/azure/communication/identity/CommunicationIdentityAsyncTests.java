// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import java.util.Arrays;
import java.util.List;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

import static com.azure.communication.identity.CteTestHelper.skipExchangeAadTeamsTokenTest;

public class CommunicationIdentityAsyncTests extends CommunicationIdentityClientTestBase {
    private CommunicationIdentityAsyncClient asyncClient;

    @Test
    public void createAsyncIdentityClientUsingConnectionString() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingConnectionString(httpClient);
        asyncClient = setupAsyncClient(builder, "createAsyncIdentityClientUsingConnectionString");
        assertNotNull(asyncClient);

        // Action & Assert
        Mono<CommunicationUserIdentifier> response = asyncClient.createUser();
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getId());
                assertFalse(item.getId().isEmpty());
            })
            .verifyComplete();
    }

    @Test
    public void createUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "createUser");

        // Action & Assert
        Mono<CommunicationUserIdentifier> response = asyncClient.createUser();
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getId());
            })
            .verifyComplete();
    }

    @Test
    public void createUserWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "createUserWithResponse");

        // Action & Assert
        Mono<Response<CommunicationUserIdentifier>> response = asyncClient.createUserWithResponse();
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getValue().getId());
                assertFalse(item.getValue().getId().isEmpty());
                assertEquals(201, item.getStatusCode(), "Expect status code to be 201");
            })
            .verifyComplete();
    }

    @Test
    public void createUserAndToken() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "createUserAndToken");
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        Mono<CommunicationUserIdentifierAndToken> createUserAndToken = asyncClient.createUserAndToken(scopes);
        StepVerifier.create(createUserAndToken)
            .assertNext(result -> {
                assertNotNull(result.getUserToken());
                assertNotNull(result.getUser());
            })
            .verifyComplete();
    }

    @Test
    public void createUserAndTokenWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWithResponse");
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        Mono<Response<CommunicationUserIdentifierAndToken>> createUserAndToken =
            asyncClient.createUserAndTokenWithResponse(scopes);
        StepVerifier.create(createUserAndToken)
            .assertNext(result -> {
                assertEquals(201, result.getStatusCode());
                assertNotNull(result.getValue().getUserToken());
                assertNotNull(result.getValue().getUser());
            })
            .verifyComplete();
    }

    @Test
    public void createUserAndTokenNullScopes() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "createUserAndTokenNullScopes");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUserAndToken(null))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void createUserAndTokenWithResponseNullScopes() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWithResponseNullScopes");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUserAndTokenWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void deleteUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "deleteUser");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    return asyncClient.deleteUser(communicationUser);
                }))
            .verifyComplete();
    }

    @Test
    public void deleteUserWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "deleteUserWithResponse");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    return asyncClient.deleteUserWithResponse(communicationUser);
                }))
            .assertNext(item -> {
                assertEquals(204, item.getStatusCode(), "Expect status code to be 204");
            })
            .verifyComplete();
    }

    @Test
    public void deleteUserWithNullUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "deleteUserWithNullUser");

        // Action & Assert
        StepVerifier.create(
            asyncClient.deleteUser(null))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void deleteUserWithResponseWithNullUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "deleteUserWithResponseWithNullUser");

        // Action & Assert
        StepVerifier.create(
            asyncClient.deleteUserWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void revokeToken() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "revokeToken");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap((CommunicationUserIdentifier communicationUser) -> {
                    List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
                    return asyncClient.getToken(communicationUser, scopes)
                        .flatMap((AccessToken communicationUserToken) -> {
                            return asyncClient.revokeTokens(communicationUser);
                        });
                }))
            .verifyComplete();
    }

    @Test
    public void revokeTokenWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "revokeTokenWithResponse");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap((CommunicationUserIdentifier communicationUser) -> {
                    List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
                    return asyncClient.getToken(communicationUser, scopes)
                        .flatMap((AccessToken communicationUserToken) -> {
                            return asyncClient.revokeTokensWithResponse(communicationUser);
                        });
                }))
            .assertNext(item -> {
                assertEquals(204, item.getStatusCode(), "Expect status code to be 204");
            })
            .verifyComplete();
    }

    @Test
    public void revokeTokenWithNullUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "revokeTokenWithNullUser");

        // Action & Assert
        StepVerifier.create(
            asyncClient.revokeTokens(null))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void revokeTokenWithResponseWithNullUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "revokeTokenWithResponseWithNullUser");

        // Action & Assert
        StepVerifier.create(
            asyncClient.revokeTokensWithResponse(null))
            .verifyError(NullPointerException.class);
    }


    @Test
    public void getToken() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "getToken");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
                    return asyncClient.getToken(communicationUser, scopes);
                }))
            .assertNext(issuedToken -> verifyTokenNotEmpty(issuedToken))
            .verifyComplete();
    }

    @Test
    public void getTokenWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenWithResponse");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
                    return asyncClient.getTokenWithResponse(communicationUser, scopes);
                }))
            .assertNext(issuedToken -> {
                verifyTokenNotEmpty(issuedToken.getValue());
                assertEquals(issuedToken.getStatusCode(), 200);
            })
            .verifyComplete();
    }

    @Test
    public void getTokenWithNullUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenWithNullUser");
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        StepVerifier.create(
            asyncClient.getToken(null, scopes))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void getTokenWithNullScope() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenWithNullScope");

        // Action & Assert
        StepVerifier.create(asyncClient.getToken(new CommunicationUserIdentifier("testUser"), null))
            .verifyError(NullPointerException.class);
    }

    @Test
    public void getTokenWithResponseWithNullUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenWithResponseWithNullUser");
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        StepVerifier.create(
            asyncClient.getTokenWithResponse(null, scopes))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithValidParams(String teamsUserAadToken, String appId, String userId) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenForTeamsUserWithValidParams");
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(teamsUserAadToken, appId, userId);
        StepVerifier.create(response)
                .assertNext(issuedToken -> verifyTokenNotEmpty(issuedToken))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithValidParamsWithResponse(String teamsUserAadToken, String appId, String userId) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenForTeamsUserWithValidParamsWithResponse");
        // Action & Assert
        Mono<Response<AccessToken>> response = asyncClient.getTokenForTeamsUserWithResponse(teamsUserAadToken, appId, userId);
        StepVerifier.create(response)
                .assertNext(issuedTokenResponse -> {
                    verifyTokenNotEmpty(issuedTokenResponse.getValue());
                    assertEquals(200, issuedTokenResponse.getStatusCode(), "Expect status code to be 201");
                })
                .verifyComplete();
    }

    @ParameterizedTest(name = "when {3} is null")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getNullParams")
    public void getTokenForTeamsUserWithNullParams(String teamsUserAadToken, String appId, String userId, String exceptionMessage) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenForTeamsUserWithNull:when " + exceptionMessage + " is null");
        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(teamsUserAadToken, appId, userId);
        StepVerifier.create(response)
                .verifyErrorSatisfies(throwable -> {
                    assertNotNull(throwable.getMessage());
                    assertTrue(throwable.getMessage().contains(exceptionMessage));
                });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getInvalidTokens")
    public void getTokenForTeamsUserWithInvalidToken(String testName, String invalidTeamsUserAadToken, String appId, String userId) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, testName);
        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(invalidTeamsUserAadToken, appId, userId);
        StepVerifier.create(response)
            .verifyErrorSatisfies(throwable -> {
                assertNotNull(throwable.getMessage());
                assertTrue(throwable.getMessage().contains("401"));
            });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getInvalidAppIds")
    public void getTokenForTeamsUserWithInvalidAppId(String testName, String teamsUserAadToken, String invalidAppId, String userId) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, testName);
        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(teamsUserAadToken, invalidAppId, userId);
        StepVerifier.create(response)
                .verifyErrorSatisfies(throwable -> {
                    assertNotNull(throwable.getMessage());
                    assertTrue(throwable.getMessage().contains("400"));
                });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.communication.identity.CteTestHelper#getInvalidUserIds")
    public void getTokenForTeamsUserWithInvalidUserId(String testName, String teamsUserAadToken, String appId, String invalidUserId) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, testName);
        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(teamsUserAadToken, appId, invalidUserId);
        StepVerifier.create(response)
                .verifyErrorSatisfies(throwable -> {
                    assertNotNull(throwable.getMessage());
                    assertTrue(throwable.getMessage().contains("400"));
                });
    }

}
