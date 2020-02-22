// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Injects a custom query string the URL before the request is initiated
 */
public class CustomQueryPipelinePolicy implements HttpPipelinePolicy {

    private final String paramKey;
    private final String paramValue;

    public CustomQueryPipelinePolicy(String paramKey, String paramValue) {
        Objects.requireNonNull(paramKey);
        Objects.requireNonNull(paramValue);

        this.paramKey = paramKey;
        this.paramValue = paramValue;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String url = context.getHttpRequest().getUrl().toString();
        String separator = url.contains("?") ? "&" : "?";
        context.getHttpRequest()
            .setUrl(url + separator + this.paramKey + "=" + this.paramValue);
        return next.process();
    }
}
