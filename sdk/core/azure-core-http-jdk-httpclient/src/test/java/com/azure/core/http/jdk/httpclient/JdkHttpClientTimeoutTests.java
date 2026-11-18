// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.util.HttpUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.validation.http.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.servlet.ServletException;
import java.io.UncheckedIOException;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisabledForJreRange(max = JRE.JAVA_11)
@Execution(ExecutionMode.SAME_THREAD)
public class JdkHttpClientTimeoutTests {
    private static final Duration TEST_TIMEOUT = Duration.ofMinutes(1);
    private static final Duration SUCCESS_TIMEOUT = Duration.ofSeconds(30);
    // The delayed body/upload must outlast the response probe, but not a healthy read or write deadline.
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration SLOW_OPERATION_DELAY = Duration.ofSeconds(3);
    private static LocalTestServer server;

    @BeforeAll
    public static void startServer() {
        // Timed-out requests can leave sleeping handlers; keep workers available for subsequent timeout probes.
        server = new LocalTestServer((request, response, requestBody) -> {
            if ("/slow-headers".equals(request.getServletPath())) {
                sleep();
            }
            if ("/upload".equals(request.getServletPath())) {
                assertEquals(2, requestBody.length);
            }
            response.setContentLength(2);
            response.getHttpOutput().write('o');
            response.getHttpOutput().flush();
            if ("/slow-body".equals(request.getServletPath())) {
                sleep();
            }
            response.getHttpOutput().write('k');
        }, 32);
        server.start();
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @ParameterizedTest
    @CsvSource({
        "false, false, 2000",
        "false, true, 2000",
        "true, false, 2000",
        "true, true, 2000",
        "false, false, 0",
        "false, true, 0",
        "true, false, 0",
        "true, true, 0",
        "false, false, -1",
        "false, true, -1",
        "true, false, -1",
        "true, true, -1" })
    public void responseOverrideDoesNotChangeReadTimeout(boolean synchronous, boolean buffered, long responseMillis) {
        HttpClient client = new JdkHttpClientBuilder().configuration(Configuration.NONE)
            .responseTimeout(SUCCESS_TIMEOUT)
            .readTimeout(SUCCESS_TIMEOUT)
            .build();
        Context context = new Context(HttpUtils.AZURE_RESPONSE_TIMEOUT, Duration.ofMillis(responseMillis))
            .addData(HttpUtils.AZURE_EAGERLY_READ_RESPONSE, buffered);
        assertRequestSucceeds(client, new HttpRequest(HttpMethod.GET, server.getHttpUri() + "/slow-body"), context,
            synchronous);
    }

    @ParameterizedTest
    @CsvSource({
        "false, false, false",
        "false, true, false",
        "true, false, false",
        "true, true, false",
        "false, false, true",
        "false, true, true",
        "true, false, true",
        "true, true, true" })
    public void responseTimeoutDoesNotIncludeUpload(boolean synchronous, boolean buffered, boolean knownLength) {
        HttpClient client = new JdkHttpClientBuilder().configuration(Configuration.NONE)
            .writeTimeout(SUCCESS_TIMEOUT)
            .responseTimeout(RESPONSE_TIMEOUT)
            .readTimeout(SUCCESS_TIMEOUT)
            .build();
        HttpRequest request = new HttpRequest(HttpMethod.POST, server.getHttpUri() + "/upload")
            .setBody(Flux.just(ByteBuffer.wrap(new byte[] { 1 }))
                .concatWith(Mono.delay(SLOW_OPERATION_DELAY).map(ignored -> ByteBuffer.wrap(new byte[] { 2 }))));
        if (knownLength) {
            request.setHeader(HttpHeaderName.CONTENT_LENGTH, "2");
        }
        assertRequestSucceeds(client, request, new Context(HttpUtils.AZURE_EAGERLY_READ_RESPONSE, buffered),
            synchronous);
    }

    @ParameterizedTest
    @CsvSource({
        "false, false, false",
        "false, true, false",
        "true, false, false",
        "true, true, false",
        "false, false, true",
        "false, true, true",
        "true, false, true",
        "true, true, true" })
    public void responseTimeoutStillAppliesAfterUpload(boolean synchronous, boolean buffered, boolean knownLength) {
        HttpClient client = new JdkHttpClientBuilder().configuration(Configuration.NONE)
            .responseTimeout(Duration.ofMillis(200))
            .readTimeout(SUCCESS_TIMEOUT)
            .build();
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, server.getHttpUri() + "/slow-headers").setBody("request");
        if (knownLength) {
            request.setHeader(HttpHeaderName.CONTENT_LENGTH, "7");
        }
        Context context = new Context(HttpUtils.AZURE_EAGERLY_READ_RESPONSE, buffered);

        if (synchronous) {
            UncheckedIOException error = assertThrows(UncheckedIOException.class, () -> {
                try (HttpResponse response = client.sendSync(request, context)) {
                    assertEquals(200, response.getStatusCode());
                }
            });
            assertInstanceOf(HttpTimeoutException.class, error.getCause());
        } else {
            StepVerifier.create(client.send(request, context))
                .expectError(HttpTimeoutException.class)
                .verify(TEST_TIMEOUT);
        }
    }

    private static void assertRequestSucceeds(HttpClient client, HttpRequest request, Context context, boolean sync) {
        if (sync) {
            try (HttpResponse response = client.sendSync(request, context)) {
                assertEquals(200, response.getStatusCode());
                assertEquals("ok", response.getBodyAsBinaryData().toString());
            }
        } else {
            StepVerifier.create(client.send(request, context)
                .flatMap(response -> Mono.using(() -> response, HttpResponse::getBodyAsString, HttpResponse::close)))
                .expectNext("ok")
                .expectComplete()
                .verify(TEST_TIMEOUT);
        }
    }

    private static void sleep() throws ServletException {
        try {
            Thread.sleep(SLOW_OPERATION_DELAY.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServletException(e);
        }
    }
}
