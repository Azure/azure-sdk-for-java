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
 * Type represents a RequestPolicy in the request-response pipeline.
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
     * Factory to create a RequestPolicy.
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
