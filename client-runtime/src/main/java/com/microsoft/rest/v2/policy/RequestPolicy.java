/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;

/**
 * Uses the decorator pattern to add custom behavior when an HTTP request is made.
 * e.g. add header, user agent, timeout, retry, etc.
 *
 */
public interface RequestPolicy {
    /**
     * Sends an HTTP request as an asynchronous operation.
     *
     * @param request The HTTP request message to send.
     * @return The rx.Single instance representing the asynchronous operation.
     */
    Single<HttpResponse> sendAsync(HttpRequest request);

    /**
     * Factory to create a RequestPolicy. RequestPolicies are instantiated per-request
     * so that they can contain instance state specific to that request/response exchange,
     * for example, the number of retries attempted so far in a counter.
     */
    interface Factory {
        /**
         * Creates RequestPolicy.
         *
         * @param next the next RequestPolicy in the request-response pipeline.
         * @return the RequestPolicy
         */
        RequestPolicy create(RequestPolicy next);
    }
}
