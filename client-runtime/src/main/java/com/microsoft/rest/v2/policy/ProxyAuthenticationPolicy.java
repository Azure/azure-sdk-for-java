/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Single;

/**
 * Adds basic proxy authentication to outgoing HTTP requests.
 */
public final class ProxyAuthenticationPolicy implements RequestPolicy {
    private final String username;
    private final String password;
    private final RequestPolicy next;

    private ProxyAuthenticationPolicy(String username, String password, RequestPolicy next) {
        this.username = username;
        this.password = password;
        this.next = next;
    }

    @Override
    public Single<HttpResponse> sendAsync(HttpRequest request) {
        String auth = username + ":" + password;
        String encodedAuth = BaseEncoding.base64().encode(auth.getBytes(Charsets.UTF_8));
        request.withHeader("Proxy-Authentication", "Basic " + encodedAuth);
        return next.sendAsync(request);
    }

    /**
     * Factory for creating ProxyAuthenticationPolicy.
     */
    public static class Factory implements RequestPolicy.Factory {
        private final String username;
        private final String password;

        /**
         * Creates a ProxyAuthenticationPolicy.Factory.
         * @param username The username for authentication.
         * @param password The password for authentication.
         */
        public Factory(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new ProxyAuthenticationPolicy(username, password, next);
        }
    }
}
