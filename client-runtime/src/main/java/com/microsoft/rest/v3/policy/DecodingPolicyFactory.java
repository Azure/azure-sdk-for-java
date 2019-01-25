/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.policy;

import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.protocol.HttpResponseDecoder;
import reactor.core.publisher.Mono;

/**
 * Creates a RequestPolicy which decodes the response body and headers.
 */
public final class DecodingPolicyFactory implements RequestPolicyFactory {
    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new DecodingPolicy(next);
    }

    private final class DecodingPolicy implements RequestPolicy {
        private final RequestPolicy next;
        private DecodingPolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Mono<HttpResponse> sendAsync(final HttpRequest request) {
            return next.sendAsync(request).flatMap(response -> {
                HttpResponseDecoder decoder = request.responseDecoder();
                if (decoder != null) {
                    return request.responseDecoder().decode(response);
                } else {
                    return Mono.error(new NullPointerException("HttpRequest.responseDecoder() was null when decoding."));
                }
            });
        }
    }
}
