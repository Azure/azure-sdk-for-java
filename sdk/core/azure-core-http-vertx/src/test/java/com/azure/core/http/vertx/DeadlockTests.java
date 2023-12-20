// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.LocalTestServer;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.servlet.ServletException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.SAME_THREAD)
public class DeadlockTests {
    private static final String GET_ENDPOINT = "/get";

    private LocalTestServer server;
    private byte[] expectedGetBytes;

    @BeforeEach
    public void startTestServer() {
        expectedGetBytes = new byte[32768];
        ThreadLocalRandom.current().nextBytes(expectedGetBytes);

        server = new LocalTestServer((req, resp, requestBody) -> {
            if ("GET".equalsIgnoreCase(req.getMethod()) && GET_ENDPOINT.equals(req.getServletPath())) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(expectedGetBytes.length);
                resp.getOutputStream().write(expectedGetBytes);
            } else {
                throw new ServletException("Unexpected request " + req.getMethod() + " " + req.getServletPath());
            }
        }, 20);

        server.start();
    }

    @AfterEach
    public void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void attemptToDeadlock() {
        HttpClient httpClient = new VertxAsyncHttpClientProvider().createInstance();

        String endpoint = server.getHttpUri() + GET_ENDPOINT;

        Mono<Tuple2<byte[], Integer>> request = httpClient.send(new HttpRequest(HttpMethod.GET, endpoint))
            .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getBody(), 32768)
                .map(bytes -> Tuples.of(bytes, response.getStatusCode())));

        List<Tuple2<byte[], Integer>> results = Flux.range(0, 100)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap(ignored -> request)
            .sequential()
            .collectList()
            .block();

        for (Tuple2<byte[], Integer> result : results) {
            assertEquals(200, result.getT2());
            TestUtils.assertArraysEqual(expectedGetBytes, result.getT1());
        }
    }
}
