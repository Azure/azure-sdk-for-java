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
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.azure.messaging.webpubsub.models.WebPubSubPermission;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
    private static final RequestOptions REQUEST_OPTIONS_TEXT = new RequestOptions()
        .addRequestCallback(request -> {
            request.setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain");
        });
    private static final RequestOptions REQUEST_OPTIONS_STREAM = new RequestOptions()
        .addRequestCallback(request -> {
            request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        });

    private static final BinaryData MESSAGE = BinaryData.fromString("Hello World - Broadcast test!");

    private WebPubSubServiceAsyncClient client;

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

        this.client = builder.buildAsyncClient();
    }

    private static void assertResponse(Mono<Response<Void>> operation) {
        StepVerifier.create(operation)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(202, response.getStatusCode());
            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    public void testBroadcastString() {
        assertResponse(client.sendToAllWithResponse(MESSAGE, REQUEST_OPTIONS_TEXT));
    }

    @Test
    public void testBroadcastStringWithContentType() {
        String message = "Hello World - Broadcast test!";
        assertResponse(client.sendToAllWithResponse(
            BinaryData.fromString(message),
            WebPubSubContentType.TEXT_PLAIN,
            message.length(),
            REQUEST_OPTIONS_TEXT));
    }

    @Test
    public void testBroadcastStringWithFilter() {
        RequestOptions requestOptions = new RequestOptions()
            .setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain")
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
        RequestOptions requestOptions = new RequestOptions()
            .addQueryParam("filter", "userId ne 'user1'");

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
        assertResponse(client.sendToConnectionWithResponse("test_connection", MESSAGE,
            REQUEST_OPTIONS_TEXT));
    }

    @Test
    public void testSendToConnectionStringWithFilter() {
        final RequestOptions filter = new RequestOptions()
            .setHeader(HttpHeaderName.CONTENT_TYPE, "text/plain")
            .addQueryParam("filter", "userId ne 'user1'");

        assertResponse(client.sendToConnectionWithResponse("test_connection", MESSAGE, filter));
    }

    @Test
    public void testSendToConnectionBytes() {
        final BinaryData binaryData = BinaryData.fromBytes("Hello World!".getBytes(StandardCharsets.UTF_8));

        assertResponse(client.sendToConnectionWithResponse("test_connection", binaryData,
            REQUEST_OPTIONS_STREAM));
    }

    @Test
    public void testSendToConnectionJson() {
        RequestOptions requestOptions = new RequestOptions()
            .addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json"));

        assertResponse(client.sendToConnectionWithResponse("test_connection",
            BinaryData.fromString("{\"data\": true}"), requestOptions));
    }

    @Test
    public void testSendToAllJson() {
        RequestOptions requestOptions = new RequestOptions()
            .addRequestCallback(request -> request.setHeader(HttpHeaderName.CONTENT_TYPE, "application/json"));


        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"boolvalue\": true}"),
            requestOptions));

        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"stringvalue\": \"testingwebpubsub\"}"),
            requestOptions));

        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"intvalue\": 25}"),
            requestOptions));

        assertResponse(client.sendToAllWithResponse(BinaryData.fromString("{\"floatvalue\": 55.4}"),
            requestOptions));
    }

    @Test
    public void testRemoveNonExistentUserFromHub() {
        StepVerifier.create(client.removeUserFromAllGroupsWithResponse("testRemoveNonExistentUserFromHub", new RequestOptions()))
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
        StepVerifier.create(client.getClientAccessToken(new GetClientAccessTokenOptions()))
            .assertNext(token -> {
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
            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    @LiveOnly
    public void testGetMqttAuthenticationToken() {
        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions();
        options.setWebPubSubClientAccess(WebPubSubClientProtocol.MQTT);
        StepVerifier.create(client.getClientAccessToken(options))
            .assertNext(token -> {
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
            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    public void testRemoveNonExistentUserFromGroup() {
        StepVerifier.create(client.removeUserFromGroupWithResponse("java",
            "testRemoveNonExistentUserFromGroup", new RequestOptions()), 204);
    }

    @Test
    public void testSendMessageToGroup() {
        StepVerifier.create(client.sendToGroupWithResponse("java",
            BinaryData.fromString("Hello World!"),
            new RequestOptions().addRequestCallback(request -> request.getHeaders()
                .set("Content-Type", "text/plain"))), 202);
    }

    @Test
    public void testSendMessageToGroupWithContentType() {
        String message = "Hello World!";
        StepVerifier.create(client.sendToGroupWithResponse("java",
            BinaryData.fromString(message),
            WebPubSubContentType.TEXT_PLAIN,
            message.length(),
            new RequestOptions().addRequestCallback(request -> request.getHeaders()
                .set("Content-Type", "text/plain"))), 202);
    }

    @Test
    public void testAddConnectionsToGroup() {
        List<String> groupList = Arrays.asList("group1", "group2");
        String filter = "userId eq 'user 1'";
        // Expect no error
        StepVerifier
            .create(client.addConnectionsToGroups(TestUtils.HUB_NAME, groupList, filter))
            .expectComplete()
            .verify(TIMEOUT);
    }

    @Test
    public void testAddConnectionsToGroupsThrowErrorWhenHubIsNull() {
        List<String> groupList = Arrays.asList("group1", "group2");
        String filter = "userId eq 'user 1'";
        StepVerifier.create(
            client.addConnectionsToGroups(null, groupList, filter)
        ).expectError(IllegalArgumentException.class);
    }

    @Test
    public void testAddConnectionsToGroupsThrowErrorWhenGroupsToAddIsNull() {
        String filter = "userId eq 'user 1'";
        StepVerifier.create(
            client.addConnectionsToGroups(TestUtils.HUB_NAME, null, filter)
        ).expectError(HttpResponseException.class);
    }

    @Test
    public void testAadCredential() {
        WebPubSubServiceClientBuilder builder = new WebPubSubServiceClientBuilder()
            .endpoint(TestUtils.getEndpoint())
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
        RequestOptions requestOptions = new RequestOptions()
            .addQueryParam("targetName", "java");

        StepVerifier.create(client.checkPermissionWithResponse(WebPubSubPermission.SEND_TO_GROUP, "M0UuAb14DkmvBp4hZsct8A-DPgpgK02",
            requestOptions))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertTrue(response.getValue());
            })
            .expectComplete()
            .verify(TIMEOUT);
    }
}
