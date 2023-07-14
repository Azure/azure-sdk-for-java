// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.GetTokenForTeamsUserOptions;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static com.azure.communication.identity.CteTestHelper.skipExchangeAadTeamsTokenTest;
import static com.azure.communication.identity.models.CommunicationTokenScope.CHAT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommunicationIdentityAsyncManagedIdentityTests extends CommunicationIdentityClientTestBase {

    private static final List<CommunicationTokenScope> SCOPES = Collections.singletonList(CHAT);
    private CommunicationIdentityAsyncClient asyncClient;
    private CommunicationIdentityClientBuilder builder;

    @Override
    public void beforeTest() {
        super.beforeTest();
        builder = createClientBuilderUsingManagedIdentity(buildAsyncAssertingClient(httpClient));
    }

    @Test
    public void createAsyncIdentityClient() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createAsyncIdentityClientUsingManagedIdentity");
        assertNotNull(asyncClient);

        // Action & Assert
        Mono<CommunicationUserIdentifier> response = asyncClient.createUser();
        StepVerifier.create(response)
                .assertNext(this::verifyUserNotEmpty)
                .verifyComplete();
    }

    @Test
    public void createUserWithResponse() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "createUserWithResponseUsingManagedIdentity");

        // Action & Assert
        Mono<Response<CommunicationUserIdentifier>> response = asyncClient.createUserWithResponse();
        StepVerifier.create(response)
                .assertNext(item -> {
                    assertEquals(201, item.getStatusCode(), "Expect status code to be 201");
                    verifyUserNotEmpty(item.getValue());
                })
                .verifyComplete();
    }

    @Test
    public void getToken() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenUsingManagedIdentity");

        // Action & Assert
        StepVerifier.create(
                asyncClient.createUser()
                    .flatMap(communicationUser -> {
                        return asyncClient.getToken(communicationUser, SCOPES);
                    }))
            .assertNext(this::verifyTokenNotEmpty)
            .verifyComplete();
    }

    @Test
    public void getTokenWithResponse() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenWithResponseUsingManagedIdentity");

        // Action & Assert
        StepVerifier.create(
                asyncClient.createUser()
                    .flatMap(communicationUser -> {
                        return asyncClient.getTokenWithResponse(communicationUser, SCOPES);
                    }))
            .assertNext(issuedToken -> {
                assertEquals(200, issuedToken.getStatusCode(), "Expect status code to be 200");
                verifyTokenNotEmpty(issuedToken.getValue());
            })
            .verifyComplete();
    }

    @Test
    public void deleteUser() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "deleteUserUsingManagedIdentity");

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
        asyncClient = setupAsyncClient(builder, "deleteUserWithResponseUsingManagedIdentity");

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
    public void revokeToken() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "revokeTokenUsingManagedIdentity");

        // Action & Assert
        StepVerifier.create(
                        asyncClient.createUser()
                                .flatMap(communicationUser -> {
                                    return asyncClient.getToken(communicationUser, SCOPES)
                                            .flatMap(communicationUserToken -> {
                                                return asyncClient.revokeTokens(communicationUser);
                                            });
                                }))
                .verifyComplete();
    }

    @Test
    public void revokeTokenWithResponse() {
        // Arrange
        asyncClient = setupAsyncClient(builder, "revokeTokenWithResponseUsingManagedIdentity");

        // Action & Assert
        StepVerifier.create(
                        asyncClient.createUser()
                                .flatMap(communicationUser -> {
                                    return asyncClient.getToken(communicationUser, SCOPES)
                                            .flatMap(communicationUserToken -> {
                                                return asyncClient.revokeTokensWithResponse(communicationUser);
                                            });
                                }))
                .assertNext(item -> {
                    assertEquals(204, item.getStatusCode(), "Expect status code to be 204");
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUser(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }

        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenForTeamsUserUsingManagedIdentity");

        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(options);
        StepVerifier.create(response)
                .assertNext(this::verifyTokenNotEmpty)
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithResponse(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }

        // Arrange
        asyncClient = setupAsyncClient(builder, "getTokenForTeamsUserWithResponseUsingManagedIdentity");

        // Action & Assert
        Mono<Response<AccessToken>> response = asyncClient.getTokenForTeamsUserWithResponse(options);
        StepVerifier.create(response)
                .assertNext(issuedTokenResponse -> {
                    verifyTokenNotEmpty(issuedTokenResponse.getValue());
                    assertEquals(200, issuedTokenResponse.getStatusCode(), "Expect status code to be 200");
                })
                .verifyComplete();
    }
}
