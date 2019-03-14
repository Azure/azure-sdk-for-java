/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.http.policy;

import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * Pipeline policy that adds 'User-Agent' header to a request.
 */
public class UserAgentPolicy implements HttpPipelinePolicy {
    private static final String DEFAULT_USER_AGENT_HEADER = "AutoRest-Java";
    private final String userAgent;

    /**
     * Creates UserAgentPolicy.
     *
     * @param userAgent The user agent string to add to request headers.
     */
    public UserAgentPolicy(String userAgent) {
        if (userAgent != null) {
            this.userAgent = userAgent;
        } else {
            this.userAgent = DEFAULT_USER_AGENT_HEADER;
        }
    }

    /**
     * Creates a {@link UserAgentPolicy} with a default user agent string.
     */
    public UserAgentPolicy() {
        this.userAgent = DEFAULT_USER_AGENT_HEADER;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String header = context.httpRequest().headers().value("User-Agent");
        if (header == null || DEFAULT_USER_AGENT_HEADER.equals(header)) {
            header = userAgent;
        } else {
            header = userAgent + " " + header;
        }
        context.httpRequest().headers().set("User-Agent", header);
        return next.process();
    }
}