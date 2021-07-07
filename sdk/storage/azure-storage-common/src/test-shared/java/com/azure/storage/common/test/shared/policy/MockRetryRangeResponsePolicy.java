// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class MockRetryRangeResponsePolicy implements HttpPipelinePolicy {

    private final String rangeMatch;

    public MockRetryRangeResponsePolicy(String rangeMatch) {
        this.rangeMatch = rangeMatch;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(response -> {
            if (!response.getRequest().getHeaders().getValue("x-ms-range").equals(rangeMatch)) {
                return Mono.error(new IllegalArgumentException("The range header was not set correctly on retry."));
            } else {
                // ETag can be a dummy value. It's not validated, but DownloadResponse requires one
                return Mono.just(new MockDownloadHttpResponse(response, 206, Flux.error(new IOException())));
            }
        });
    }
}
