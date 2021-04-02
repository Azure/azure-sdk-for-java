// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class WebPubSubServiceClientTests extends TestBase {

    private static final String DEFAULT_CONNECTION_STRING =
        "Endpoint=https://example.com;AccessKey=dummykey;Version=1.0;";
    private static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("WEB_PUB_SUB_CS", DEFAULT_CONNECTION_STRING);

    private WebPubSubServiceClient client;
    private WebPubSubGroup groupClient;

    private WebPubSubAsyncServiceClient asyncClient;
    private WebPubSubAsyncGroup asyncGroupClient;

    @BeforeEach
    public void setup() {
        WebPubSubClientBuilder webPubSubClientBuilder = new WebPubSubClientBuilder()
            .connectionString(CONNECTION_STRING)
            .httpClient(HttpClient.createDefault())
            .hub("test");

        if (getTestMode() == TestMode.PLAYBACK) {
            webPubSubClientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            webPubSubClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        this.client = webPubSubClientBuilder
            .buildClient();
        this.groupClient = client.getGroup("test_group");

        this.asyncClient = webPubSubClientBuilder
            .buildAsyncClient();
        this.asyncGroupClient = asyncClient.getAsyncGroup("test_async_group");
    }

    private void assertResponse(Response<?> response, int expectedCode) {
        assertNotNull(response);
        assertEquals(expectedCode, response.getStatusCode());
    }

    /*****************************************************************************************************************
     * Sync Tests - WebPubSubServiceClient
     ****************************************************************************************************************/

    @Test
    public void assertClientNotNull() {
        assertNotNull(client);
    }

    @Test
    public void testBroadcastString() {
        assertResponse(client.sendToAllWithResponse(
            "Hello World - Broadcast test!",
            WebPubSubContentType.TEXT_PLAIN,
            Collections.emptyList(),
            Context.NONE),
            202);
    }

    @Test
    public void testBroadcastBytes() {
        byte[] bytes = "Hello World - Broadcast test!".getBytes();
        assertResponse(client.sendToAllWithResponse(bytes,
            WebPubSubContentType.TEXT_PLAIN,
            Collections.emptyList(),
            Context.NONE),
            202);
    }

    @Test
    public void testSendToUserString() {
        assertResponse(client.sendToUserWithResponse("test_user", "Hello World!", WebPubSubContentType.TEXT_PLAIN, Context.NONE), 202);
    }

    @Test
    public void testSendToUserBytes() {
        assertResponse(client.sendToUserWithResponse("test_user", "Hello World!".getBytes(), WebPubSubContentType.TEXT_PLAIN, Context.NONE), 202);
    }

    @Test
    public void testSendToConnectionString() {
        assertResponse(client.sendToConnectionWithResponse("test_connection", "Hello World!", WebPubSubContentType.TEXT_PLAIN, Context.NONE), 202);
    }

    @Test
    public void testSendToConnectionBytes() {
        assertResponse(client.sendToConnectionWithResponse("test_connection", "Hello World!".getBytes(), WebPubSubContentType.TEXT_PLAIN, Context.NONE), 202);
    }

    @Test
    public void testRemoveNonExistentUserFromHub() {
        // TODO (jogiles) can we determine if this user exists anywhere in the current hub?
        Response<Void> removeUserResponse =
            client.removeUserFromAllGroupsWithResponse("testRemoveNonExistentUserFromHub", Context.NONE);
        assertEquals(200, removeUserResponse.getStatusCode());
    }

    /*****************************************************************************************************************
     * Sync Tests - WebPubSubGroup
     ****************************************************************************************************************/

    @Test
    public void testRemoveNonExistentUserFromGroup() {
        WebPubSubGroup javaGroup = client.getGroup("java");

        assertFalse(javaGroup.checkUserExists("testRemoveNonExistentUserFromGroup"));
        Response<Void> removeUserResponse = javaGroup.removeUserWithResponse("testRemoveNonExistentUserFromGroup", Context.NONE);
        assertEquals(200, removeUserResponse.getStatusCode());
        assertFalse(javaGroup.checkUserExists("testRemoveNonExistentUserFromGroup"));
    }

    @Test
    public void testAddAndRemoveUserToGroup() {
//        WebPubSubServiceClient simpleChat = client.getHubClient("test");
        WebPubSubGroup javaGroup = client.getGroup("java");

        // TODO (jogiles) don't block
        if (javaGroup.checkUserExists("Jonathan")) {
            javaGroup.removeUser("Jonathan");
        }
        assertFalse(javaGroup.checkUserExists("Jonathan"));
        Response<Void> addUserResponse = javaGroup.addUserWithResponse("Jonathan", Context.NONE);
        assertEquals(200, addUserResponse.getStatusCode());
        assertTrue(javaGroup.checkUserExists("Jonathan"));
        Response<Void> removeUserResponse = javaGroup.removeUserWithResponse("Jonathan", Context.NONE);
        assertEquals(200, removeUserResponse.getStatusCode());
        assertFalse(javaGroup.checkUserExists("Jonathan"));
    }


    /*****************************************************************************************************************
     * Async Tests
     ****************************************************************************************************************/

//    @Test
//    public void testAsyncAddAndRemoveUserToGroup() {
//        SignalRHubAsyncClient simpleChat = asyncClient.getHubClient("simplechat");
//        WebPubSubAsyncGroup javaGroup = simpleChat.getGroupClient("java");
//
//        // TODO (jogiles) don't block
//        assertFalse(javaGroup.doesUserExist("Jonathan").block().getValue());
//        javaGroup.addUser("Jonathan").block();
//        assertTrue(javaGroup.doesUserExist("Jonathan").block().getValue());
//        javaGroup.removeUser("Jonathan").block();
//        assertFalse(javaGroup.doesUserExist("Jonathan").block().getValue());
//    }
}
