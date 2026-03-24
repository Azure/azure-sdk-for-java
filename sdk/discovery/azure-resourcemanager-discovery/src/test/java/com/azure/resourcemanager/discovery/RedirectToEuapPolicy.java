// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * HTTP pipeline policy that redirects requests to the EUAP endpoint.
 * This is required because the Discovery API 2026-02-01-preview is only available
 * in the EUAP environment.
 */
public class RedirectToEuapPolicy implements HttpPipelinePolicy {
    private final String euapEndpoint;

    public RedirectToEuapPolicy(String euapEndpoint) {
        this.euapEndpoint = euapEndpoint;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        redirectRequest(context.getHttpRequest());
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        redirectRequest(context.getHttpRequest());
        return next.processSync();
    }

    private void redirectRequest(HttpRequest request) {
        URL originalUrl = request.getUrl();
        String host = originalUrl.getHost();

        // Replace management.azure.com with EUAP endpoint
        if ("management.azure.com".equals(host) || host.endsWith(".management.azure.com")) {
            try {
                URL euapUrl = new URL(euapEndpoint);
                UrlBuilder builder = UrlBuilder.parse(originalUrl);
                builder.setHost(euapUrl.getHost());
                request.setUrl(builder.toUrl());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to redirect request to EUAP endpoint", e);
            }
        }
    }
}
