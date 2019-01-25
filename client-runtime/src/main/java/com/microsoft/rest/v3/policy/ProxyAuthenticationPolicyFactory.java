/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.policy;

import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.util.Base64Util;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

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
        public Mono<HttpResponse> sendAsync(HttpRequest request) {
            String auth = username + ":" + password;
            String encodedAuth = Base64Util.encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            request.withHeader("Proxy-Authentication", "Basic " + encodedAuth);
            return next.sendAsync(request);
        }
    }
}
