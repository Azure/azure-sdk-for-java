/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Single;

/**
 * Adds a 'User-Agent' header to a request.
 */
public final class UserAgentPolicyFactory implements RequestPolicyFactory {
    private static final String DEFAULT_USER_AGENT_HEADER = "AutoRest-Java";
    private final String userAgent;

    /**
     * Creates a {@link UserAgentPolicyFactory} with the given user agent string.
     * @param userAgent The user agent string to add to request headers.
     */
    public UserAgentPolicyFactory(String userAgent) {
        if (userAgent != null) {
            this.userAgent = userAgent;
        } else {
            this.userAgent = DEFAULT_USER_AGENT_HEADER;
        }
    }

    /**
     * Creates a {@link UserAgentPolicyFactory} with a default user agent string.
     */
    public UserAgentPolicyFactory() {
        this.userAgent = DEFAULT_USER_AGENT_HEADER;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new UserAgentPolicy(next);
    }

    private final class UserAgentPolicy implements RequestPolicy {
        private final RequestPolicy next;
        private UserAgentPolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            String header = request.headers().value("User-Agent");
            if (header == null || DEFAULT_USER_AGENT_HEADER.equals(header)) {
                header = userAgent;
            } else {
                header = userAgent + " " + header;
            }
            request.headers().set("User-Agent", header);

            return next.sendAsync(request);
        }
    }
}