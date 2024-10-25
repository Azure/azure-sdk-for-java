// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common.implementation;

import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import java.nio.ByteBuffer;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;

public class HmacAuthenticationPolicyTests {

    class NoOpHttpClient implements HttpClient {
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.empty(); // NOP
        }
    }

    private final String key = "68810419818922fb0263dd6ee4b9c56537dbad914aa7324a119fce26778a286e";
    private final HttpPipelinePolicy verifyHeadersPolicy = (context, next) -> {
        HttpRequest request = context.getHttpRequest();
        String dateHeaderValue = request.getHeaders().getValue("x-ms-date");
        LocalDateTime headerDate = LocalDateTime.parse(dateHeaderValue, DateTimeFormatter.RFC_1123_DATE_TIME);
        LocalDateTime current = LocalDateTime.now(ZoneOffset.UTC);
        assertTrue(Duration.between(headerDate, current).toMillis() < 5 * 1000,
                "Date header no more than five seconds apart from now");

        String hostHeaderValue = request.getHeaders().getValue("host");
        assertEquals("localhost", hostHeaderValue);

        String hashHeaderValue = request.getHeaders().getValue("x-ms-content-sha256");
        assertNotNull("Must contain hash header", hashHeaderValue);

        String authHeaderValue = request.getHeaders().getValue("Authorization");
        assertTrue(authHeaderValue.startsWith("HMAC-SHA256 SignedHeaders=x-ms-date;host;x-ms-content-sha256&Signature="));
        return next.process();
    };

    private final HttpPipelinePolicy verifyContentHashPolicy = (context, next) -> {
        HttpRequest request = context.getHttpRequest();
        String hashHeaderValue = request.getHeaders().getValue("x-ms-content-sha256");

        String expectedHash = getBodyHash(context);
        assertEquals(expectedHash, hashHeaderValue);

        return next.process();
    };

    private final HttpPipelinePolicy verifyExpectedStringHashPolicy = (context, next) -> {
        HttpRequest request = context.getHttpRequest();
        String hashHeaderValue = request.getHeaders().getValue("x-ms-content-sha256");

        String expectedHash = "tJPUg2Sv5E0RwBZc9HCkFk0eJgmRHvmYvoaNRq3j3k4=";
        assertEquals(expectedHash, hashHeaderValue);

        return next.process();
    };

    private final HttpPipelinePolicy verifyUnicodeStringHashPolicy = (context, next) -> {
        HttpRequest request = context.getHttpRequest();
        String hashHeaderValue = request.getHeaders().getValue("x-ms-content-sha256");

        String expectedHash = "8EQ6NCxe9UeDoRG1G6Vsk45HTDIyTZDDpgycjjo34tk=";
        assertEquals(expectedHash, hashHeaderValue);

        return next.process();
    };

    private String getBodyHash(HttpPipelineCallContext context) {
        Flux<ByteBuffer> contents = context.getHttpRequest().getBody() == null ? Flux.just(ByteBuffer.allocate(0))
                : context.getHttpRequest().getBody();
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(contents.blockFirst());
            return Base64.getEncoder().encodeToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private AzureKeyCredential credential;

    @BeforeEach
    public void setup() throws InvalidKeyException, NoSuchAlgorithmException {
        credential = new AzureKeyCredential(key);
    }

    @Test
    public void getRequestTest() throws MalformedURLException {
        final HmacAuthenticationPolicy clientPolicy = new HmacAuthenticationPolicy(credential);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(clientPolicy, verifyHeadersPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void postRequestTest() throws MalformedURLException {
        final HmacAuthenticationPolicy clientPolicy = new HmacAuthenticationPolicy(credential);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(clientPolicy, verifyHeadersPolicy, verifyContentHashPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        request.setBody("{\"propName\":\"name\", \"propValue\": \"value\"}");
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void postRequestWithPortTest() throws MalformedURLException {
        final HmacAuthenticationPolicy clientPolicy = new HmacAuthenticationPolicy(credential);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(clientPolicy, verifyHeadersPolicy, verifyContentHashPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("https://localhost:443?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        request.setBody("{\"propName\":\"name\", \"propValue\": \"value\"}");
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void crossLanguageAsciiHashMatchTest() throws MalformedURLException {
        final HmacAuthenticationPolicy clientPolicy = new HmacAuthenticationPolicy(credential);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(clientPolicy, verifyHeadersPolicy, verifyExpectedStringHashPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        request.setBody("banana");
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void crossLanguageUnicodeHashMatchTest() throws MalformedURLException {
        final HmacAuthenticationPolicy clientPolicy = new HmacAuthenticationPolicy(credential);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(clientPolicy, verifyHeadersPolicy, verifyUnicodeStringHashPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        request.setBody("😀");
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void patchRequestTest() throws MalformedURLException {
        final HmacAuthenticationPolicy clientPolicy = new HmacAuthenticationPolicy(credential);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(clientPolicy, verifyHeadersPolicy, verifyContentHashPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.PATCH, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        request.setBody("{\"propName\":\"name1\", \"propValue\": \"value1\"}");
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void putRequestTest() throws MalformedURLException {
        final HmacAuthenticationPolicy clientPolicy = new HmacAuthenticationPolicy(credential);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(clientPolicy, verifyHeadersPolicy, verifyContentHashPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.PUT, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        request.setBody("{\"propName\":\"name2\", \"propValue\": \"value2\"}");
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void deleteRequestTest() throws MalformedURLException {
        final HmacAuthenticationPolicy clientPolicy = new HmacAuthenticationPolicy(credential);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(clientPolicy, verifyHeadersPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.DELETE, new URL("https://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        StepVerifier.create(pipeline.send(request))
            .verifyComplete();
    }

    @Test
    public void httpRequestTest() throws MalformedURLException {
        final HmacAuthenticationPolicy clientPolicy = new HmacAuthenticationPolicy(credential);

        final HttpPipeline pipeline = new HttpPipelineBuilder()
                .httpClient(new NoOpHttpClient())
                .policies(clientPolicy, verifyHeadersPolicy)
                .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://localhost?id=b93a5ef4-f622-44d8-a80b-ff983122554e"));
        StepVerifier.create(pipeline.send(request))
            .verifyError(RuntimeException.class);
    }
}
