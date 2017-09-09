/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;

/**
 * User agent interceptor for putting a 'User-Agent' header in the request.
 */
public final class UserAgentPolicy implements RequestPolicy {
    private static final String DEFAULT_USER_AGENT_HEADER = "AutoRest-Java";

    public static class Factory implements RequestPolicy.Factory {
        private final String userAgent;

        public Factory(String userAgent) {
            this.userAgent = userAgent;
        }

        public Factory() {
            this.userAgent = DEFAULT_USER_AGENT_HEADER;
        }

        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new UserAgentPolicy(userAgent, next);
        }
    }

    private final RequestPolicy next;
    private final String userAgent;

    /**
     * Initialize an instance of {@link UserAgentPolicy} class with the default
     * 'User-Agent' header.
     */
    public UserAgentPolicy(String userAgent, RequestPolicy next) {
        this.userAgent = userAgent;
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
        request.headers().overwrite("User-Agent", header);

        return next.sendAsync(request);
    }
}
