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
    public void createAsyncIdentityClientUsingConnectionString(HttpClient client) {
        // Arrange
        asyncClient = getCommunicationIdentityClientUsingConnectionString(client).buildAsyncClient();
        assertNotNull(asyncClient);

        // Act & Assert
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
    public void createUser(HttpClient client) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(client).buildAsyncClient();

        // Act & Assert
        Mono<CommunicationUser> response = asyncClient.createUser();
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createUserWithResponse(HttpClient client) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(client).buildAsyncClient();

        // Act & Assert
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
    public void createUserWithContext(HttpClient client) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(client).buildAsyncClient();

        // Act & Assert
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
    public void deleteUser(HttpClient client) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(client).buildAsyncClient();
        CommunicationUser communicationUser = asyncClient.createUser().block();

        // Act & Assert
        StepVerifier.create(asyncClient.deleteUser(communicationUser))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteUserWithResponse(HttpClient client) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(client).buildAsyncClient();
        CommunicationUser communicationUser = asyncClient.createUser().block();

        // Act & Assert
        StepVerifier.create(asyncClient.deleteUserWithResponse(communicationUser))
            .assertNext(item -> {
                assertEquals(204, item.getStatusCode(), "Expect status code to be 204");
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeToken(HttpClient client) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(client).buildAsyncClient();
        Mono<CommunicationUser> createUserReponse = asyncClient.createUser();
        CommunicationUser communicationUser = createUserReponse.block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken communicationUserWithToken = asyncClient.issueToken(communicationUser, scopes).block();

        // Act & Assert
        StepVerifier.create(asyncClient.revokeTokens(communicationUserWithToken.getUser(), null))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void revokeTokenWithResponse(HttpClient client) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(client).buildAsyncClient();
        Mono<CommunicationUser> createUserReponse = asyncClient.createUser();
        CommunicationUser communicationUser = createUserReponse.block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken communicationUserWithToken = asyncClient.issueToken(communicationUser, scopes).block();

        // Act & Assert
        StepVerifier.create(asyncClient.revokeTokensWithResponse(communicationUserWithToken.getUser(), null))
            .assertNext(item -> {
                assertEquals(204, item.getStatusCode(), "Expect status code to be 204");        
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void issueToken(HttpClient client) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(client).buildAsyncClient();
        CommunicationUser communicationUser = asyncClient.createUser().block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        Mono<CommunicationUserToken> response = asyncClient.issueToken(communicationUser, scopes);

        // Act & Assert
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getToken());
                assertFalse(item.getToken().isEmpty());
                assertNotNull(item.getExpiresOn());
                assertFalse(item.getExpiresOn().toString().isEmpty());
                assertNotNull(item.getUser());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void issueTokenWithResponse(HttpClient client) {
        // Arrange
        asyncClient = getCommunicationIdentityClient(client).buildAsyncClient();
        CommunicationUser communicationUser = asyncClient.createUser().block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        Mono<Response<CommunicationUserToken>> response = asyncClient.issueTokenWithResponse(communicationUser, scopes);

        // Act & Assert
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getValue().getToken());
                assertFalse(item.getValue().getToken().isEmpty());
                assertNotNull(item.getValue().getExpiresOn());
                assertFalse(item.getValue().getExpiresOn().toString().isEmpty());
                assertNotNull(item.getValue().getUser());
                assertEquals(200, item.getStatusCode(), "Expect response status code to be 200");
            })
            .verifyComplete();
    }
}
