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

public class MockFailureResponsePolicy implements HttpPipelinePolicy {

    private int tries;

    public MockFailureResponsePolicy(int tries) {
        this.tries = tries;
    }
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().flatMap(response -> {
            if (this.tries == 0) {
                return Mono.just(response);
            } else {
                this.tries -= 1;
                return Mono.<HttpResponse> just(new MockDownloadHttpResponse(response, 206,
                    Flux.error(new IOException())));
            }
        });
    }
}
