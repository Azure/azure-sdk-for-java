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
    static final HttpHeaderName RANGE_HEADER = HttpHeaderName.fromString("x-ms-range");
    private int tries;
    private final List<String> rangeHeaders = new ArrayList<>();

    public MockPartialResponsePolicy(int tries) {
        this.tries = tries;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(response -> {
            HttpHeader rangeHttpHeader = response.getRequest().getHeaders().get(RANGE_HEADER);
            String rangeHeader = rangeHttpHeader == null ? null : rangeHttpHeader.getValue();

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                rangeHeaders.add(rangeHeader);
            }

            if ((response.getRequest().getHttpMethod() != HttpMethod.GET) || this.tries == 0) {
                return Mono.just(response);
            } else {
                this.tries -= 1;
                return response.getBody().collectList().flatMap(bodyBuffers -> {
                    ByteBuffer firstBuffer = bodyBuffers.get(0);
                    byte firstByte = firstBuffer.get();

                    // Simulate partial response by returning the first byte only from the requested range and timeout
                    return Mono.just(new MockDownloadHttpResponse(response, 206,
                            Flux.just(ByteBuffer.wrap(new byte[] { firstByte }))
                                    .concatWith(Flux.error(new IOException("Simulated timeout")))
                    ));
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
