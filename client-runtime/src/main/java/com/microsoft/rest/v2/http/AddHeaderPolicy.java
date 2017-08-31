/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import rx.Single;

/**
 * Type representing RequestPolicy that can intercept and modify headers.
 */
public class AddHeaderPolicy implements RequestPolicy {
    private final RequestPolicy next;

    /**
     * Creates AddHeaderPolicy.
     *
     * @param next reference to the next RequestPolicy in the request-response pipeline.
     */
    public AddHeaderPolicy(RequestPolicy next) {
        this.next = next;
    }

    /**
     * Factory to create AddHeaderPolicy.
     */
    static class Factory implements RequestPolicy.Factory {
        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new AddHeaderPolicy(next);
        }
    }

    @Override
    public Single<HttpResponse> sendAsync(HttpRequest request) {

        request.headers().add("x-my-header", "42");
        return next.sendAsync(request);
    }
}
