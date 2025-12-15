// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MockPartialResponsePolicy implements HttpPipelinePolicy {
    static final HttpHeaderName X_MS_RANGE_HEADER = HttpHeaderName.fromString("x-ms-range");
    static final HttpHeaderName RANGE_HEADER = HttpHeaderName.RANGE;
    private int tries;
    private final List<String> rangeHeaders = new ArrayList<>();
    private final int maxBytesPerResponse;  // Maximum bytes to return before simulating timeout

    /**
     * Creates a MockPartialResponsePolicy that simulates network interruptions.
     * 
     * @param tries Number of times to simulate interruptions (0 = no interruptions)
     */
    public MockPartialResponsePolicy(int tries) {
        this(tries, 560);  // Default: return up to 560 bytes before interrupting (enough for 1 segment + header)
    }

    /**
     * Creates a MockPartialResponsePolicy with configurable interruption behavior.
     * 
     * @param tries Number of times to simulate interruptions (0 = no interruptions)
     * @param maxBytesPerResponse Maximum bytes to return in each interrupted response
     */
    public MockPartialResponsePolicy(int tries, int maxBytesPerResponse) {
        this.tries = tries;
        this.maxBytesPerResponse = maxBytesPerResponse;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(response -> {
            HttpHeader rangeHttpHeader = response.getRequest().getHeaders().get(RANGE_HEADER);
            HttpHeader xMsRangeHttpHeader = response.getRequest().getHeaders().get(X_MS_RANGE_HEADER);

            if (rangeHttpHeader != null && rangeHttpHeader.getValue().startsWith("bytes=")) {
                rangeHeaders.add(rangeHttpHeader.getValue());
            }

            if (xMsRangeHttpHeader != null && xMsRangeHttpHeader.getValue().startsWith("bytes=")) {
                String xMsRangeValue = xMsRangeHttpHeader.getValue();

                // Avoid recording the same value twice if both Range and x-ms-range were set to the same string
                if (rangeHttpHeader == null || !xMsRangeValue.equals(rangeHttpHeader.getValue())) {
                    rangeHeaders.add(xMsRangeValue);
                }
            }

            if ((response.getRequest().getHttpMethod() != HttpMethod.GET) || this.tries == 0) {
                return Mono.just(response);
            } else {
                this.tries -= 1;
                // Simulate partial response by limiting the amount of data returned from the stream
                // before throwing an IOException to simulate a network interruption.
                // This tests smart retry behavior where downloads should resume from the last
                // complete segment boundary after each interruption.
                Flux<ByteBuffer> interruptedBody = limitAndInterruptStream(response.getBody(), maxBytesPerResponse);
                return Mono.just(new MockDownloadHttpResponse(response, 206, interruptedBody));
            }
        });
    }

    /**
     * Limits a stream to return at most maxBytes before throwing an IOException.
     */
    private Flux<ByteBuffer> limitAndInterruptStream(Flux<ByteBuffer> body, int maxBytes) {
        return Flux.defer(() -> {
            final int[] bytesEmitted = new int[] {0};
            return body.concatMap(buffer -> {
                int remaining = maxBytes - bytesEmitted[0];
                if (remaining <= 0) {
                    // Already emitted enough bytes, throw error now
                    return Flux.error(new IOException("Simulated timeout"));
                }
                
                int bytesToEmit = Math.min(buffer.remaining(), remaining);
                if (bytesToEmit < buffer.remaining()) {
                    // Need to slice the buffer
                    ByteBuffer limited = ByteBuffer.allocate(bytesToEmit);
                    int originalLimit = buffer.limit();
                    buffer.limit(buffer.position() + bytesToEmit);
                    limited.put(buffer);
                    buffer.limit(originalLimit);
                    limited.flip();
                    bytesEmitted[0] += bytesToEmit;
                    // Emit the limited buffer, then error
                    return Flux.just(limited).concatWith(Flux.error(new IOException("Simulated timeout")));
                } else {
                    // Emit the full buffer and continue
                    bytesEmitted[0] += bytesToEmit;
                    if (bytesEmitted[0] >= maxBytes) {
                        // Reached the limit, emit this buffer then error
                        return Flux.just(buffer).concatWith(Flux.error(new IOException("Simulated timeout")));
                    }
                    return Flux.just(buffer);
                }
            });
        });
    }
        });
    }

    public int getTriesRemaining() {
        return tries;
    }

    public List<String> getRangeHeaders() {
        return rangeHeaders;
    }
}
