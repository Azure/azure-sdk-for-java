// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MockPartialResponsePolicy implements HttpPipelinePolicy {
    static final HttpHeaderName X_MS_RANGE_HEADER = HttpHeaderName.fromString("x-ms-range");
    static final HttpHeaderName RANGE_HEADER = HttpHeaderName.RANGE;
    private final AtomicInteger tries;
    private final List<String> rangeHeaders = Collections.synchronizedList(new ArrayList<>());
    private final int maxBytesPerResponse;
    private final AtomicInteger hits = new AtomicInteger();
    private final String targetUrlPrefix;

    /**
     * Creates a MockPartialResponsePolicy that simulates network interruptions.
     *
     * @param tries Number of times to simulate interruptions (0 = no interruptions)
     */
    public MockPartialResponsePolicy(int tries) {
        this(tries, 200, null);
    }

    /**
     * Creates a MockPartialResponsePolicy with configurable interruption behavior.
     *
     * @param tries Number of times to simulate interruptions (0 = no interruptions)
     * @param maxBytesPerResponse Maximum bytes to return in each interrupted response
     */
    public MockPartialResponsePolicy(int tries, int maxBytesPerResponse) {
        this(tries, maxBytesPerResponse, null);
    }

    /**
     * Creates a MockPartialResponsePolicy with configurable interruption behavior and an optional URL filter.
     *
     * @param tries Number of times to simulate interruptions (0 = no interruptions)
     * @param maxBytesPerResponse Maximum bytes to return in each interrupted response
     * @param targetUrlPrefix If non-null, only requests whose URL starts with this prefix will be interrupted.
     */
    public MockPartialResponsePolicy(int tries, int maxBytesPerResponse, String targetUrlPrefix) {
        this.tries = new AtomicInteger(tries);
        this.maxBytesPerResponse = maxBytesPerResponse;
        this.targetUrlPrefix = targetUrlPrefix;
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_RETRY;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(response -> {
            HttpHeader rangeHttpHeader = response.getRequest().getHeaders().get(RANGE_HEADER);
            HttpHeader xMsRangeHttpHeader = response.getRequest().getHeaders().get(X_MS_RANGE_HEADER);

            if (response.getRequest().getHttpMethod() == HttpMethod.GET) {
                String recordedRange = null;
                if (rangeHttpHeader != null && rangeHttpHeader.getValue().startsWith("bytes=")) {
                    recordedRange = rangeHttpHeader.getValue();
                } else if (xMsRangeHttpHeader != null && xMsRangeHttpHeader.getValue().startsWith("bytes=")) {
                    recordedRange = xMsRangeHttpHeader.getValue();
                }
                rangeHeaders.add(recordedRange == null ? "" : recordedRange);
            }

            boolean urlMatches = targetUrlPrefix == null
                || response.getRequest().getUrl().toString().startsWith(targetUrlPrefix);

            if ((response.getRequest().getHttpMethod() != HttpMethod.GET) || !urlMatches) {
                return Mono.just(response);
            } else {
                int remainingTries = this.tries.getAndUpdate(value -> value > 0 ? value - 1 : value);
                if (remainingTries <= 0) {
                    return Mono.just(response);
                }
                hits.incrementAndGet();
                System.out.println("[MockPartialResponsePolicy] invoked. tries=" + remainingTries
                    + ", maxBytesPerResponse=" + maxBytesPerResponse);

                Flux<ByteBuffer> limitedBody = limitStreamToBytes(response.getBody(), maxBytesPerResponse);
                return Mono.just(
                    new MockDownloadHttpResponse(response, response.getStatusCode(), response.getHeaders(),
                        limitedBody));
            }
        });
    }

    private Flux<ByteBuffer> limitStreamToBytes(Flux<ByteBuffer> body, int maxBytes) {
        return Flux.defer(() -> {
            final long[] bytesEmitted = new long[] { 0 };
            return body.concatMap(buffer -> {
                if (buffer == null || !buffer.hasRemaining()) {
                    return Flux.just(buffer);
                }

                long remaining = maxBytes - bytesEmitted[0];
                if (remaining <= 0) {
                    return Flux.error(new IOException("Simulated timeout"));
                }

                int bufferSize = buffer.remaining();
                if (bufferSize <= remaining) {
                    bytesEmitted[0] += bufferSize;
                    if (bytesEmitted[0] >= maxBytes) {
                        return Flux.just(buffer).concatWith(Flux.error(new IOException("Simulated timeout")));
                    }
                    return Flux.just(buffer);
                } else {
                    int bytesToEmit = (int) remaining;
                    ByteBuffer slice = buffer.duplicate();
                    slice.limit(slice.position() + bytesToEmit);

                    ByteBuffer limited = ByteBuffer.allocate(bytesToEmit);
                    limited.put(slice);
                    limited.flip();

                    bytesEmitted[0] += bytesToEmit;
                    return Flux.just(limited).concatWith(Flux.error(new IOException("Simulated timeout")));
                }
            });
        });
    }

    public int getTriesRemaining() {
        return tries.get();
    }

    public List<String> getRangeHeaders() {
        return rangeHeaders;
    }

    public int getHits() {
        return hits.get();
    }
}
