/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.policy;

import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import reactor.core.publisher.Mono;

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
     * @return The reactor.core.publisher.Mono instance representing the asynchronous operation.
     */
    Mono<HttpResponse> sendAsync(HttpRequest request);
}
