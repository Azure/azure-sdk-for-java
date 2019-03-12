/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.http.HttpPipelineCallContext;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.HttpPipelineNextPolicy;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * The Pipeline policy that limits the time allowed between sending a request
 * and receiving the response.
 *
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