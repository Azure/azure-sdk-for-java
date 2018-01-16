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
 * Creates a RequestPolicy that adds basic proxy authentication to outgoing HTTP requests.
 */
public class ProxyAuthenticationPolicyFactory implements RequestPolicyFactory {
    private final String username;
    private final String password;

    /**
     * Creates a ProxyAuthenticationPolicyFactory.
     * @param username The username for authentication.
     * @param password The password for authentication.
     */
    public ProxyAuthenticationPolicyFactory(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new ProxyAuthenticationPolicy(next);
    }

    private final class ProxyAuthenticationPolicy implements RequestPolicy {
        private final RequestPolicy next;

        private ProxyAuthenticationPolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            String auth = username + ":" + password;
            String encodedAuth = BaseEncoding.base64().encode(auth.getBytes(Charsets.UTF_8));
            request.withHeader("Proxy-Authentication", "Basic " + encodedAuth);
            return next.sendAsync(request);
        }
    }
}
