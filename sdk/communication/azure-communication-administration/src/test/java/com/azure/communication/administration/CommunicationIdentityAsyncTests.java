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
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


public class CommunicationIdentityAsyncTests extends CommunicationIdentityClientTestBase {

    private CommunicationIdentityAsyncClient asyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        asyncClient = getCommunicationIdentityClient().buildAsyncClient();
    }

    @Test
    public void createAsyncIdentityClientUsingConnectionString() {
        //Arrange
        asyncClient = getCommunicationIdentityClientUsingConnectionString().buildAsyncClient();
        assertNotNull(asyncClient);

        //Act & Assert
        Mono<CommunicationUser> response = asyncClient.createUser();
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getId());
                assertFalse(item.getId().isEmpty());
            })
            .verifyComplete();
    }

    @Test
    public void createUser() {
        Mono<CommunicationUser> response = asyncClient.createUser();
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getId());
            })
            .verifyComplete();
    }

    @Test
    public void createUserWithResponse() {
        Mono<Response<CommunicationUser>> response = asyncClient.createUserWithResponse();
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getValue().getId());
                assertFalse(item.getValue().getId().isEmpty());
                assertEquals(200, item.getStatusCode(), "Expect status code to be 200");
            })
            .verifyComplete();
    }


    @Test
    public void createUserWithContext() {
        Mono<Response<CommunicationUser>> response = asyncClient.createUser(Context.NONE);
        StepVerifier.create(response)
            .assertNext(item -> {
                assertNotNull(item.getValue().getId());
                assertFalse(item.getValue().getId().isEmpty());
                assertEquals(200, item.getStatusCode(), "Expect status code to be 200");
            })
            .verifyComplete();
    }

    @Test
    public void deleteUser() {
        //Arrange
        CommunicationUser communicationUser = asyncClient.createUser().block();

        //Act & Assert
        StepVerifier.create(asyncClient.deleteUser(communicationUser))
            .verifyComplete();
    }

    @Test
    public void deleteUserWithResponse() {
        //Arrange
        CommunicationUser communicationUser = asyncClient.createUser().block();

        //Act & Assert
        StepVerifier.create(asyncClient.deleteUserWithResponse(communicationUser))
            .assertNext(item -> {
                assertEquals(204, item.getStatusCode(), "Expect status code to be 204");
            })
            .verifyComplete();
    }

    @Test
    public void revokeToken() {
        //Arrange
        Mono<CommunicationUser> createUserReponse = asyncClient.createUser();
        CommunicationUser communicationUser = createUserReponse.block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken communicationUserWithToken = asyncClient.issueToken(communicationUser, scopes).block();

        //Act & Assert
        StepVerifier.create(asyncClient.revokeTokens(communicationUserWithToken.getUser(), null))
            .verifyComplete();
    }

    @Test
    public void revokeTokenWithResponse() {
        //Arrange
        Mono<CommunicationUser> createUserReponse = asyncClient.createUser();
        CommunicationUser communicationUser = createUserReponse.block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken communicationUserWithToken = asyncClient.issueToken(communicationUser, scopes).block();

        //Act & Assert
        StepVerifier.create(asyncClient.revokeTokensWithResponse(communicationUserWithToken.getUser(), null))
            .assertNext(item -> {
                assertEquals(204, item.getStatusCode(), "Expect status code to be 204");        
            })
            .verifyComplete();
    }

    @Test
    public void issueToken() {
        //Arrange
        CommunicationUser communicationUser = asyncClient.createUser().block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        Mono<CommunicationUserToken> response = asyncClient.issueToken(communicationUser, scopes);

        //Act & Assert
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

    @Test
    public void issueTokenWithResponse() {
        //Arrange
        CommunicationUser communicationUser = asyncClient.createUser().block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        Mono<Response<CommunicationUserToken>> response = asyncClient.issueTokenWithResponse(communicationUser, scopes);

        //Act & Assert
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
