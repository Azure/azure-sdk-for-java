// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.LocalTestServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.servlet.ServletException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.SAME_THREAD)
public class OkHttpHttpClientDeadlockTests {
    private static final String GET_ENDPOINT = "/get";
    private static final byte[] EXPECTED_GET_BYTES;

    static {
        EXPECTED_GET_BYTES = new byte[32768];
        ThreadLocalRandom.current().nextBytes(EXPECTED_GET_BYTES);
    }

    private LocalTestServer server;

    @BeforeEach
    public void startTestServer() {
        server = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, (req, resp, responseBody) -> {
            if ("GET".equalsIgnoreCase(req.getMethod()) && GET_ENDPOINT.equals(req.getServletPath())) {
                resp.setContentType("application/octet-stream");
                resp.setContentLength(EXPECTED_GET_BYTES.length);
                resp.getOutputStream().write(EXPECTED_GET_BYTES);
            } else {
                throw new ServletException("Unexpected request " + req.getMethod() + " " + req.getServletPath());
            }
        });

        server.start();
    }

    @AfterEach
    public void stopTestServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void attemptToDeadlock() throws InterruptedException, ExecutionException {
        HttpClient httpClient = new OkHttpHttpClientProvider().getSharedInstance();

        String endpoint = server.getUri() + GET_ENDPOINT;

        ForkJoinPool pool = new ForkJoinPool((int) Math.ceil(Runtime.getRuntime().availableProcessors() / 2.0));
        try {
            List<Future<Response<BinaryData>>> futures = pool.invokeAll(IntStream.range(0, 100)
                .mapToObj(ignored -> (Callable<Response<BinaryData>>) () -> httpClient
                    .send(new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint)))
                .collect(Collectors.toList()));

            for (Future<Response<BinaryData>> future : futures) {
                Response<BinaryData> response = future.get();

                assertEquals(200, response.getStatusCode());
                TestUtils.assertArraysEqual(EXPECTED_GET_BYTES, response.getValue().toBytes());
            }
        } finally {
            pool.shutdown();
        }
    }
}
