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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

public class MockPartialResponsePolicy implements HttpPipelinePolicy {
    static final HttpHeaderName X_MS_RANGE_HEADER = HttpHeaderName.fromString("x-ms-range");
    static final HttpHeaderName RANGE_HEADER = HttpHeaderName.RANGE;
    private int tries;
    private final List<String> rangeHeaders = new ArrayList<>();
    private final int maxBytesPerResponse;  // Maximum bytes to return before simulating timeout
    private final AtomicInteger hits = new AtomicInteger();
    private final String targetUrlPrefix;

    /**
     * Creates a MockPartialResponsePolicy that simulates network interruptions.
     *
     * @param tries Number of times to simulate interruptions (0 = no interruptions)
     */
    public MockPartialResponsePolicy(int tries) {
        this(tries, 200, null);  // Default: return 200 bytes for subsequent interruptions (enables 3 interrupts with 1KB data)
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
        this.tries = tries;
        this.maxBytesPerResponse = maxBytesPerResponse;
        this.targetUrlPrefix = targetUrlPrefix;
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        // Apply on every retry to mirror .NET test behavior.
        return HttpPipelinePosition.PER_RETRY;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(response -> {
            HttpHeader rangeHttpHeader = response.getRequest().getHeaders().get(RANGE_HEADER);
            HttpHeader xMsRangeHttpHeader = response.getRequest().getHeaders().get(X_MS_RANGE_HEADER);

            // Record every GET attempt so tests can assert retries occurred, even if no range header was present.
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

            if ((response.getRequest().getHttpMethod() != HttpMethod.GET) || !urlMatches || this.tries == 0) {
                return Mono.just(response);
            } else {
                hits.incrementAndGet();
                System.out.println("[MockPartialResponsePolicy] invoked. tries=" + tries
                    + ", maxBytesPerResponse=" + maxBytesPerResponse);
                this.tries -= 1;

                // Simulate an interruption mid-stream (like FaultyStream in .NET) without mutating headers.
                // Emit up to maxBytesPerResponse, then complete early to let the decoder detect an incomplete message
                // and trigger smart-retry.
                Flux<ByteBuffer> limitedBody = limitStreamToBytes(response.getBody(), maxBytesPerResponse);
                return Mono.just(
                    new MockDownloadHttpResponse(response, response.getStatusCode(), response.getHeaders(),
                        limitedBody));
            }
        });
    }

    /**
     * Limits a Flux of ByteBuffers to emit at most maxBytes, then completes early to simulate
     * an abrupt connection close without surfacing an explicit error.
     */
    private Flux<ByteBuffer> limitStreamToBytes(Flux<ByteBuffer> body, int maxBytes) {
        return Flux.defer(() -> {
            final long[] bytesEmitted = new long[] { 0 };
            return body.concatMap(buffer -> {
                if (buffer == null || !buffer.hasRemaining()) {
                    return Flux.just(buffer);
                }

                long remaining = maxBytes - bytesEmitted[0];
                if (remaining <= 0) {
                    // Emit an error to simulate the network fault (mirrors FaultyStream in .NET).
                    return Flux.error(new IOException("Simulated timeout"));
                }

                int bufferSize = buffer.remaining();
                if (bufferSize <= remaining) {
                    bytesEmitted[0] += bufferSize;
                    if (bytesEmitted[0] >= maxBytes) {
                        // Emit this buffer then fail to simulate the connection drop.
                        return Flux.just(buffer).concatWith(Flux.error(new IOException("Simulated timeout")));
                    }
                    return Flux.just(buffer);
                } else {
                    // Buffer is larger than remaining, slice and then error.
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
        return tries;
    }

    public List<String> getRangeHeaders() {
        return rangeHeaders;
    }

    public int getHits() {
        return hits.get();
    }
}
