// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Policy that adds the Cognitive Search Service api-key into the request's Authorization header.
 */
public final class SearchApiKeyPipelinePolicy implements HttpPipelinePolicy {
    private static final String API_KEY = "api-key";

    private final SearchApiKeyCredential apiKey;

    /**
     * Constructor
     *
     * @param apiKey Azure Cognitive Search service api admin or query key
     * @throws IllegalArgumentException when the api key is an empty string
     */
    public SearchApiKeyPipelinePolicy(SearchApiKeyCredential apiKey) {
        Objects.requireNonNull(apiKey, "'apiKey' cannot be null.");
        this.apiKey = apiKey;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if ("http".equals(context.getHttpRequest().getUrl().getProtocol())) {
            return Mono.error(new RuntimeException("Search api-key credentials require a URL using the HTTPS protocol "
                + "scheme."));
        }
        context.getHttpRequest().setHeader(API_KEY, this.apiKey.getApiKey());
        return next.process();
    }
}
