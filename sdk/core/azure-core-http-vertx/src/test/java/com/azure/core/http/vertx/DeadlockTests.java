// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.FluxUtil;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

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
        expectedGetBytes = new byte[1024 * 1024];
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
        HttpClient httpClient = new VertxAsyncHttpClientProvider().createInstance();

        String endpoint = server.baseUrl() + GET_ENDPOINT;

        Mono<Tuple2<byte[], Integer>> request = httpClient.send(new HttpRequest(HttpMethod.GET, endpoint))
            .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getBody())
                .map(bytes -> Tuples.of(bytes, response.getStatusCode())));

        Collection<Tuple2<byte[], Integer>> results = Flux.range(0, 100)
            .parallel()
            .runOn(Schedulers.boundedElastic())
            .flatMap(ignored -> request)
            .collect(() -> new ConcurrentLinkedQueue<Tuple2<byte[], Integer>>(), Collection::add)
            .sequential()
            .blockLast();

        for (Tuple2<byte[], Integer> result : results) {
            assertEquals(200, result.getT2());
            assertArrayEquals(expectedGetBytes, result.getT1());
        }
    }
}
