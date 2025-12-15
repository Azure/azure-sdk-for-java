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

    public MockPartialResponsePolicy(int tries) {
        this.tries = tries;
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
                //  Simulate partial response by taking only the first buffer from the stream and immediately
                // throwing an error to simulate a network interruption. This tests smart retry behavior.
                Flux<ByteBuffer> interruptedBody = response.getBody().take(1).concatWith(Flux.error(new IOException("Simulated timeout")));
                return Mono.just(new MockDownloadHttpResponse(response, 206, interruptedBody));
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
