/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;

import java.util.UUID;

/**
 * Factory to create a unique request ID and set the x-ms-client-request-id header.
 */
public final class RequestIDFactory implements RequestPolicyFactory {

    private final class RequestIDPolicy implements RequestPolicy {
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
         *      the request to populate with the client request ID
         * @return
         *      A {@link Single} representing the {@link HttpResponse} that will arrive asynchronously.
         */
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            request.headers().set(Constants.HeaderConstants.CLIENT_REQUEST_ID_HEADER, UUID.randomUUID().toString());
            return nextPolicy.sendAsync(request);
        }
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new RequestIDPolicy(next, options);
    }
}
