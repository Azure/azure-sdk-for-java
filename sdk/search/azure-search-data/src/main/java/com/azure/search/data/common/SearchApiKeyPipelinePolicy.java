// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.common;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.search.data.common.credentials.ApiKeyCredentials;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class SearchApiKeyPipelinePolicy implements HttpPipelinePolicy {

    private final ApiKeyCredentials apiKey;

    /**
     * Constrcutor
     * @param apiKey Search Service api admin or query key
     * @throws IllegalArgumentException when the api key is an empty string
     */
    public SearchApiKeyPipelinePolicy(ApiKeyCredentials apiKey) {
        Objects.requireNonNull(apiKey);
        this.apiKey = apiKey;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.httpRequest().header("api-key", this.apiKey.getApiKey());
        return next.process();
    }
}
