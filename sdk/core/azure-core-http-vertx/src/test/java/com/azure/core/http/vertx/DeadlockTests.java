// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.vertx;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.EXPECTED_GET_BYTES;
import static com.azure.core.http.vertx.VertxHttpClientLocalTestServer.GET_ENDPOINT;
import static com.azure.core.validation.http.HttpValidatonUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.SAME_THREAD)
public class DeadlockTests {
    @Test
    public void attemptToDeadlock() {
        HttpClient httpClient = new VertxAsyncHttpClientProvider().createInstance();

        String endpoint = VertxHttpClientLocalTestServer.getServer().getHttpUri() + GET_ENDPOINT;

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
            assertArraysEqual(EXPECTED_GET_BYTES, result.getT1());
        }
    }
}
