// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.validation.http.LocalTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.servlet.ServletException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.SAME_THREAD)
public class OkHttpResponseTimeoutTests {
    private static LocalTestServer server;

    @BeforeAll
    public static void startServer() {
        server = new LocalTestServer((request, response, body) -> {
            response.setStatus(200);
            response.setContentLength(2);
            response.getHttpOutput().write('o');
            response.getHttpOutput().flush();
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServletException(e);
            }
            response.getHttpOutput().write('k');
        });
        server.start();
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @ParameterizedTest
    @CsvSource({ "false, false", "false, true", "true, false", "true, true" })
    public void finalHeadersStopResponseTimeout(boolean synchronous, boolean expectContinue) {
        HttpClient client = new OkHttpAsyncHttpClientBuilder().responseTimeout(Duration.ofMillis(200))
            .readTimeout(Duration.ofSeconds(2))
            .build();
        HttpRequest request = new HttpRequest(HttpMethod.POST, server.getHttpUri() + "/slow-body").setBody("request");
        if (expectContinue) {
            request.setHeader(HttpHeaderName.EXPECT, "100-continue");
        }

        if (synchronous) {
            try (HttpResponse response = client.sendSync(request, Context.NONE)) {
                assertEquals(200, response.getStatusCode());
                assertEquals("ok", response.getBodyAsBinaryData().toString());
            }
        } else {
            StepVerifier.create(client.send(request)
                .flatMap(response -> Mono.using(() -> response, HttpResponse::getBodyAsString, HttpResponse::close)))
                .expectNext("ok")
                .expectComplete()
                .verify(Duration.ofSeconds(5));
        }
    }
}
