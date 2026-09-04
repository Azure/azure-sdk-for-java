// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.messaging.webpubsub.chat.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.chat.models.WebPubSubClientAccessToken;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebPubSubChatClientAccessTokenTests {
    private static final String ENDPOINT = "https://example.webpubsub.azure.com";
    private static final String KEY = "01234567890123456789012345678901";

    @Test
    public void keyCredentialGeneratesChatTokenLocally() throws ParseException {
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
            .hub("chat")
            .credential(new AzureKeyCredential(KEY))
            .httpClient(request -> Mono.error(new AssertionError("Local token generation must not send a request.")))
            .buildClient();

        Instant before = Instant.now();
        WebPubSubClientAccessToken token
            = client.getClientAccessToken(new GetClientAccessTokenOptions().setUserId("alice"));

        assertNotNull(token.getToken());
        assertEquals("wss://example.webpubsub.azure.com/client/hubs/chat?access_token=" + token.getToken(),
            token.getUrl());
        JWTClaimsSet claims = SignedJWT.parse(token.getToken()).getJWTClaimsSet();
        assertEquals("alice", claims.getSubject());
        assertEquals(Arrays.asList("webpubsub.getGroupState", "webpubsub.setGroupState"), claims.getClaim("role"));
        assertEquals(ENDPOINT + "/client/hubs/chat", claims.getAudience().get(0));
        assertTrue(claims.getExpirationTime().toInstant().isAfter(before.plus(Duration.ofMinutes(59))));
        assertTrue(claims.getExpirationTime().toInstant().isBefore(before.plus(Duration.ofMinutes(61))));
    }

    @Test
    public void asyncKeyCredentialUsesSameTokenSemantics() throws ParseException {
        WebPubSubChatServiceAsyncClient client = new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
            .hub("chat")
            .credential(new AzureKeyCredential(KEY))
            .httpClient(request -> Mono.error(new AssertionError("Local token generation must not send a request.")))
            .buildAsyncClient();

        WebPubSubClientAccessToken token
            = client.getClientAccessToken(new GetClientAccessTokenOptions().setUserId("alice")).block();

        assertNotNull(token);
        JWTClaimsSet claims = SignedJWT.parse(token.getToken()).getJWTClaimsSet();
        assertEquals("alice", claims.getSubject());
        assertEquals(Arrays.asList("webpubsub.getGroupState", "webpubsub.setGroupState"), claims.getClaim("role"));
    }

    @Test
    public void connectionStringGeneratesTokenLocally() {
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder()
            .connectionString("Endpoint=https://example.webpubsub.azure.com;AccessKey=" + KEY + ";Port=8443")
            .hub("chat")
            .httpClient(request -> Mono.error(new AssertionError("Local token generation must not send a request.")))
            .buildClient();

        WebPubSubClientAccessToken token = client.getClientAccessToken(new GetClientAccessTokenOptions());

        assertTrue(token.getUrl().startsWith("wss://example.webpubsub.azure.com:8443/client/hubs/chat?access_token="));
    }

    @Test
    public void signingFailureReturnsNullToken() {
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
            .hub("chat")
            .credential(new AzureKeyCredential("short-key"))
            .buildClient();

        WebPubSubClientAccessToken token = client.getClientAccessToken(new GetClientAccessTokenOptions());

        assertNull(token.getToken());
        assertEquals("wss://example.webpubsub.azure.com/client/hubs/chat?access_token=null", token.getUrl());
    }

    @Test
    public void tokenCredentialCallsGenerateTokenOperation() {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        WebPubSubChatServiceClient client = createTokenCredentialClient(sentRequest, null);

        WebPubSubClientAccessToken token = client.getClientAccessToken(
            new GetClientAccessTokenOptions().setUserId("alice").setExpiresAfter(Duration.ofMinutes(30)));

        assertEquals("server-token", token.getToken());
        assertEquals("wss://example.webpubsub.azure.com/client/hubs/chat?access_token=server-token", token.getUrl());
        assertEquals("/api/hubs/chat/:generateToken", sentRequest.get().getUrl().getPath());
        String query = sentRequest.get().getUrl().getQuery();
        assertTrue(query.contains("userId=alice"));
        assertTrue(query.contains("minutesToExpire=30"));
        assertTrue(query.contains("role=webpubsub.getGroupState"));
        assertTrue(query.contains("role=webpubsub.setGroupState"));
        assertTrue(query.contains("api-version=2024-12-01"));
        assertTrue(query.contains("clientType=default"));
        assertEquals("Bearer mockToken", sentRequest.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    @Test
    public void asyncTokenCredentialCallsGenerateTokenOperation() {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        WebPubSubChatServiceAsyncClient client = new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
            .hub("chat")
            .credential(new MockTokenCredential())
            .httpClient(request -> Mono.defer(() -> {
                sentRequest.set(request);
                byte[] body = "{\"token\":\"server-token\"}".getBytes(StandardCharsets.UTF_8);
                return Mono.just(new MockHttpResponse(request, 200, body));
            }))
            .buildAsyncClient();

        WebPubSubClientAccessToken token
            = client.getClientAccessToken(new GetClientAccessTokenOptions().setUserId("alice")).block();

        assertNotNull(token);
        assertEquals("server-token", token.getToken());
        assertEquals("/api/hubs/chat/:generateToken", sentRequest.get().getUrl().getPath());
        assertTrue(sentRequest.get().getUrl().getQuery().contains("userId=alice"));
        assertTrue(sentRequest.get().getUrl().getQuery().contains("minutesToExpire=60"));
    }

    @Test
    public void tokenCredentialUsesReverseProxyForGenerateTokenOperation() {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        WebPubSubChatServiceClient client = createTokenCredentialClient(sentRequest, "https://proxy.example/gateway");

        client.getClientAccessToken(new GetClientAccessTokenOptions());

        assertEquals("proxy.example", sentRequest.get().getUrl().getHost());
        assertEquals("/gateway/api/hubs/chat/:generateToken", sentRequest.get().getUrl().getPath());
        assertEquals("Bearer mockToken", sentRequest.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    @Test
    public void validatesTokenOptions() {
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
            .hub("chat")
            .credential(new AzureKeyCredential(KEY))
            .buildClient();

        assertThrows(NullPointerException.class, () -> client.getClientAccessToken(null));
        assertThrows(NullPointerException.class,
            () -> client.getClientAccessToken(new GetClientAccessTokenOptions().setExpiresAfter(null)));
        assertThrows(IllegalArgumentException.class, () -> client
            .getClientAccessToken(new GetClientAccessTokenOptions().setExpiresAfter(Duration.ofSeconds(59))));
    }

    private static WebPubSubChatServiceClient createTokenCredentialClient(AtomicReference<HttpRequest> sentRequest,
        String reverseProxyEndpoint) {
        WebPubSubChatServiceClientBuilder builder = new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
            .hub("chat")
            .credential(new MockTokenCredential())
            .httpClient(request -> Mono.defer(() -> {
                sentRequest.set(request);
                byte[] body = "{\"token\":\"server-token\"}".getBytes(StandardCharsets.UTF_8);
                return Mono.just(new MockHttpResponse(request, 200, body));
            }));
        if (reverseProxyEndpoint != null) {
            builder.reverseProxyEndpoint(reverseProxyEndpoint);
        }
        return builder.buildClient();
    }
}
