// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.validation.http.HttpClientTests;
import com.azure.core.validation.http.HttpClientTestsServer;
import com.azure.core.validation.http.LocalTestServer;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Duration;

import static com.azure.core.http.okhttp.TestUtils.createQuietDispatcher;

@Execution(ExecutionMode.SAME_THREAD)
public class OkHttpAsyncHttpClientHttpClientTests extends HttpClientTests {
    private static LocalTestServer server;

    @BeforeAll
    public static void startTestServer() {
        server = HttpClientTestsServer.getHttpClientTestsServer();
        server.start();
    }

    @AfterAll
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    @Deprecated
    protected int getPort() {
        return server.getHttpPort();
    }

    @Override
    protected String getServerUri(boolean secure) {
        return secure ? server.getHttpsUri() : server.getHttpUri();
    }

    @Override
    protected HttpClient createHttpClient() {
        // For testing purposes we're using a custom Dispatcher that will swallow UnexpectedLengthException exceptions
        // thrown during testing as these are expected. By default, the OkHttp Dispatcher will use
        // Thread.getDefaultUncaughtExceptionHandler() to handle uncaught exceptions, which prints the exception stack
        // trace to System.err, which is just noise during testing.
        return new OkHttpAsyncHttpClientBuilder().dispatcher(createQuietDispatcher(UnexpectedLengthException.class, ""))
            .build();
    }

    @Test
    public void testVerySlowFluxGetsInterruptedByOkHttpInternals() {
        HttpClient httpClient = new OkHttpAsyncHttpClientBuilder()
            .dispatcher(createQuietDispatcher(IllegalStateException.class, "blocking read"))
            .callTimeout(Duration.ofMillis(1000)) // this caps full req-res round trip.
            .build();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        Flux<ByteBuffer> delayedFlux = Flux.just(byteBuffer)
            .map(ByteBuffer::duplicate)
            .repeat(101)
            .delayElements(Duration.ofMillis(10))
            // append last element that takes a day to emit.
            .concatWith(Flux.just(byteBuffer).map(ByteBuffer::duplicate).delayElements(Duration.ofDays(1)));
        Mono<BinaryData> requestBodyMono = BinaryData.fromFlux(delayedFlux, null, false);

        StepVerifier.create(requestBodyMono.flatMap(data -> {
            HttpRequest request
                = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(), data);
            return httpClient.send(request);
        })).verifyError();
    }

    @Test
    public void testUnresponsiveFluxGetsInterruptedInFluxRequestBody() {
        HttpClient httpClient = new OkHttpAsyncHttpClientBuilder()
            .dispatcher(createQuietDispatcher(IllegalStateException.class, "blocking read"))
            .callTimeout(Duration.ofMillis(1000)) // this caps full req-res round trip.
            .build();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        Flux<ByteBuffer> delayedFlux
            = Flux.just(byteBuffer).map(ByteBuffer::duplicate).delayElements(Duration.ofDays(1));
        Mono<BinaryData> requestBodyMono = BinaryData.fromFlux(delayedFlux, null, false);

        StepVerifier.create(requestBodyMono.flatMap(data -> {
            HttpRequest request
                = new HttpRequest(HttpMethod.PUT, getRequestUrl(ECHO_RESPONSE), new HttpHeaders(), data);
            return httpClient.send(request);
        })).verifyError();
    }
}
