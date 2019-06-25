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

/**
 * This is a factory which creates policies in an {@link HttpPipeline} for setting the request property on the response
 * object. This is necessary because of a bug in autorest which fails to set this property. In most cases, it is
 * sufficient to allow the default pipeline to add this factory automatically and assume that it works. The factory and
 * policy must only be used directly when creating a custom pipeline.
 */
final class SetResponseFieldFactory implements RequestPolicyFactory {

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new SetResponseFieldPolicy(next);
    }

    private static final class SetResponseFieldPolicy implements RequestPolicy {
        private final RequestPolicy nextPolicy;

        private SetResponseFieldPolicy(RequestPolicy nextPolicy) {
            this.nextPolicy = nextPolicy;
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
            return nextPolicy.sendAsync(request)
                    .map(response ->
                            response.withRequest(request));
        }
    }
}
