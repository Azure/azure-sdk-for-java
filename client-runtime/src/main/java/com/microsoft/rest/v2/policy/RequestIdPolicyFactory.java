/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Single;

import java.util.UUID;

/**
 * Creates a policy which puts a UUID in the request header. Azure uses
 * the request id as the unique identifier for the request.
 */
public final class RequestIdPolicyFactory implements RequestPolicyFactory {
    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new RequestIdPolicy(next);
    }

    private static final class RequestIdPolicy implements RequestPolicy {
        private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";

        private final RequestPolicy next;
        private RequestIdPolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            String requestId = request.headers().value(REQUEST_ID_HEADER);
            if (requestId == null) {
                request.headers().set(REQUEST_ID_HEADER, UUID.randomUUID().toString());
            }

            return next.sendAsync(request);
        }
    }

}