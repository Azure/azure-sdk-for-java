/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.policy;

import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import rx.Single;

import java.io.IOException;

/**
 * Netty OutboundHandler to set authorization header.
 */
public class AzureTokenCredentialsPolicy implements RequestPolicy {
    public static class Factory implements RequestPolicy.Factory {
        private final AzureTokenCredentials credentials;

        public Factory(AzureTokenCredentials credentials) {
            this.credentials = credentials;
        }

        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new AzureTokenCredentialsPolicy(credentials, next);
        }
    }

    private final AzureTokenCredentials credentials;
    private final RequestPolicy next;

    /**
     * Creates AzureTokenCredentialsPolicy.
     *
     * @param credentials the credentials
     */
    private AzureTokenCredentialsPolicy(AzureTokenCredentials credentials, RequestPolicy next) {
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
        String token;
        try {
            token = credentials.getTokenFromUri(request.url());
        } catch (IOException e) {
            return Single.error(e);
        }

        request.headers().add("Authorization", "Bearer " + token);
        return next.sendAsync(request);
    }
}
