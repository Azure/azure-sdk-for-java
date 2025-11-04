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
import com.azure.messaging.webpubsub.models.*;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import com.azure.core.http.rest.PagedFlux;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.azure.messaging.webpubsub.TestUtils.buildAsyncAssertingClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class WebPubSubServiceAsyncClientTests extends TestProxyTestBase {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String USER_ID = "test_user";
    private static final RequestOptions REQUEST_OPTIONS_TEXT = new RequestOptions().addRequestCallback(request -> {
        request.setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain");
    });
    private static final RequestOptions REQUEST_OPTIONS_STREAM = new RequestOptions().addRequestCallback(request -> {
        request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
    });

    private static final BinaryData MESSAGE = BinaryData.fromString("Hello World - Broadcast test!");

    private WebPubSubServiceAsyncClient client;

    @Override
    protected void beforeTest() {
        WebPubSubServiceClientBuilder builder
            = new WebPubSubServiceClientBuilder().connectionString(TestUtils.getConnectionString())
                .retryOptions(TestUtils.getRetryOptions())
                .hub(TestUtils.HUB_NAME);

        switch (getTestMode()) {
            case LIVE:
                builder.httpClient(HttpClient.createDefault());
                break;

            case RECORD:
                builder.httpClient(HttpClient.createDefault()).addPolicy(interceptorManager.getRecordPolicy());
                break;

            case PLAYBACK:
                builder.httpClient(interceptorManager.getPlaybackClient());
                break;

            default:
                throw new IllegalStateException("Unknown test mode. " + getTestMode());
        }

        this.client = builder.buildAsyncClient();
    }

    private static void assertResponse(Mono<Response<Void>> operation) {
        StepVerifier.create(operation).assertNext(response -> {
            assertNotNull(response);
            assertEquals(202, response.getStatusCode());
        }).expectComplete().verify(TIMEOUT);
    }

    @Test
    public void testBroadcastString() {
        assertResponse(client.sendToAllWithResponse(MESSAGE, REQUEST_OPTIONS_TEXT));
    }

    @Test
    public void testBroadcastStringWithContentType() {
        String message = "Hello World - Broadcast test!";
        assertResponse(client.sendToAllWithResponse(BinaryData.fromString(message), WebPubSubContentType.TEXT_PLAIN,
            message.length(), REQUEST_OPTIONS_TEXT));
    }

    @Test
    public void testBroadcastStringWithFilter() {
        RequestOptions requestOptions = new RequestOptions().setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain")
            .addQueryParam("filter", "userId ne 'user1'");

        assertResponse(client.sendToAllWithResponse(MESSAGE, requestOptions));
    }

    @Test
    public void testBroadcastBytes() {
        byte[] bytes = "Hello World - Broadcast test!".getBytes();

        assertResponse(client.sendToAllWithResponse(BinaryData.fromBytes(bytes), REQUEST_OPTIONS_STREAM));
    }

    @Test
    public void testSendToUserString() throws IOException {
        assertResponse(client.sendToUserWithResponse(USER_ID, MESSAGE, REQUEST_OPTIONS_TEXT));

        assertResponse(client.sendToUserWithResponse(USER_ID, MESSAGE, WebPubSubContentType.TEXT_PLAIN,
            MESSAGE.getLength(), null));

        try (ByteArrayInputStream messageStream = new ByteArrayInputStream(MESSAGE.toBytes())) {
            assertResponse(client.sendToUserWithResponse(USER_ID, BinaryData.fromStream(messageStream),
                WebPubSubContentType.APPLICATION_OCTET_STREAM, MESSAGE.getLength(), null));
        }
    }

    @Test
    public void testSendToUserStringWithFilter() throws IOException {
        RequestOptions requestOptions = new RequestOptions().addQueryParam("filter", "userId ne 'user1'");

        assertResponse(client.sendToUserWithResponse(USER_ID, MESSAGE, REQUEST_OPTIONS_TEXT));

        assertResponse(client.sendToUserWithResponse(USER_ID, MESSAGE, WebPubSubContentType.TEXT_PLAIN,
            MESSAGE.getLength(), null));

        try (ByteArrayInputStream messageStream = new ByteArrayInputStream(MESSAGE.toBytes())) {
            assertResponse(client.sendToUserWithResponse(USER_ID, BinaryData.fromStream(messageStream),
                WebPubSubContentType.APPLICATION_OCTET_STREAM, MESSAGE.getLength(), requestOptions));
        }
    }

    @Test
    public void testSendToUserBytes() {
        final BinaryData binaryData = BinaryData.fromBytes("Hello World!".getBytes(StandardCharsets.UTF_8));

        assertResponse(client.sendToUserWithResponse(USER_ID, binaryData, REQUEST_OPTIONS_STREAM));
    }

    @Test
    public void testSendToConnectionString() {
        assertResponse(client.sendToConnectionWithResponse("test_connection", MESSAGE, REQUEST_OPTIONS_TEXT));
    }

    @Test
    public void testSendToConnectionStringWithFilter() {
        final RequestOptions filter = new RequestOptions().setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain")
            .addQueryParam("filter", "userId ne 'user1'");

        assertResponse(client.sendToConnectionWithResponse("test_connection", MESSAGE, filter));
    }

    @Test
    public void testSendToConnectionBytes() {
        final BinaryData binaryData = BinaryData.fromBytes("Hello World!".getBytes(StandardCharsets.UTF_8));

        assertResponse(client.sendToConnectionWithResponse("test_connection", binaryData, REQUEST_OPTIONS_STREAM));
    }

    @Test
    public void testSendToConnectionJson() {
        RequestOptions requestOptions = new RequestOptions()
            .addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json"));

        assertResponse(client.sendToConnectionWithResponse("test_connection", BinaryData.fromString("{\"data\": true}"),
            requestOptions));
    }

    @Test
    public void testSendToAllJson() {
        RequestOptions requestOptions = new RequestOptions()
            .addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json"));

        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"boolvalue\": true}"), requestOptions));

        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"stringvalue\": \"testingwebpubsub\"}"),
            requestOptions));

        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"intvalue\": 25}"), requestOptions));

        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"floatvalue\": 55.4}"), requestOptions));
    }

    @Test
    public void testRemoveNonExistentUserFromHub() {
        StepVerifier
            .create(
                client.removeUserFromAllGroupsWithResponse("testRemoveNonExistentUserFromHub", new RequestOptions()))
            .assertNext(response -> {
                assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    public void testRemoveConnectionFromAllGroup() {
        StepVerifier.create(client.removeConnectionFromAllGroupsWithResponse("test_connection", new RequestOptions()))
            .assertNext(response -> {
                assertEquals(204, response.getStatusCode());
            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    @LiveOnly
    public void testGetAuthenticationToken() {
        StepVerifier.create(client.getClientAccessToken(new GetClientAccessTokenOptions())).assertNext(token -> {
            Assertions.assertNotNull(token);
            Assertions.assertNotNull(token.getToken());
            Assertions.assertNotNull(token.getUrl());

            assertTrue(token.getUrl().startsWith("wss://"));
            assertTrue(token.getUrl().contains(".webpubsub.azure.com/client/hubs/"));

            String authToken = token.getToken();
            JWT jwt;
            try {
                jwt = JWTParser.parse(authToken);
            } catch (ParseException e) {
                fail("Unable to parse auth token: " + authToken + " exception: ", e);
                return;
            }

            JWTClaimsSet claimsSet;
            try {
                claimsSet = jwt.getJWTClaimsSet();
            } catch (ParseException e) {
                fail("Unable to parse claims: " + authToken + " exception: ", e);
                return;
            }

            assertNotNull(claimsSet);
            assertNotNull(claimsSet.getAudience());
            assertFalse(claimsSet.getAudience().isEmpty());

            String aud = claimsSet.getAudience().iterator().next();
            assertTrue(aud.contains(".webpubsub.azure.com/client/hubs/"));
        }).expectComplete().verify(TIMEOUT);
    }

    @Test
    @LiveOnly
    public void testGetMqttAuthenticationToken() {
        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions();
        options.setWebPubSubClientProtocol(WebPubSubClientProtocol.MQTT);
        StepVerifier.create(client.getClientAccessToken(options)).assertNext(token -> {
            Assertions.assertNotNull(token);
            Assertions.assertNotNull(token.getToken());
            Assertions.assertNotNull(token.getUrl());

            assertTrue(token.getUrl().startsWith("wss://"));
            assertTrue(token.getUrl().contains(".webpubsub.azure.com/clients/mqtt/hubs/"));

            String authToken = token.getToken();
            JWT jwt;
            try {
                jwt = JWTParser.parse(authToken);
            } catch (ParseException e) {
                fail("Unable to parse auth token: " + authToken + " exception: ", e);
                return;
            }

            JWTClaimsSet claimsSet;
            try {
                claimsSet = jwt.getJWTClaimsSet();
            } catch (ParseException e) {
                fail("Unable to parse claims: " + authToken + " exception: ", e);
                return;
            }

            assertNotNull(claimsSet);
            assertNotNull(claimsSet.getAudience());
            assertFalse(claimsSet.getAudience().isEmpty());

            String aud = claimsSet.getAudience().iterator().next();
            assertTrue(aud.contains(".webpubsub.azure.com/clients/mqtt/hubs/"));
        }).expectComplete().verify(TIMEOUT);
    }

    @Test
    @LiveOnly
    public void testGetSocketIOAuthenticationToken() {
        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions();
        options.setWebPubSubClientProtocol(WebPubSubClientProtocol.SOCKET_IO);
        StepVerifier.create(client.getClientAccessToken(options)).assertNext(token -> {
            Assertions.assertNotNull(token);
            Assertions.assertNotNull(token.getToken());
            Assertions.assertNotNull(token.getUrl());

            assertTrue(token.getUrl().startsWith("wss://"));
            assertTrue(token.getUrl().contains(".webpubsub.azure.com/clients/socketio/hubs/"));

            String authToken = token.getToken();
            JWT jwt;
            try {
                jwt = JWTParser.parse(authToken);
            } catch (ParseException e) {
                fail("Unable to parse auth token: " + authToken + " exception: ", e);
                return;
            }

            JWTClaimsSet claimsSet;
            try {
                claimsSet = jwt.getJWTClaimsSet();
            } catch (ParseException e) {
                fail("Unable to parse claims: " + authToken + " exception: ", e);
                return;
            }

            assertNotNull(claimsSet);
            assertNotNull(claimsSet.getAudience());
            assertFalse(claimsSet.getAudience().isEmpty());

            String aud = claimsSet.getAudience().iterator().next();
            assertTrue(aud.contains(".webpubsub.azure.com/clients/socketio/hubs/"));
        }).expectComplete().verify(TIMEOUT);
    }

    @Test
    @LiveOnly
    public void testGetSocketIOAuthenticationTokenAAD() {
        WebPubSubServiceClientBuilder aadClientBuilder
            = new WebPubSubServiceClientBuilder().endpoint(TestUtils.getSocketIOEndpoint())
                .httpClient(HttpClient.createDefault())
                .credential(TestUtils.getIdentityTestCredential(interceptorManager))
                .hub(TestUtils.HUB_NAME);
        WebPubSubServiceAsyncClient aadClient = aadClientBuilder.buildAsyncClient();
        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions();
        options.setWebPubSubClientProtocol(WebPubSubClientProtocol.SOCKET_IO);
        StepVerifier.create(aadClient.getClientAccessToken(options)).assertNext(token -> {
            Assertions.assertNotNull(token);
            Assertions.assertNotNull(token.getToken());
            Assertions.assertNotNull(token.getUrl());

            assertTrue(token.getUrl().startsWith("wss://"));
            assertTrue(token.getUrl().contains(".webpubsub.azure.com/clients/socketio/hubs/"));

            String authToken = token.getToken();
            JWT jwt;
            try {
                jwt = JWTParser.parse(authToken);
            } catch (ParseException e) {
                fail("Unable to parse auth token: " + authToken + " exception: ", e);
                return;
            }

            JWTClaimsSet claimsSet;
            try {
                claimsSet = jwt.getJWTClaimsSet();
            } catch (ParseException e) {
                fail("Unable to parse claims: " + authToken + " exception: ", e);
                return;
            }

            assertNotNull(claimsSet);
            assertNotNull(claimsSet.getAudience());
            assertFalse(claimsSet.getAudience().isEmpty());

            String aud = claimsSet.getAudience().iterator().next();
            assertTrue(aud.contains(".webpubsub.azure.com/clients/socketio/hubs/"));
        }).expectComplete().verify(TIMEOUT);
    }

    @Test
    public void testRemoveNonExistentUserFromGroup() {
        StepVerifier.create(
            client.removeUserFromGroupWithResponse("java", "testRemoveNonExistentUserFromGroup", new RequestOptions()),
            204);
    }

    @Test
    public void testSendMessageToGroup() {
        StepVerifier.create(
            client.sendToGroupWithResponse("java", BinaryData.fromString("Hello World!"), new RequestOptions()
                .addRequestCallback(request -> request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/plain"))),
            202);
    }

    @Test
    public void testSendMessageToGroupWithContentType() {
        String message = "Hello World!";
        StepVerifier.create(client.sendToGroupWithResponse("java", BinaryData.fromString(message),
            WebPubSubContentType.TEXT_PLAIN, message.length(),
            new RequestOptions()
                .addRequestCallback(request -> request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/plain"))),
            202);
    }

    @Test
    public void testAddConnectionsToGroup() {
        List<String> groupList = Arrays.asList("group1", "group2");
        String filter = "userId eq 'user 1'";
        // Expect no error
        StepVerifier.create(client.addConnectionsToGroups(groupList, filter)).expectComplete().verify(TIMEOUT);
    }

    @Test
    public void testAddConnectionsToGroupsThrowErrorWhenGroupsToAddIsNull() {
        String filter = "userId eq 'user 1'";
        StepVerifier.create(client.addConnectionsToGroups(null, filter)).expectError(HttpResponseException.class);
    }

    @Test
    public void testAadCredential() {
        WebPubSubServiceClientBuilder builder = new WebPubSubServiceClientBuilder().endpoint(TestUtils.getEndpoint())
            .httpClient(HttpClient.createDefault())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .hub("test");

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(buildAsyncAssertingClient(interceptorManager.getPlaybackClient()))
                .connectionString(TestUtils.getConnectionString());
        } else {
            builder.credential(TestUtils.getIdentityTestCredential(interceptorManager));
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        final WebPubSubServiceAsyncClient asyncClient = builder.buildAsyncClient();

        assertResponse(asyncClient.sendToUserWithResponse(USER_ID, MESSAGE, REQUEST_OPTIONS_TEXT));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "(LIVE|live|Live|RECORD|record|Record)")
    @DisabledIfSystemProperty(named = "AZURE_TEST_MODE", matches = "(LIVE|live|Live|RECORD|record|Record)")
    public void testCheckPermission() {
        // This test requires a connectionId with SEND_TO_GROUP permission. Fails consistently in LIVE mode and
        // needs to be fixed. Github issue: https://github.com/Azure/azure-sdk-for-java/issues/41343
        RequestOptions requestOptions = new RequestOptions().addQueryParam("targetName", "java");

        StepVerifier
            .create(client.checkPermissionWithResponse(WebPubSubPermission.SEND_TO_GROUP,
                "M0UuAb14DkmvBp4hZsct8A-DPgpgK02", requestOptions))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertTrue(response.getValue());
            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    @ParameterizedTest
    @CsvSource({
        "6, 6, -1, 6, 1, group2_uniqueA",
        "6, 3, -1, 3, 1, group2_uniqueB",
        "6, -1, 2, 6, 3, group2_uniqueC",
        "6, 5, 2, 5, 3, group2_uniqueD" })
    public void testListConnectionsInGroupAsync(int totalConnectionCount, int top, int maxPageSize,
        int expectedTotalCount, int expectedPageCount, String groupName) {
        final List<WebSocketTestClient> clients = new java.util.ArrayList<>();
        if (getTestMode() != TestMode.PLAYBACK) {
            StepVerifier
                .create(
                    client.getClientAccessToken(new GetClientAccessTokenOptions().setGroups(Arrays.asList(groupName))))
                .assertNext(token -> {
                    for (int i = 0; i < totalConnectionCount; i++) {
                        WebSocketTestClient wsClient = new WebSocketTestClient();
                        wsClient.connect(token.getUrl());
                        clients.add(wsClient);
                    }
                })
                .expectComplete()
                .verify(TIMEOUT);
        }

        int[] actualPageCount = { 0 };
        int[] actualConnectionCount = { 0 };

        RequestOptions options = new RequestOptions();
        if (maxPageSize != -1) {
            options.addQueryParam("maxpagesize", String.valueOf(maxPageSize));
        }
        if (top != -1) {
            options.addQueryParam("top", String.valueOf(top));
        }

        PagedFlux<WebPubSubGroupConnection> pagedFlux = client.listConnectionsInGroup(groupName, options);
        StepVerifier.create(pagedFlux.byPage()).thenConsumeWhile(page -> {
            actualPageCount[0]++;
            actualConnectionCount[0] += page.getValue().size();
            return true;
        }).expectComplete().verify(TIMEOUT);

        assertEquals(expectedPageCount, actualPageCount[0]);
        assertEquals(expectedTotalCount, actualConnectionCount[0]);

        if (getTestMode() != TestMode.PLAYBACK) {
            for (WebSocketTestClient wsClient : clients) {
                wsClient.close();
            }
        }
    }
}
