/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.credentials.ServiceClientCredentials;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Single;

import java.io.IOException;

/**
 * Creates a policy which adds credentials from ServiceClientCredentials to a request.
 */
public class CredentialsPolicyFactory implements RequestPolicyFactory {
    private final ServiceClientCredentials credentials;

    /**
     * Creates a Factory which produces CredentialsPolicy.
     * @param credentials The credentials to use for authentication.
     */
    public CredentialsPolicyFactory(ServiceClientCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new CredentialsPolicy(next);
    }

    private final class CredentialsPolicy implements RequestPolicy {
        private final RequestPolicy next;
        private CredentialsPolicy(RequestPolicy next) {
            this.next = next;
        }

        /**
         * Adds a token header to the request, making another request to get the token if necessary.
         * @param request The HTTP request message to send.
         * @return An io.reactivex.Single representing the pending response.
         */
        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            try {
                String token = credentials.authorizationHeaderValue(request.url().toString());
                request.headers().set("Authorization", token);
                return next.sendAsync(request);
            } catch (IOException e) {
                return Single.error(e);
            }
        }
    }
}
