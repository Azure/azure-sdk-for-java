/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.protocol.HttpResponseDecoder;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

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
        public Single<HttpResponse> sendAsync(final HttpRequest request) {
            return next.sendAsync(request).flatMap(new Function<HttpResponse, SingleSource<? extends HttpResponse>>() {
                @Override
                public SingleSource<? extends HttpResponse> apply(final HttpResponse response) throws Exception {
                    HttpResponseDecoder decoder = request.responseDecoder();
                    if (decoder != null) {
                        return request.responseDecoder().decode(response);
                    } else {
                        return Single.error(new NullPointerException("HttpRequest.responseDecoder() was null when decoding."));
                    }
                }
            });
        }
    }
}
