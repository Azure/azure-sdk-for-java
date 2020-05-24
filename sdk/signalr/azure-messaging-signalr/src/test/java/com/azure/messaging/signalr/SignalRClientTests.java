// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class SignalRClientTests {

    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get("SIGNALR_CS");
    private static final String ENDPOINT = Configuration.getGlobalConfiguration().get("SIGNALR_ENDPOINT");

    private SignalRClient client;
    private SignalRGroupClient groupClient;

    private SignalRAsyncClient asyncClient;
    private SignalRGroupAsyncClient asyncGroupClient;

    @BeforeEach
    public void setup() {
        this.client = new SignalRClientBuilder()
            .connectionString(CONNECTION_STRING)
            .hub("test")
//            .endpoint(endpoint)
            .buildClient();
        this.groupClient = client.getGroupClient("test_group");

        this.asyncClient = new SignalRClientBuilder()
          .connectionString(CONNECTION_STRING)
          .hub("test")
          .buildAsyncClient();
        this.asyncGroupClient = asyncClient.getGroupAsyncClient("test_async_group");
    }

    private void assertResponse(Response<?> response, int expectedCode) {
        assertNotNull(response);
        assertEquals(202, response.getStatusCode());
    }

    /*****************************************************************************************************************
     * Sync Tests - SignalRClient
     ****************************************************************************************************************/

    @Test
    public void assertClientNotNull() {
        assertNotNull(client);
    }

    @Test
    public void testStatus() {
        SignalRHubStatus status = client.getStatus();
        assertNotNull(status);
        assertTrue(status.isAvailable());
    }

    @Test
    public void testBroadcastString() {
        assertResponse(client.sendToAllWithResponse(
            "Hello World - Broadcast test!",
            Collections.emptyList(),
            Context.NONE),
            202);
    }

    @Test
    public void testBroadcastBytes() {
        byte[] bytes = "Hello World - Broadcast test!".getBytes();
        assertResponse(client.sendToAllWithResponse(bytes,
            Collections.emptyList(),
            Context.NONE),
            202);
    }

    @Test
    public void testSendToUserString() {
        assertResponse(client.sendToUserWithResponse("test_user", "Hello World!", Context.NONE), 202);
    }

    @Test
    public void testSendToUserBytes() {
        assertResponse(client.sendToUserWithResponse("test_user", "Hello World!".getBytes(), Context.NONE), 202);
    }

    @Test
    public void testSendToConnectionString() {
        assertResponse(client.sendToConnectionWithResponse("test_connection", "Hello World!", Context.NONE), 202);
    }

    @Test
    public void testSendToConnectionBytes() {
        assertResponse(client.sendToConnectionWithResponse("test_connection", "Hello World!".getBytes(), Context.NONE), 202);
    }

    @Test
    public void testRemoveNonExistentUserFromHub() {
        // TODO (jogiles) can we determine if this user exists anywhere in the current hub?
        Response<Void> removeUserResponse =
            client.removeUserFromAllGroupsWithResponse("testRemoveNonExistentUserFromHub", Context.NONE);
        assertEquals(200, removeUserResponse.getStatusCode());
    }

    /*****************************************************************************************************************
     * Sync Tests - SignalRGroupClient
     ****************************************************************************************************************/

    @Test
    public void testRemoveNonExistentUserFromGroup() {
        SignalRGroupClient javaGroup = client.getGroupClient("java");

        assertFalse(javaGroup.userExists("testRemoveNonExistentUserFromGroup"));
        Response<Void> removeUserResponse = javaGroup.removeUserWithResponse("testRemoveNonExistentUserFromGroup", Context.NONE);
        assertEquals(202, removeUserResponse.getStatusCode());
        assertFalse(javaGroup.userExists("testRemoveNonExistentUserFromGroup"));
    }

    @Test
    public void testAddAndRemoveUserToGroup() {
//        SignalRClient simpleChat = client.getHubClient("test");
        SignalRGroupClient javaGroup = client.getGroupClient("java");

        // TODO (jogiles) don't block
        assertFalse(javaGroup.userExists("Jonathan"));
        Response<Void> addUserResponse = javaGroup.addUserWithResponse("Jonathan", null, Context.NONE);
        assertEquals(202, addUserResponse.getStatusCode());
        assertTrue(javaGroup.userExists("Jonathan"));
        Response<Void> removeUserResponse = javaGroup.removeUserWithResponse("Jonathan", Context.NONE);
        assertEquals(202, removeUserResponse.getStatusCode());
        assertFalse(javaGroup.userExists("Jonathan"));
    }


    /*****************************************************************************************************************
     * Async Tests
     ****************************************************************************************************************/

//    @Test
//    public void testAsyncAddAndRemoveUserToGroup() {
//        SignalRHubAsyncClient simpleChat = asyncClient.getHubClient("simplechat");
//        SignalRGroupAsyncClient javaGroup = simpleChat.getGroupClient("java");
//
//        // TODO (jogiles) don't block
//        assertFalse(javaGroup.doesUserExist("Jonathan").block().getValue());
//        javaGroup.addUser("Jonathan").block();
//        assertTrue(javaGroup.doesUserExist("Jonathan").block().getValue());
//        javaGroup.removeUser("Jonathan").block();
//        assertFalse(javaGroup.doesUserExist("Jonathan").block().getValue());
//    }
}
