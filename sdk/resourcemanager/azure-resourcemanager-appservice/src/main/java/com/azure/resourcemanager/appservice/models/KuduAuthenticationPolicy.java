// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Kudu web app authentication via basic auth
 */
public final class KuduAuthenticationPolicy implements HttpPipelinePolicy {
    private final WebAppBase webApp;
    private static final String HEADER_NAME = "Authorization";
    private String basicToken;

    /**
     * Constructor
     * @param webApp the web app
     */
    public KuduAuthenticationPolicy(WebAppBase webApp) {
        this.webApp = webApp;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        Mono<String> basicTokenMono =
            basicToken == null
                ? webApp
                    .getPublishingProfileAsync()
                    .map(
                        profile -> {
                            basicToken =
                                Base64
                                    .getEncoder()
                                    .encodeToString(
                                        (profile.gitUsername() + ":" + profile.gitPassword())
                                            .getBytes(StandardCharsets.UTF_8));
                            return basicToken;
                        })
                : Mono.just(basicToken);
        return basicTokenMono
            .flatMap(
                key -> {
                    context.getHttpRequest().setHeader(HEADER_NAME, "Basic " + basicToken);
                    return next.process();
                });
    }
}
