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

import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;

/**
 * Anonymous credentials are to be used with with HTTP(S) requests
 * that read blobs from public containers or requests that use a
 * Shared Access Signature (SAS).
 */
public final class AnonymousCredentials implements ICredentials {

    /**
     * Returns an empty instance of {@code AnonymousCredentials}.
     */
    public AnonymousCredentials(){}

    /**
     * Creates a new {@code AnonymousCredentialsPolicy}.
     *
     * @param nextRequestPolicy
     *      The next {@link RequestPolicy} in the pipeline which will be called after this policy completes.
     * @param options
     *      Unused.
     * @return
     *      A {@link RequestPolicy} object to be inserted into the {@link HttpPipeline}.
     */
    @Override
    public RequestPolicy create(RequestPolicy nextRequestPolicy, RequestPolicyOptions options) {
        return new AnonymousCredentialsPolicy(nextRequestPolicy);
    }

    /**
     * Anonymous credentials are to be used with with HTTP(S) requests
     * that read blobs from public containers or requests that use a
     * Shared Access Signature (SAS).
     */
    private final class AnonymousCredentialsPolicy implements RequestPolicy {
        final RequestPolicy nextPolicy;

        AnonymousCredentialsPolicy(RequestPolicy nextPolicy) {
            this.nextPolicy = nextPolicy;
        }

        /**
         * For anonymous credentials, this is effectively a no-op.
         *
         * @param request
         *      An {@link HttpRequest} object representing the storage request.
         * @return
         *      A Single containing the {@link HttpResponse} if successful.
         */
        public Single<HttpResponse> sendAsync(HttpRequest request) { return nextPolicy.sendAsync(request); }
    }
}