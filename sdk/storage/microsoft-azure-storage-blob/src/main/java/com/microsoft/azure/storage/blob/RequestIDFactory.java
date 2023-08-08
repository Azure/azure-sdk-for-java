// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;

import java.util.UUID;

/**
 * This is a factory which creates policies in an {@link HttpPipeline} for setting a unique request ID in the
 * x-ms-client-request-id header as is required for all requests to the service. In most cases, it is sufficient to
 * allow the default pipeline to add this factory automatically and assume that it works. The factory and policy must
 * only be used directly when creating a custom pipeline.
 */
public final class RequestIDFactory implements RequestPolicyFactory {

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new RequestIDPolicy(next, options);
    }

    private static final class RequestIDPolicy implements RequestPolicy {
        private final RequestPolicy nextPolicy;

        private final RequestPolicyOptions options;

        private RequestIDPolicy(RequestPolicy nextPolicy, RequestPolicyOptions options) {
            this.nextPolicy = nextPolicy;
            this.options = options;
        }

        /**
         * Add the unique client request ID to the request.
         *
         * @param request
         *         the request to populate with the client request ID
         *
         * @return A {@link Single} representing the {@link HttpResponse} that will arrive asynchronously.
         */
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            request.headers().set(Constants.HeaderConstants.CLIENT_REQUEST_ID_HEADER, UUID.randomUUID().toString());
            return nextPolicy.sendAsync(request);
        }
    }
}
