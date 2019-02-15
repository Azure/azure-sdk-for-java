/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.http.HttpPipelineCallContext;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.NextPolicy;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * The Pipeline policy that limits the time allowed between sending a request
 * and receiving the response.
 *
 */
public class TimeoutPolicy implements HttpPipelinePolicy {
    private final long timeout;
    private final ChronoUnit unit;

    /**
     * Creates a TimeoutPolicy.
     *
     * @param timeout the length of the timeout
     * @param unit the unit of the timeout
     */
    public TimeoutPolicy(long timeout, ChronoUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, NextPolicy next) {
        return next.process().timeout(Duration.of(timeout, unit));
    }
}