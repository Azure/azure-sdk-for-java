// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.FluxUtil;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.SecureRandom;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeadlockTests {
    private static final String GET_ENDPOINT = "/get";

    private WireMockServer server;
    private byte[] expectedGetBytes;

    @BeforeEach
    public void configureWireMockServer() {
        expectedGetBytes = new byte[10 * 1024 * 1024];
        new SecureRandom().nextBytes(expectedGetBytes);

        server = new WireMockServer(WireMockConfiguration.options()
            .dynamicPort()
            .disableRequestJournal()
            .gzipDisabled(true));

        server.stubFor(get(GET_ENDPOINT).willReturn(aResponse().withBody(expectedGetBytes)));

        server.start();
    }

    @AfterEach
    public void shutdownWireMockServer() {
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    public void attemptToDeadlock() {
        HttpClient httpClient = new OkHttpAsyncClientProvider().createInstance();

        String endpoint = server.baseUrl() + GET_ENDPOINT;

        for (int i = 0; i < 100; i++) {
            StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, endpoint))
                .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getBody())
                    .zipWith(Mono.just(response.getStatusCode()))))
                .assertNext(responseTuple -> {
                    assertEquals(200, responseTuple.getT2());
                    assertArrayEquals(expectedGetBytes, responseTuple.getT1());
                })
                .verifyComplete();
        }
    }
}
