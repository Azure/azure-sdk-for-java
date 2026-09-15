// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.net.URL;

/** Rejects untrusted request destinations before an authentication policy can attach a token. */
public final class AuthenticatedEndpointPolicy implements HttpPipelinePolicy {

    private final URL configuredEndpoint;

    public AuthenticatedEndpointPolicy(URL configuredEndpoint) {
        this.configuredEndpoint = configuredEndpoint;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        URL requestUrl = context.getHttpRequest().getUrl();
        if (!RedirectPolicyHelper.isTrustedRedirect(configuredEndpoint, requestUrl)) {
            return Mono.error(
                new IllegalArgumentException("Refusing to authenticate request to untrusted endpoint: " + requestUrl));
        }
        return next.process();
    }
}
