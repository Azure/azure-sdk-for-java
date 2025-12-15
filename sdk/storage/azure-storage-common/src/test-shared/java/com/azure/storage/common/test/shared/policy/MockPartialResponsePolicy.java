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
                // Collect the body to be able to slice it properly
                return response.getBody().collectList().flatMap(bodyBuffers -> {
                    if (bodyBuffers.isEmpty()) {
                        // If no body was returned, don't attempt to slice a partial response
                        return Mono.just(response);
                    }
                    
                    // Calculate total bytes available
                    int totalBytes = bodyBuffers.stream().mapToInt(ByteBuffer::remaining).sum();
                    
                    // Determine how many bytes to return (limited by maxBytesPerResponse)
                    int bytesToReturn = Math.min(totalBytes, maxBytesPerResponse);
                    
                    if (bytesToReturn >= totalBytes) {
                        // Return all data and still throw error to simulate interruption during next chunk
                        return Mono.just(new MockDownloadHttpResponse(response, 206,
                            Flux.fromIterable(bodyBuffers)
                                .concatWith(Flux.error(new IOException("Simulated timeout")))));
                    }
                    
                    // Create a new buffer with limited bytes
                    ByteBuffer limited = ByteBuffer.allocate(bytesToReturn);
                    int bytesCollected = 0;
                    
                    for (ByteBuffer buffer : bodyBuffers) {
                        int bufferRemaining = buffer.remaining();
                        int bytesNeeded = bytesToReturn - bytesCollected;
                        
                        if (bufferRemaining <= bytesNeeded) {
                            // Take the entire buffer
                            limited.put(buffer);
                            bytesCollected += bufferRemaining;
                        } else {
                            // Take only part of this buffer
                            ByteBuffer slice = buffer.duplicate();
                            slice.limit(slice.position() + bytesNeeded);
                            limited.put(slice);
                            bytesCollected += bytesNeeded;
                            break;
                        }
                    }
                    
                    limited.flip();
                    
                    // Return the limited buffer and simulate timeout
                    return Mono.just(new MockDownloadHttpResponse(response, 206,
                        Flux.just(limited).concatWith(Flux.error(new IOException("Simulated timeout")))));
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
