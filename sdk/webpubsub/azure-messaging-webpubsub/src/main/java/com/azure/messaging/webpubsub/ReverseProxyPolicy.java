// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

import java.net.URL;

/**
 * The reverse proxy policy.
 */
public final class ReverseProxyPolicy implements HttpPipelinePolicy {

    private final String reverseProxyEndpoint;

    /**
     * Creates an instance of the reverse proxy policy.
     *
     * @param reverseProxyEndpoint The reverse proxy endpoint.
     */
    public ReverseProxyPolicy(String reverseProxyEndpoint) {
        this.reverseProxyEndpoint = reverseProxyEndpoint;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        URL url = context.getHttpRequest().getUrl();
        String reverseProxyUrl = reverseProxyEndpoint;
        String path = url.getPath();
        if (!CoreUtils.isNullOrEmpty(path)) {
            reverseProxyUrl += path;
        }
        String query = url.getQuery();
        if (!CoreUtils.isNullOrEmpty(query)) {
            reverseProxyUrl += "?" + query;
        }

        HttpRequest requestCopy = context.getHttpRequest().copy();
        context.setHttpRequest(requestCopy.setUrl(reverseProxyUrl));
        return next.clone().process();
    }
}
