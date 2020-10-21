// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.communication.common.CommunicationUser;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


public class CommunicationIdentityAsyncTests extends CommunicationIdentityClientTestBase {

    private CommunicationIdentityAsyncClient asyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createAsyncIdentityClientUsingConnectionString(HttpClient httpClient) {
        // Arrange
        asyncClient = getCommunicationIdentityClientUsingConnectionString(httpClient).buildAsyncClient();
        assertNotNull(asyncClient);

        // Action & Assert
        Mono<CommunicationUser> response = asyncClient.createUser();
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
        asyncClient = getCommunicationIdentityClient(httpClient).buildAsyncClient();

        // Action & Assert
        Mono<CommunicationUser> response = asyncClient.createUser();
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
        asyncClient = getCommunicationIdentityClient(httpClient).buildAsyncClient();

        // Action & Assert
        Mono<Response<CommunicationUser>> response = asyncClient.createUserWithResponse();
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getValue().getId());
                assertFalse(item.getValue().getId().isEmpty());
                assertEquals(200, item.getStatusCode(), "Expect status code to be 200");
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserWithContext(HttpClient httpClient) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(httpClient).buildAsyncClient();

        // Action & Assert
        Mono<Response<CommunicationUser>> response = asyncClient.createUser(Context.NONE);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getValue().getId());
                assertFalse(item.getValue().getId().isEmpty());
                assertEquals(200, item.getStatusCode(), "Expect status code to be 200");
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUser(HttpClient httpClient) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(httpClient).buildAsyncClient();

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
        asyncClient = getCommunicationIdentityClient(httpClient).buildAsyncClient();

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
    public void revokeToken(HttpClient httpClient) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(httpClient).buildAsyncClient();

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
                    return asyncClient.issueToken(communicationUser, scopes)
                        .flatMap(communicationUserToken -> {
                            return asyncClient.revokeTokens(communicationUserToken.getUser(), null);
                        });
                }))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenWithResponse(HttpClient httpClient) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(httpClient).buildAsyncClient();

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
                    return asyncClient.issueToken(communicationUser, scopes)
                        .flatMap(communicationUserToken -> {
                            return asyncClient.revokeTokensWithResponse(communicationUserToken.getUser(), null);
                        });
                }))
            .assertNext(item -> {
                assertEquals(204, item.getStatusCode(), "Expect status code to be 204");    
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void issueToken(HttpClient httpClient) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(httpClient).buildAsyncClient();

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
                    return asyncClient.issueToken(communicationUser, scopes);
                }))
            .assertNext(issuedToken -> {
                assertNotNull(issuedToken.getToken());
                assertFalse(issuedToken.getToken().isEmpty());
                assertNotNull(issuedToken.getExpiresOn());
                assertFalse(issuedToken.getExpiresOn().toString().isEmpty());
                assertNotNull(issuedToken.getUser());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void issueTokenWithResponse(HttpClient httpClient) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(httpClient).buildAsyncClient();

        // Action & Assert
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
                    return asyncClient.issueTokenWithResponse(communicationUser, scopes);
                }))
            .assertNext(issuedToken -> {
                assertNotNull(issuedToken.getValue().getToken());
                assertFalse(issuedToken.getValue().getToken().isEmpty());
                assertNotNull(issuedToken.getValue().getExpiresOn());
                assertFalse(issuedToken.getValue().getExpiresOn().toString().isEmpty());
                assertNotNull(issuedToken.getValue().getUser());
            })
            .verifyComplete();
    }
}
