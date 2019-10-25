// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * The pipeline policy that limits the time allowed between sending a request and receiving the response.
 */
public class TimeoutPolicy implements HttpPipelinePolicy {
    private final Duration timoutDuration;

    /**
     * Creates a TimeoutPolicy.
     *
     * @param timoutDuration the timeout duration
     */
    public TimeoutPolicy(Duration timoutDuration) {
        this.timoutDuration = timoutDuration;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().timeout(this.timoutDuration);
    }
}
