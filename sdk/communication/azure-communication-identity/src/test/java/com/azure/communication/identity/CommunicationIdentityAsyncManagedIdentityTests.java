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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.azure.communication.identity.CteTestHelper.skipExchangeAadTeamsTokenTest;

public class CommunicationIdentityAsyncManagedIdentityTests extends CommunicationIdentityClientTestBase {

    private CommunicationIdentityAsyncClient asyncClient;

    @Test
    public void createAsyncIdentityClient() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createAsyncIdentityClientUsingManagedIdentity");
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
    public void createUserWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createUserWithResponseUsingManagedIdentity");

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
    public void deleteUser() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
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
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
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
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "revokeTokenUsingManagedIdentity");

        // Action & Assert
        StepVerifier.create(
                        asyncClient.createUser()
                                .flatMap(communicationUser -> {
                                    List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
                                    return asyncClient.getToken(communicationUser, scopes)
                                            .flatMap(communicationUserToken -> {
                                                return asyncClient.revokeTokens(communicationUser);
                                            });
                                }))
                .verifyComplete();
    }

    @Test
    public void revokeTokenWithResponse() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "revokeTokenWithResponseUsingManagedIdentity");

        // Action & Assert
        StepVerifier.create(
                        asyncClient.createUser()
                                .flatMap(communicationUser -> {
                                    List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
                                    return asyncClient.getToken(communicationUser, scopes)
                                            .flatMap(communicationUserToken -> {
                                                return asyncClient.revokeTokensWithResponse(communicationUser);
                                            });
                                }))
                .assertNext(item -> {
                    assertEquals(204, item.getStatusCode(), "Expect status code to be 204");
                })
                .verifyComplete();
    }

    @Test
    public void getToken() {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenUsingManagedIdentity");

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
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenWithResponseUsingManagedIdentity");

        // Action & Assert
        StepVerifier.create(
                        asyncClient.createUser()
                                .flatMap(communicationUser -> {
                                    List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
                                    return asyncClient.getTokenWithResponse(communicationUser, scopes);
                                }))
                .assertNext(issuedToken -> verifyTokenNotEmpty(issuedToken.getValue()))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUser(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }

        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenForTeamsUserUsingManagedIdentity");
        // Action & Assert
        Mono<AccessToken> response = asyncClient.getTokenForTeamsUser(options);
        StepVerifier.create(response)
                .assertNext(issuedToken -> verifyTokenNotEmpty(issuedToken))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.communication.identity.CteTestHelper#getValidParams")
    public void getTokenForTeamsUserWithResponse(GetTokenForTeamsUserOptions options) {
        if (skipExchangeAadTeamsTokenTest()) {
            return;
        }

        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenForTeamsUserWithResponseUsingManagedIdentity");
        // Action & Assert
        Mono<Response<AccessToken>> response = asyncClient.getTokenForTeamsUserWithResponse(options);
        StepVerifier.create(response)
                .assertNext(issuedTokenResponse -> {
                    verifyTokenNotEmpty(issuedTokenResponse.getValue());
                    assertEquals(200, issuedTokenResponse.getStatusCode(), "Expect status code to be 201");
                })
                .verifyComplete();
    }

}
