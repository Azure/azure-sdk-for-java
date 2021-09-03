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
 * The API Management Policy.
 */
public final class ApimPolicy implements HttpPipelinePolicy {

    private final String apimEndpoint;

    /**
     * Creates an instance of the APIM policy.
     *
     * @param apimEndpoint The APIM endpoint.
     */
    public ApimPolicy(String apimEndpoint) {
        this.apimEndpoint = apimEndpoint;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        URL url = context.getHttpRequest().getUrl();
        String apimUrl = apimEndpoint;
        String path = url.getPath();
        if (!CoreUtils.isNullOrEmpty(path)) {
            apimUrl += path;
        }
        String query = url.getQuery();
        if (!CoreUtils.isNullOrEmpty(query)) {
            apimUrl += "?" + query;
        }

        HttpRequest requestCopy = context.getHttpRequest().copy();
        context.setHttpRequest(requestCopy.setUrl(apimUrl));
        return next.clone().process();
    }
}
