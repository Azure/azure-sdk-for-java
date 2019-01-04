/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.policy;

import com.microsoft.azure.v2.credentials.AsyncServiceClientCredentials;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import reactor.core.publisher.Mono;

/**
 * Creates a policy which adds credentials from AsyncServiceClientCredentials to a request.
 */
public class AsyncCredentialsPolicyFactory implements RequestPolicyFactory {
    private final AsyncServiceClientCredentials credentials;

    /**
     * Creates a Factory which produces CredentialsPolicy.
     * @param credentials The credentials to use for authentication.
     */
    public AsyncCredentialsPolicyFactory(AsyncServiceClientCredentials credentials) {
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
         * @return A reactor.core.publisher.Mono representing the pending response.
         */
        @Override
        public Mono<HttpResponse> sendAsync(HttpRequest request) {
                return credentials.authorizationHeaderValueAsync(request.url().toString())
                        .flatMap(token -> {
                            request.headers().set("Authorization", token);
                            return next.sendAsync(request);
                        });
        }
    }
}
