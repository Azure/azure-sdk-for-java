/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.credentials.ServiceClientCredentials;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;

import java.io.IOException;

/**
 * Adds credentials from ServiceClientCredentials to a request.
 */
public final class CredentialsPolicy implements RequestPolicy {
    /**
     * Factory which instantiates CredentialsPolicy.
     */
    public static class Factory implements RequestPolicy.Factory {
        private final ServiceClientCredentials credentials;

        /**
         * Creates a Factory which produces CredentialsPolicy.
         * @param credentials The credentials to use for authentication.
         */
        public Factory(ServiceClientCredentials credentials) {
            this.credentials = credentials;
        }

        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new CredentialsPolicy(credentials, next);
        }
    }

    private final ServiceClientCredentials credentials;
    private final RequestPolicy next;

    private CredentialsPolicy(ServiceClientCredentials credentials, RequestPolicy next) {
        this.credentials = credentials;
        this.next = next;
    }

    /**
     * Adds a token header to the request, making another request to get the token if necessary.
     * @param request The HTTP request message to send.
     * @return An rx.Single representing the pending response.
     */
    @Override
    public Single<HttpResponse> sendAsync(HttpRequest request) {
        try {
            String token = credentials.authorizationHeaderValue(request.url());
            request.headers().set("Authorization", token);
            return next.sendAsync(request);
        } catch (IOException e) {
            return Single.error(e);
        }
    }
}
