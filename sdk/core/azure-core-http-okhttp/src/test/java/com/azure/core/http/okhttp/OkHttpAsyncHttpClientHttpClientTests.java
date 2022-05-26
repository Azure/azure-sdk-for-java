// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.HttpClientTestsWireMockServer;
import com.azure.core.test.http.HttpClientTests;
import com.azure.core.util.BinaryData;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Duration;

public class OkHttpAsyncHttpClientHttpClientTests extends HttpClientTests {
    private static WireMockServer server;

    @BeforeAll
    public static void getWireMockServer() {
        server = HttpClientTestsWireMockServer.getHttpClientTestsServer();
        server.start();
    }

    @AfterAll
    public static void shutdownWireMockServer() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Override
    protected int getWireMockPort() {
        return server.port();
    }

    @Override
    protected HttpClient createHttpClient() {
        return new OkHttpAsyncClientProvider().createInstance();
    }

    @Test
    public void testVerySlowFluxGetsInterruptedByOkHttpInternals() {
        HttpClient httpClient = new OkHttpAsyncHttpClientBuilder()
            .callTimeout(Duration.ofMillis(1000)) // this caps full req-res round trip.
            .build();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        Flux<ByteBuffer> delayedFlux = Flux.just(byteBuffer).map(ByteBuffer::duplicate).repeat(101)
            .delayElements(Duration.ofMillis(10))
            // append last element that takes a day to emit.
            .concatWith(Flux.just(byteBuffer).map(ByteBuffer::duplicate).delayElements(Duration.ofDays(1)));
        Mono<BinaryData> requestBodyMono = BinaryData.fromFlux(delayedFlux, null, false);

        StepVerifier.create(
            requestBodyMono.flatMap(data -> {
                HttpRequest request = new HttpRequest(
                    HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(), data);
                return httpClient.send(request);
            })
        ).verifyError();
    }

    @Test
    public void testUnresponsiveFluxGetsInterruptedInFluxRequestBody() {
        HttpClient httpClient = new OkHttpAsyncHttpClientBuilder()
            .callTimeout(Duration.ofMillis(1000)) // this caps full req-res round trip.
            .build();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        Flux<ByteBuffer> delayedFlux = Flux.just(byteBuffer).map(ByteBuffer::duplicate)
            .delayElements(Duration.ofDays(1));
        Mono<BinaryData> requestBodyMono = BinaryData.fromFlux(delayedFlux, null, false);

        StepVerifier.create(
            requestBodyMono.flatMap(data -> {
                HttpRequest request = new HttpRequest(
                    HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(), data);
                return httpClient.send(request);
            })
        ).verifyError();
    }
}
