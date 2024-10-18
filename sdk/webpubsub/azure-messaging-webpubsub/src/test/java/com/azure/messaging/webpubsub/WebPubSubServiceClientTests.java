// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.models.WebPubSubClientProtocol;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.azure.messaging.webpubsub.models.WebPubSubPermission;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class WebPubSubServiceClientTests extends TestProxyTestBase {

    private WebPubSubServiceClient client;

    @Override
    protected void beforeTest() {
        WebPubSubServiceClientBuilder builder = new WebPubSubServiceClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .retryOptions(TestUtils.getRetryOptions())
            .hub(TestUtils.HUB_NAME);

        switch (getTestMode()) {
            case LIVE:
                builder.httpClient(HttpClient.createDefault());
                break;
            case RECORD:
                builder.httpClient(HttpClient.createDefault())
                    .addPolicy(interceptorManager.getRecordPolicy());
                break;
            case PLAYBACK:
                builder.httpClient(interceptorManager.getPlaybackClient());
                break;
            default:
                throw new IllegalStateException("Unknown test mode. " + getTestMode());
        }

        this.client = builder
            .buildClient();
    }

    private static void assertResponse(Response<?> response, int expectedCode) {
        assertNotNull(response);
        assertEquals(expectedCode, response.getStatusCode());
    }

    @Test
    public void testBroadcastStringWithContentType() {
        String message = "Hello World - Broadcast test!";
        assertResponse(client.sendToAllWithResponse(
            BinaryData.fromString(message),
            WebPubSubContentType.TEXT_PLAIN,
            message.length(),
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain"))), 202);
    }

    @Test
    public void testBroadcastString() {
        assertResponse(client.sendToAllWithResponse(
            BinaryData.fromString("Hello World - Broadcast test!"),
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain"))), 202);
    }

    @Test
    public void testBroadcastStringWithFilter() {
        assertResponse(client.sendToAllWithResponse(
            BinaryData.fromString("Hello World - Broadcast test!"),
            new RequestOptions().setHeader("Content-Type", "text/plain")
                .addQueryParam("filter", "userId ne 'user1'")), 202);
    }

    @Test
    public void testBroadcastBytes() {
        byte[] bytes = "Hello World - Broadcast test!".getBytes();
        assertResponse(client.sendToAllWithResponse(
            BinaryData.fromBytes(bytes),
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/octet-stream"))), 202);
    }

    @Test
    public void testSendToUserString() {
        BinaryData message = BinaryData.fromString("Hello World!");

        assertResponse(client.sendToUserWithResponse("test_user",
            message,
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain"))), 202);

        assertResponse(client.sendToUserWithResponse("test_user",
                message, WebPubSubContentType.TEXT_PLAIN, message.getLength(),
                null),
            202);

        ByteArrayInputStream messageStream = new ByteArrayInputStream(message.toBytes());
        assertResponse(client.sendToUserWithResponse("test_user",
                BinaryData.fromStream(messageStream),
                WebPubSubContentType.APPLICATION_OCTET_STREAM, message.getLength(),
                null),
            202);
    }

    @Test
    public void testSendToUserStringWithFilter() {
        BinaryData message = BinaryData.fromString("Hello World!");

        assertResponse(client.sendToUserWithResponse("test_user",
            message,
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain"))), 202);

        assertResponse(client.sendToUserWithResponse("test_user",
                message, WebPubSubContentType.TEXT_PLAIN, message.getLength(),
                null),
            202);

        ByteArrayInputStream messageStream = new ByteArrayInputStream(message.toBytes());
        assertResponse(client.sendToUserWithResponse("test_user",
                BinaryData.fromStream(messageStream),
                WebPubSubContentType.APPLICATION_OCTET_STREAM, message.getLength(),
                new RequestOptions().addQueryParam("filter", "userId ne 'user1'")),
            202);
    }

    @Test
    public void testSendToUserBytes() {
        assertResponse(client.sendToUserWithResponse("test_user",
            BinaryData.fromBytes("Hello World!".getBytes(StandardCharsets.UTF_8)),
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/octet-stream"))), 202);
    }

    @Test
    public void testSendToConnectionString() {
        assertResponse(client.sendToConnectionWithResponse("test_connection",
            BinaryData.fromString("Hello World!"),
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain"))), 202);
    }

    @Test
    public void testSendToConnectionStringWithFilter() {
        assertResponse(client.sendToConnectionWithResponse("test_connection",
            BinaryData.fromString("Hello World!"),
            new RequestOptions().setHeader("Content-Type", "text/plain")
                .addQueryParam("filter", "userId ne 'user1'")), 202);
    }

    @Test
    public void testSendToConnectionBytes() {
        assertResponse(client.sendToConnectionWithResponse("test_connection",
            BinaryData.fromBytes("Hello World!".getBytes(StandardCharsets.UTF_8)),
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/octet-stream"))), 202);
    }

    @Test
    public void testSendToConnectionJson() {
        assertResponse(client.sendToConnectionWithResponse("test_connection",
            BinaryData.fromString("{\"data\": true}"),
            new RequestOptions()
                .addRequestCallback(request -> request.getHeaders().set("Content-Type", "application/json"))), 202);
    }

    @Test
    public void testSendToAllJson() {
        RequestOptions requestOptions = new RequestOptions().addRequestCallback(request -> request.getHeaders().set(
            "Content-Type", "application/json"));

        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"boolvalue\": true}"),
            requestOptions), 202);
        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"stringvalue\": \"testingwebpubsub\"}"),
            requestOptions), 202);

        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"intvalue\": 25}"),
            requestOptions), 202);

        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"floatvalue\": 55.4}"),
            requestOptions), 202);
    }

    @Test
    public void testRemoveNonExistentUserFromHub() {
        // TODO (jogiles) can we determine if this user exists anywhere in the current hub?
        Response<Void> removeUserResponse =
            client.removeUserFromAllGroupsWithResponse("testRemoveNonExistentUserFromHub", new RequestOptions());
        assertEquals(204, removeUserResponse.getStatusCode());
    }

    @Test
    public void testRemoveConnectionFromAllGroup() {
        Response<Void> response =
            client.removeConnectionFromAllGroupsWithResponse("test_connection", new RequestOptions());
        assertEquals(204, response.getStatusCode());
    }

    @Test
    @LiveOnly
    public void testGetAuthenticationToken() throws ParseException {
        WebPubSubClientAccessToken token = client.getClientAccessToken(new GetClientAccessTokenOptions());
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getToken());
        Assertions.assertNotNull(token.getUrl());

        Assertions.assertTrue(token.getUrl().startsWith("wss://"));
        Assertions.assertTrue(token.getUrl().contains(".webpubsub.azure.com/client/hubs/"));

        String authToken = token.getToken();
        JWT jwt = JWTParser.parse(authToken);
        JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
        Assertions.assertNotNull(claimsSet);
        Assertions.assertNotNull(claimsSet.getAudience());
        Assertions.assertFalse(claimsSet.getAudience().isEmpty());

        String aud = claimsSet.getAudience().iterator().next();
        Assertions.assertTrue(aud.contains(".webpubsub.azure.com/client/hubs/"));
    }

    @Test
    @LiveOnly
    public void testGetMqttAuthenticationToken() throws ParseException {
        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions()
            .setWebPubSubClientProtocol(WebPubSubClientProtocol.MQTT);
        WebPubSubClientAccessToken token = client.getClientAccessToken(options);

        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getToken());
        Assertions.assertNotNull(token.getUrl());

        Assertions.assertTrue(token.getUrl().startsWith("wss://"));
        Assertions.assertTrue(token.getUrl().contains(".webpubsub.azure.com/clients/mqtt/hubs/"));

        String authToken = token.getToken();
        JWT jwt = JWTParser.parse(authToken);
        JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
        Assertions.assertNotNull(claimsSet);
        Assertions.assertNotNull(claimsSet.getAudience());
        Assertions.assertFalse(claimsSet.getAudience().isEmpty());

        String aud = claimsSet.getAudience().iterator().next();
        Assertions.assertTrue(aud.contains(".webpubsub.azure.com/clients/mqtt/hubs/"));
    }

    @Test
    public void testRemoveNonExistentUserFromGroup() {
        assertResponse(client.removeUserFromGroupWithResponse("java",
            "testRemoveNonExistentUserFromGroup", new RequestOptions()), 204);
    }

    @Test
    public void testSendMessageToGroup() {
        assertResponse(client.sendToGroupWithResponse("java",
            BinaryData.fromString("Hello World!"),
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain"))), 202);
    }

    @Test
    public void testSendMessageToGroupWithContentType() {
        String message = "Hello World!";
        assertResponse(client.sendToGroupWithResponse("java",
            BinaryData.fromString(message),
            WebPubSubContentType.TEXT_PLAIN,
            message.length(),
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain"))), 202);
    }

    @Test
    public void testAddConnectionsToGroups() {
        List<String> groupList = Arrays.asList("group1", "group2");
        String filter = "userId eq 'user 1'";
        assertDoesNotThrow(() -> client.addConnectionsToGroups(groupList, filter));
    }

    @Test
    public void testAddConnectionsToGroupsThrowErrorWhenGroupsIsNull() {
        String filter = "userId eq 'user 1'";
        assertThrows(HttpResponseException.class, () -> client.addConnectionsToGroups(null, filter));
    }

    @Test
    public void testAadCredential() {
        WebPubSubServiceClientBuilder webPubSubServiceClientBuilder = new WebPubSubServiceClientBuilder()
            .endpoint(TestUtils.getEndpoint())
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .hub("test");

        if (getTestMode() == TestMode.PLAYBACK) {
            webPubSubServiceClientBuilder.httpClient(interceptorManager.getPlaybackClient())
                .connectionString(TestUtils.getConnectionString());
        } else if (getTestMode() == TestMode.RECORD) {
            webPubSubServiceClientBuilder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(TestUtils.getIdentityTestCredential(interceptorManager));
        } else if (getTestMode() == TestMode.LIVE) {
            webPubSubServiceClientBuilder.credential(TestUtils.getIdentityTestCredential(interceptorManager));
        }

        this.client = webPubSubServiceClientBuilder.buildClient();

        assertResponse(client.sendToUserWithResponse("test_user",
            BinaryData.fromString("Hello World!"),
            new RequestOptions().addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain"))), 202);
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "(LIVE|live|Live|RECORD|record|Record)")
    @DisabledIfSystemProperty(named = "AZURE_TEST_MODE", matches = "(LIVE|live|Live|RECORD|record|Record)")
    public void testCheckPermission() {

        // This test is failing consistently, needs to be fixed, disabled.
        // Github issue: https://github.com/Azure/azure-sdk-for-java/issues/41343
        RequestOptions requestOptions = new RequestOptions()
            .addQueryParam("targetName", "java");
        /*
            To run this test in LIVE mode, please obtain a connectionID of a client with SEND_TO_GROUP Permission.
            To do this, refer to https://learn.microsoft.com/en-us/azure/azure-web-pubsub/quickstarts-pubsub-among-clients
            and define the connected event callback to get the connectionID.
         */
        boolean permission = client.checkPermissionWithResponse(WebPubSubPermission.SEND_TO_GROUP, "M0UuAb14DkmvBp4hZsct8A-DPgpgK02",
            requestOptions).getValue();
        Assertions.assertTrue(permission);
    }
}
