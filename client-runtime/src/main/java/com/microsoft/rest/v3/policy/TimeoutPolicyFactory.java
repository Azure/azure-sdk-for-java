/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.policy;

import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Creates a RequestPolicy that limits the time allowed between sending a request and receiving the response.
 */
public final class TimeoutPolicyFactory implements RequestPolicyFactory {
    private final long timeout;
    private final ChronoUnit unit;

    /**
     * Creates a TimeoutPolicyFactory.
     * @param timeout the length of the timeout
     * @param unit the unit of the timeout
     */
    public TimeoutPolicyFactory(long timeout, ChronoUnit unit) {
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new TimeoutPolicy(next);
    }

    private final class TimeoutPolicy implements RequestPolicy {
        private final RequestPolicy next;
        TimeoutPolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Mono<HttpResponse> sendAsync(HttpRequest request) {
            return next.sendAsync(request).timeout(Duration.of(timeout, unit));
        }
    }
}
