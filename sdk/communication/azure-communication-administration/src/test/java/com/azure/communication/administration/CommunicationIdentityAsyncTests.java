// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.communication.common.CommunicationUser;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;

public class CommunicationIdentityAsyncTests extends CommunicationIdentityClientTestBase {

    private CommunicationIdentityAsyncClient asyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        asyncClient = getCommunicationIdentityClient().buildAsyncClient();
    }

    @Test
    public void createUser() {
        Mono<CommunicationUser> response = asyncClient.createUser();
        String id = response.block().getId();
        assertNotNull(id);
        assertFalse(id.isEmpty());
    }

    @Test
    public void createUserWithResponse() {
        Response<CommunicationUser> response = asyncClient.createUserWithResponse().block();
        CommunicationUser communicationUser = response.getValue();
        String id = communicationUser.getId();
        int statusCode = response.getStatusCode();

        assertNotNull(id);
        assertFalse(id.isEmpty());
        assertEquals(200, statusCode, "Expect status code to be 200");
    }


    @Test
    public void createUserWithContext() {
        Response<CommunicationUser> response = asyncClient.createUser(Context.NONE).block();
        CommunicationUser communicationUser = response.getValue();
        String id = communicationUser.getId();
        int statusCode = response.getStatusCode();

        assertNotNull(id);
        assertFalse(id.isEmpty());
        assertEquals(200, statusCode, "Expect status code to be 200");
    }

    @Test
    public void deleteUser() {
        CommunicationUser communicationUser = asyncClient.createUser().block();
        asyncClient.deleteUser(communicationUser).block();
        assertTrue(true);
    }

    @Test
    public void deleteUserWithResponse() {
        Mono<CommunicationUser> createUserReponse = asyncClient.createUser();
        CommunicationUser communicationUser = createUserReponse.block();
        Response<Void> response = asyncClient.deleteUserWithResponse(communicationUser).block();
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @Test
    public void revokeToken() {
        Mono<CommunicationUser> createUserReponse = asyncClient.createUser();
        CommunicationUser communicationUser = createUserReponse.block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken communicationUserWithToken = asyncClient.issueToken(communicationUser, scopes).block();
        asyncClient.revokeTokens(communicationUserWithToken.getUser(), null).block();
        assertTrue(true);
    }

    @Test
    public void revokeTokenWithResponse() {
        Mono<CommunicationUser> createUserReponse = asyncClient.createUser();
        CommunicationUser communicationUser = createUserReponse.block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken communicationUserWithToken = asyncClient.issueToken(communicationUser, scopes).block();
        Response<Void> response = asyncClient.revokeTokensWithResponse(communicationUserWithToken.getUser(), null).block();
        assertEquals(204, response.getStatusCode(), "Expect status code to be 204");
    }

    @Test
    public void issueToken() {
        CommunicationUser communicationUser = asyncClient.createUser().block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken userToken = asyncClient.issueToken(communicationUser, scopes).block();

        assertNotNull(userToken.getToken());
        assertFalse(userToken.getToken().isEmpty());

        assertNotNull(userToken.getExpiresOn());
        assertFalse(userToken.getExpiresOn().toString().isEmpty());

        assertNotNull(userToken.getUser());
    }

    @Test
    public void issueTokenWithResponse() {
        CommunicationUser communicationUser = asyncClient.createUser().block();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        Response<CommunicationUserToken> response = asyncClient.issueTokenWithResponse(communicationUser, scopes).block();
        CommunicationUserToken userToken = response.getValue();

        assertNotNull(userToken.getToken());
        assertFalse(userToken.getToken().isEmpty());

        assertNotNull(userToken.getExpiresOn());
        assertFalse(userToken.getExpiresOn().toString().isEmpty());

        assertNotNull(userToken.getUser());
        assertEquals(200, response.getStatusCode(), "Expect response status code to be 200");
    }
}
