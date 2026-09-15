// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebPubSubChatServiceClientBuilderTests {
    private static final String ENDPOINT = "https://example.webpubsub.azure.com";
    private static final String FIRST_KEY = "01234567890123456789012345678901";
    private static final String SECOND_KEY = "abcdefghijklmnopqrstuvwxyzABCDEF";

    @Test
    public void keyCredentialAuthenticatesChatRequest() throws ParseException {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        WebPubSubChatServiceClient client = createClient(new AzureKeyCredential(FIRST_KEY), sentRequest);

        assertThrows(ResourceNotFoundException.class, () -> client.getRole("room.reader"));

        String authorization = sentRequest.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertNotNull(authorization);
        assertTrue(authorization.startsWith("Bearer "));
        JWTClaimsSet claims = SignedJWT.parse(authorization.substring("Bearer ".length())).getJWTClaimsSet();
        assertEquals(sentRequest.get().getUrl().toString(), claims.getAudience().get(0));
        assertTrue(claims.getExpirationTime().toInstant().isAfter(Instant.now()));
    }

    @Test
    public void keyCredentialUpdateChangesSubsequentTokens() {
        AzureKeyCredential credential = new AzureKeyCredential(FIRST_KEY);
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        WebPubSubChatServiceClient client = createClient(credential, sentRequest);

        assertThrows(ResourceNotFoundException.class, () -> client.getRole("room.reader"));
        String firstAuthorization = sentRequest.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        credential.update(SECOND_KEY);
        assertThrows(ResourceNotFoundException.class, () -> client.getRole("room.reader"));

        assertNotEquals(firstAuthorization, sentRequest.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    @Test
    public void signingFailureOmitsAuthorizationHeader() {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        WebPubSubChatServiceClient client = createClient(new AzureKeyCredential("short-key"), sentRequest);

        assertThrows(ResourceNotFoundException.class, () -> client.getRole("room.reader"));

        assertNull(sentRequest.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    @Test
    public void tokenCredentialAuthenticationIsPreserved() {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
            .hub("chat")
            .credential(new MockTokenCredential())
            .httpClient(request -> Mono.defer(() -> {
                sentRequest.set(request);
                return Mono.just(new MockHttpResponse(request, 404));
            }))
            .buildClient();

        assertThrows(ResourceNotFoundException.class, () -> client.getRole("room.reader"));

        assertEquals("Bearer mockToken", sentRequest.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    @Test
    public void connectionStringConfiguresEndpointPortAndCredential() throws ParseException {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder()
            .connectionString("Endpoint=https://example.webpubsub.azure.com;AccessKey=" + FIRST_KEY + ";Port=8443")
            .hub("chat")
            .httpClient(request -> Mono.defer(() -> {
                sentRequest.set(request);
                return Mono.just(new MockHttpResponse(request, 404));
            }))
            .buildClient();

        assertThrows(ResourceNotFoundException.class, () -> client.getRole("room.reader"));

        assertEquals(8443, sentRequest.get().getUrl().getPort());
        String authorization = sentRequest.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        JWTClaimsSet claims = SignedJWT.parse(authorization.substring("Bearer ".length())).getJWTClaimsSet();
        assertEquals(sentRequest.get().getUrl().toString(), claims.getAudience().get(0));
    }

    @Test
    public void connectionStringRejectsDuplicateKeys() {
        assertThrows(IllegalArgumentException.class, () -> new WebPubSubChatServiceClientBuilder()
            .connectionString("Endpoint=" + ENDPOINT + ";endpoint=" + ENDPOINT + ";AccessKey=" + FIRST_KEY));
    }

    @Test
    public void connectionStringRejectsMissingRequiredKeys() {
        assertThrows(IllegalArgumentException.class,
            () -> new WebPubSubChatServiceClientBuilder().connectionString("Endpoint=" + ENDPOINT));
        assertThrows(IllegalArgumentException.class,
            () -> new WebPubSubChatServiceClientBuilder().connectionString("AccessKey=" + FIRST_KEY));
    }

    @Test
    public void reverseProxyPreservesJavaServiceBehavior() throws ParseException {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
            .hub("chat")
            .credential(new AzureKeyCredential(FIRST_KEY))
            .reverseProxyEndpoint("https://proxy.example/gateway")
            .httpClient(request -> Mono.defer(() -> {
                sentRequest.set(request);
                return Mono.just(new MockHttpResponse(request, 404));
            }))
            .buildClient();

        assertThrows(ResourceNotFoundException.class, () -> client.getRole("room.reader"));

        assertEquals("proxy.example", sentRequest.get().getUrl().getHost());
        assertTrue(sentRequest.get().getUrl().getPath().startsWith("/gateway/api/hubs/chat/chat/roles/"));
        assertEquals("api-version=2026-02-01-preview", sentRequest.get().getUrl().getQuery());
        String authorization = sentRequest.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        JWTClaimsSet claims = SignedJWT.parse(authorization.substring("Bearer ".length())).getJWTClaimsSet();
        assertEquals(ENDPOINT + "/api/hubs/chat/chat/roles/room.reader?api-version=2026-02-01-preview",
            claims.getAudience().get(0));
    }

    @Test
    public void validatesBuilderInputs() {
        assertThrows(NullPointerException.class,
            () -> new WebPubSubChatServiceClientBuilder().credential((AzureKeyCredential) null));
        assertThrows(IllegalStateException.class,
            () -> new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
                .hub("")
                .credential(new AzureKeyCredential(FIRST_KEY))
                .buildClient());
        assertThrows(IllegalStateException.class,
            () -> new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT).hub("chat").buildClient());
    }

    private static WebPubSubChatServiceClient createClient(AzureKeyCredential credential,
        AtomicReference<HttpRequest> sentRequest) {
        return new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
            .hub("chat")
            .credential(credential)
            .httpClient(request -> Mono.defer(() -> {
                sentRequest.set(request);
                return Mono.just(new MockHttpResponse(request, 404));
            }))
            .buildClient();
    }
}
