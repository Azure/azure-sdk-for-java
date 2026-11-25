// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @EnumSource(WebPubSubChatServiceVersion.class)
    public void reverseProxyPreservesJavaServiceBehavior(WebPubSubChatServiceVersion serviceVersion)
        throws ParseException {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        WebPubSubChatServiceClient client = new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT)
            .hub("chat")
            .credential(new AzureKeyCredential(FIRST_KEY))
            .serviceVersion(serviceVersion)
            .reverseProxyEndpoint("https://proxy.example/gateway")
            .httpClient(request -> Mono.defer(() -> {
                sentRequest.set(request);
                return Mono.just(new MockHttpResponse(request, 404));
            }))
            .buildClient();

        assertThrows(ResourceNotFoundException.class, () -> client.getRole("room.reader"));

        assertEquals("proxy.example", sentRequest.get().getUrl().getHost());
        assertTrue(sentRequest.get().getUrl().getPath().startsWith("/gateway/api/hubs/chat/chat/roles/"));
        assertEquals("api-version=" + serviceVersion.getVersion(), sentRequest.get().getUrl().getQuery());
        String authorization = sentRequest.get().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        JWTClaimsSet claims = SignedJWT.parse(authorization.substring("Bearer ".length())).getJWTClaimsSet();
        assertEquals(ENDPOINT + "/api/hubs/chat/chat/roles/room.reader?api-version=" + serviceVersion.getVersion(),
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

    @ParameterizedTest
    @MethodSource("pathSegments")
    public void validatesPathSegmentsBeforeTransport(String parameter, String value, boolean customPipeline,
        boolean async, boolean invalid) {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        HttpClient transport = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertTrue(async);
                return Mono.defer(() -> {
                    sentRequest.set(request);
                    return Mono.just(new MockHttpResponse(request, 404));
                });
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                assertTrue(!async);
                sentRequest.set(request);
                return new MockHttpResponse(request, 404);
            }
        };
        WebPubSubChatServiceClientBuilder builder
            = new WebPubSubChatServiceClientBuilder().endpoint(ENDPOINT).hub("chat");
        if (customPipeline) {
            HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(transport)
                .policies(
                    new AddHeadersPolicy(
                        new HttpHeaders().set("custom-policy", "first").set("first-policy", "preserved")),
                    new AddHeadersPolicy(new HttpHeaders().set("custom-policy", "second")))
                .build();
            builder.pipeline(pipeline);
        } else {
            builder.credential(new MockTokenCredential()).httpClient(transport);
        }
        Class<? extends RuntimeException> exception
            = invalid ? IllegalArgumentException.class : ResourceNotFoundException.class;
        if (async) {
            WebPubSubChatServiceAsyncClient client = builder.buildAsyncClient();
            assertThrows(exception, () -> getAsync(client, parameter, value).block());
        } else {
            WebPubSubChatServiceClient client = builder.buildClient();
            assertThrows(exception, () -> getSync(client, parameter, value));
        }
        if (invalid) {
            assertNull(sentRequest.get());
        } else {
            assertNotNull(sentRequest.get());
            assertTrue(sentRequest.get().getUrl().getPath().endsWith("/" + value.replace("%", "%25")));
            if (customPipeline) {
                assertEquals("second", sentRequest.get().getHeaders().getValue("custom-policy"));
                assertEquals("preserved", sentRequest.get().getHeaders().getValue("first-policy"));
            }
        }
    }

    private static Stream<Arguments> pathSegments() {
        return Stream.of("userId", "roomId", "topicId", "conversationId", "roleName")
            .flatMap(
                parameter -> Stream.of(false, true)
                    .flatMap(
                        custom -> Stream.of(false, true)
                            .flatMap(async -> Stream.concat(
                                Stream
                                    .of(".", "..", "%2e", "%2E", "%2e%2e", "%2E%2e", ".%2e", "%2E.", "%252e",
                                        "%252e%2e")
                                    .map(value -> Arguments.of(parameter, value, custom, async, true)),
                                Stream
                                    .of("...", "alice.smith", "c.room.default", "room.reader", ".leading", "trailing.",
                                        "part%2ename", "%2e%2e%2e")
                                    .map(value -> Arguments.of(parameter, value, custom, async, false))))));
    }

    private static void getSync(WebPubSubChatServiceClient client, String parameter, String value) {
        switch (parameter) {
            case "userId":
                client.getUser(value);
                break;

            case "roomId":
                client.getRoomWithResponse(value, new RequestOptions());
                break;

            case "topicId":
                client.getTopic("room", value);
                break;

            case "conversationId":
                client.getConversationWithResponse(value, new RequestOptions());
                break;

            case "roleName":
                client.getRole(value);
                break;

            default:
                throw new IllegalArgumentException("Unknown parameter: " + parameter);
        }
    }

    private static Mono<?> getAsync(WebPubSubChatServiceAsyncClient client, String parameter, String value) {
        switch (parameter) {
            case "userId":
                return client.getUser(value);

            case "roomId":
                return client.getRoomWithResponse(value, new RequestOptions());

            case "topicId":
                return client.getTopic("room", value);

            case "conversationId":
                return client.getConversationWithResponse(value, new RequestOptions());

            case "roleName":
                return client.getRole(value);

            default:
                throw new IllegalArgumentException("Unknown parameter: " + parameter);
        }
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
