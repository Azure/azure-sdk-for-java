// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.utils.SharedExecutorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.servlet.ServletException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public void attemptToDeadlock() throws InterruptedException, ExecutionException {
        HttpClient httpClient = new OkHttpHttpClientProvider().getSharedInstance();

        String endpoint = server.getHttpUri() + GET_ENDPOINT;

        List<Future<Response<BinaryData>>> futures = SharedExecutorService.getInstance().invokeAll(IntStream.range(0, 100)
            .mapToObj(ignored -> (Callable<Response<BinaryData>>) () -> httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(endpoint)))
            .collect(Collectors.toList()));

        for (Future<Response<BinaryData>> future : futures) {
            Response<BinaryData> response = future.get();

            assertEquals(200, response.getStatusCode());
            TestUtils.assertArraysEqual(expectedGetBytes, response.getValue().toBytes());
        }
    }
}
