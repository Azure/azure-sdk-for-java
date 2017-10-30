/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;

import java.util.UUID;

/**
 * An instance of this class puts an UUID in the request header. Azure uses
 * the request id as the unique identifier for the request.
 */
public final class RequestIdPolicy implements RequestPolicy {
    private static final String REQUEST_ID_HEADER = "x-ms-client-request-id";

    /**
     * Factory which instantiates RequestIdPolicy.
     */
    public static class Factory implements RequestPolicy.Factory {
        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new RequestIdPolicy(next);
        }
    }

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
