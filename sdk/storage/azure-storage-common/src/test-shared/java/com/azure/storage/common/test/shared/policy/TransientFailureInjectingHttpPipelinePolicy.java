// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Injects one retry-able IOException failure per url.
 */
public final class TransientFailureInjectingHttpPipelinePolicy implements HttpPipelinePolicy {
    private final ConcurrentHashMap<String, Boolean> failureTracker = new ConcurrentHashMap<>();

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();
        String key = request.getUrl().toString();

        // Make sure that failure happens once per url.
        if (failureTracker.get(key) == null) {
            failureTracker.put(key, false);
            return next.process();
        } else {
            failureTracker.put(key, true);
            return request.getBody().flatMap(byteBuffer -> {
                // Read a byte from each buffer to simulate that failure occurred in the middle of transfer.
                byteBuffer.get();
                return Flux.just(byteBuffer);
            })// Reduce in order to force processing of all buffers.
                .reduce(0L, (a, byteBuffer) -> a + byteBuffer.remaining())
                .flatMap(aLong -> Mono.error(new IOException("KABOOM!")));
        }
    }
}
