/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Single;

import java.util.concurrent.TimeUnit;

/**
 * Creates a RequestPolicy that limits the time allowed between sending a request and receiving the response.
 */
public final class TimeoutPolicyFactory implements RequestPolicyFactory {
    private final long timeout;
    private final TimeUnit unit;

    /**
     * Creates a TimeoutPolicyFactory.
     * @param timeout the length of the timeout
     * @param unit the unit of the timeout
     */
    public TimeoutPolicyFactory(long timeout, TimeUnit unit) {
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
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            return next.sendAsync(request).timeout(timeout, unit);
        }
    }
}
