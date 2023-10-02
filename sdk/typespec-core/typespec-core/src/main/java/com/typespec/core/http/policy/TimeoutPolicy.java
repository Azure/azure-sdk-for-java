// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.http.HttpPipelineCallContext;
import com.typespec.core.http.HttpPipelineNextPolicy;
import com.typespec.core.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * The pipeline policy that limits the time allowed between sending a request and receiving the response.
 * @deprecated Consider configuring timeouts with {@link com.typespec.core.util.HttpClientOptions}.
 */
@Deprecated
public class TimeoutPolicy implements HttpPipelinePolicy {
    private final Duration timeoutDuration;

    /**
     * Creates a TimeoutPolicy.
     *
     * @param timeoutDuration the timeout duration
     */
    public TimeoutPolicy(Duration timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return next.process().timeout(this.timeoutDuration);
    }
}
