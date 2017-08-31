/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import rx.Single;

/**
 * Type representing request-response pipeline holding request policies.
 */
public class RequestPolicyChain extends HttpClient {
    private final RequestPolicy.Factory[] factories;

    /**
     * Creates RequestPolicyChain.
     *
     * @param factories the factories that can creates RequestPolicy instances in the pipeline.
     */
    public RequestPolicyChain(RequestPolicy.Factory... factories) {
        this.factories = factories;
    }

    /**
     * @return RequestPolicy instance
     */
    public RequestPolicy create() {
        RequestPolicy first = null;
        for (RequestPolicy.Factory factory : factories) {
            first = factory.create(first);
        }
        return first;
    }

    /**
     * Send the request asynchronously.
     *
     * @param request The HTTP request to send.
     * @return The rx.Single instance representing the asynchronous operation.
     */
    @Override
    public Single<? extends HttpResponse> sendRequestAsync(HttpRequest request) {
        return create().sendAsync(request);
    }
}
