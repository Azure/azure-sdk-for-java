// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

/**
 * Function app authentication via x-functions-key
 */
public final class FunctionAuthenticationPolicy implements HttpPipelinePolicy {
    private final FunctionApp functionApp;
    private static final String HEADER_NAME = "x-functions-key";
    private String masterKey;

    /**
     * Constructor
     * @param functionApp the function app
     */
    public FunctionAuthenticationPolicy(FunctionApp functionApp) {
        this.functionApp = functionApp;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        Mono<String> masterKeyMono =
            masterKey == null
                ? functionApp
                    .getMasterKeyAsync()
                    .map(
                        key -> {
                            masterKey = key;
                            return key;
                        })
                : Mono.just(masterKey);
        return masterKeyMono
            .flatMap(
                key -> {
                    context.getHttpRequest().setHeader(HEADER_NAME, key);
                    return next.process();
                });
    }
}
