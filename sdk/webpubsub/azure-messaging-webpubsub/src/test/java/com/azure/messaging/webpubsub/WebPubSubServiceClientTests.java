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
    private WebPubSubAsyncServiceClient asyncClient;

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

        this.asyncClient = webPubSubClientBuilder
            .buildAsyncClient();
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
    public void testSendToConnectionJson() {
        assertResponse(client.sendToConnectionWithResponse("test_connection", "{\"data\": true}", null, Context.NONE),
            202);
    }

    @Test
    public void testSendToAllJson() {
        assertResponse(client.sendToAllWithResponse("{\"boolvalue\": true}", null, null, Context.NONE), 202);
        assertResponse(client.sendToAllWithResponse("{\"stringvalue\": \"testingwebpubsub\"}", null, null,
            Context.NONE), 202);
        assertResponse(client.sendToAllWithResponse("{\"intvalue\": 25}", null, null, Context.NONE), 202);
        assertResponse(client.sendToAllWithResponse("{\"floatvalue\": 55.4}", null, null, Context.NONE), 202);
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

        Response<Void> removeUserResponse = javaGroup.removeUserWithResponse("testRemoveNonExistentUserFromGroup", Context.NONE);
        assertEquals(200, removeUserResponse.getStatusCode());
    }

    @Test
    public void testSendMessageToGroup() {
        WebPubSubGroup javaGroup = client.getGroup("java");
        Response<Void> sendResponse = javaGroup.sendToAllWithResponse("Hello world!", WebPubSubContentType.TEXT_PLAIN,
            Collections.emptyList(), Context.NONE);
        assertEquals(202, sendResponse.getStatusCode());
    }
}
