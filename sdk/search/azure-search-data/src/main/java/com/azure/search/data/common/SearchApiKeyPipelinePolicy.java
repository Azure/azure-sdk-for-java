// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.common;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.logging.ClientLogger;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

public class SearchApiKeyPipelinePolicy implements HttpPipelinePolicy {

    private final String apiKey;
    private final ClientLogger logger = new ClientLogger(SearchApiKeyPipelinePolicy.class);

    /**
     * Constrcutor
     * @param apiKey Search Service api admin or query key
     * @throws IllegalArgumentException when the api key is an empty string
     */
    public SearchApiKeyPipelinePolicy(String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Empty apiKey"));
        }
        this.apiKey = apiKey;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.httpRequest().header("api-key", this.apiKey);
        return next.process();
    }
}
