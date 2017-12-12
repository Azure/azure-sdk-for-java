/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Single;

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
     * @return The io.reactivex.Single instance representing the asynchronous operation.
     */
    Single<HttpResponse> sendAsync(HttpRequest request);
}
