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
        StepVerifier.create(
            asyncClient.createUser()
                .flatMap(communicationUser -> {
                    return asyncClient.deleteUser(communicationUser);
                }))
            .verifyComplete();
    }

    @Test
    public void deleteUserWithResponse() {
        //Arrange
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

    @Test
    public void revokeTokenWithResponse() {
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

    @Test
    public void issueToken() {
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

    @Test
    public void issueTokenWithResponse() {
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
