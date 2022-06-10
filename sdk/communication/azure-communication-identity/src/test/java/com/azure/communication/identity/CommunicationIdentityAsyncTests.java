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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class CommunicationIdentityAsyncTests extends CommunicationIdentityClientTestBase {
    private CommunicationIdentityAsyncClient asyncClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createAsyncIdentityClientUsingManagedIdentity(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createAsyncIdentityClientUsingConnectionString(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUser(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserWithResponse(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserAndToken(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserAndTokenWithResponse(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserAndTokenNullScopes(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "createUserAndTokenNullScopes");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUserAndToken(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserAndTokenWithResponseNullScopes(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "createUserAndTokenWithResponseNullScopes");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUserAndTokenWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUser(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserWithResponse(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserWithNullUser(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "deleteUserWithNullUser");

        // Action & Assert
        StepVerifier.create(
            asyncClient.deleteUser(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserWithResponseWithNullUser(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "deleteUserWithResponseWithNullUser");

        // Action & Assert
        StepVerifier.create(
            asyncClient.deleteUserWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeToken(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenWithResponse(HttpClient httpClient) {
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenWithNullUser(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "revokeTokenWithNullUser");

        // Action & Assert
        StepVerifier.create(
            asyncClient.revokeTokens(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenWithResponseWithNullUser(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "revokeTokenWithResponseWithNullUser");

        // Action & Assert
        StepVerifier.create(
            asyncClient.revokeTokensWithResponse(null))
            .verifyError(NullPointerException.class);
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getToken(HttpClient httpClient) {
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
            .assertNext(issuedToken -> {
                assertNotNull(issuedToken.getToken());
                assertFalse(issuedToken.getToken().isEmpty());
                assertNotNull(issuedToken.getExpiresAt());
                assertFalse(issuedToken.getExpiresAt().toString().isEmpty());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenWithResponse(HttpClient httpClient) {
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
                assertNotNull(issuedToken.getValue().getToken());
                assertFalse(issuedToken.getValue().getToken().isEmpty());
                assertNotNull(issuedToken.getValue().getExpiresAt());
                assertFalse(issuedToken.getValue().getExpiresAt().toString().isEmpty());
                assertEquals(issuedToken.getStatusCode(), 200);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenWithNullUser(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenWithNullUser");
        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);

        // Action & Assert
        StepVerifier.create(
            asyncClient.getToken(null, scopes))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenWithNullScope(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilder(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenWithNullScope");

        // Action & Assert
        StepVerifier.create(asyncClient.getToken(new CommunicationUserIdentifier("testUser"), null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenWithResponseWithNullUser(HttpClient httpClient) {
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
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserWithResponseUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder =  createClientBuilderUsingManagedIdentity(httpClient);
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserWithContextUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder = createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "createUserWithContextUsingManagedIdentity");

        // Action & Assert
        Mono<CommunicationUserIdentifier> response = asyncClient.createUser();
        StepVerifier.create(response)
            .assertNext(user -> {
                assertNotNull(user.getId());
                assertFalse(user.getId().isEmpty());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder =  createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "deleteUserUsingManagedIdentity");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    return asyncClient.deleteUser(communicationUser);
                }))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserWithResponseUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder =  createClientBuilderUsingManagedIdentity(httpClient);
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder =  createClientBuilderUsingManagedIdentity(httpClient);
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenWithResponseUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder =  createClientBuilderUsingManagedIdentity(httpClient);
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder =  createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenUsingManagedIdentity");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
                    return asyncClient.getToken(communicationUser, scopes);
                }))
            .assertNext(issuedToken -> {
                assertNotNull(issuedToken.getToken());
                assertFalse(issuedToken.getToken().isEmpty());
                assertNotNull(issuedToken.getExpiresAt());
                assertFalse(issuedToken.getExpiresAt().toString().isEmpty());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTokenWithResponseUsingManagedIdentity(HttpClient httpClient) {
        // Arrange
        CommunicationIdentityClientBuilder builder =  createClientBuilderUsingManagedIdentity(httpClient);
        asyncClient = setupAsyncClient(builder, "getTokenWithResponseUsingManagedIdentity");

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
                    return asyncClient.getTokenWithResponse(communicationUser, scopes);
                }))
            .assertNext(issuedToken -> {
                assertNotNull(issuedToken.getValue().getToken());
                assertFalse(issuedToken.getValue().getToken().isEmpty());
                assertNotNull(issuedToken.getValue().getExpiresAt());
                assertFalse(issuedToken.getValue().getExpiresAt().toString().isEmpty());
            })
            .verifyComplete();
    }

    private CommunicationIdentityAsyncClient setupAsyncClient(CommunicationIdentityClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
