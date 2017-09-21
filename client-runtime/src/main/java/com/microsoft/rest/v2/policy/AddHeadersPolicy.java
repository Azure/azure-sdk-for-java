/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;

/**
 * Adds a particular set of headers to HTTP requests.
 */
public final class AddHeadersPolicy implements RequestPolicy {
    private final HttpHeaders headers;
    private final RequestPolicy next;

    /**
     * Creates AddHeadersPolicy.
     *
     * @param next reference to the next RequestPolicy in the request-response pipeline.
     */
    private AddHeadersPolicy(HttpHeaders headers, RequestPolicy next) {
        this.headers = headers;
        this.next = next;
    }

    /**
     * Factory to create AddHeadersPolicy.
     */
    public static class Factory implements RequestPolicy.Factory {
        private final HttpHeaders headers;

        /**
         * Creates a AddHeadersPolicy.Factory which adds headers from this HttpHeaders object to the request.
         * @param headers The headers to add to outgoing requests.
         */
        public Factory(HttpHeaders headers) {
            this.headers = headers;
        }

        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new AddHeadersPolicy(headers, next);
        }
    }

    @Override
    public Single<HttpResponse> sendAsync(HttpRequest request) {
        for (HttpHeader header : headers) {
            request.withHeader(header.name(), header.value());
        }

        return next.sendAsync(request);
    }
}
