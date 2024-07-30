// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.servlet.ServletException;
import java.util.concurrent.ThreadLocalRandom;

@Execution(ExecutionMode.SAME_THREAD)
public class DeadlockTests {
    private static final String GET_ENDPOINT = "/get";

    private LocalTestServer server;
    private byte[] expectedGetBytes;

    @BeforeEach
    public void startTestServer() {
        expectedGetBytes = new byte[32768];
        ThreadLocalRandom.current().nextBytes(expectedGetBytes);

        server = new LocalTestServer((req, resp, responseBody) -> {
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
        // TODO (vcolin7): Figure out how to attempt a deadlock without using Reactor.
        /*HttpClient httpClient = new OkHttpHttpClientProvider().createInstance();

        String endpoint = server.getHttpUri() + GET_ENDPOINT;

        Mono<Tuple2<byte[], Integer>> request = Mono.just(httpClient.send(new HttpRequest(HttpMethod.GET, endpoint)))
            .map(response -> Tuples.of(response.getBody().toBytes(), response.getStatusCode()));

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
        }*/
    }
}
