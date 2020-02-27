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
 * Adds a custom query string to the URL.
 */
public class CustomQueryPipelinePolicy implements HttpPipelinePolicy {

    private final String paramKey;
    private final String paramValue;

    /**
     * Constructor for {@link CustomQueryPipelinePolicy}.
     *
     * @param paramKey Name of the query parameter to add.
     * @param paramValue Value of the query parameter being added.
     */
    public CustomQueryPipelinePolicy(String paramKey, String paramValue) {
        this.paramKey = Objects.requireNonNull(paramKey);
        this.paramValue = Objects.requireNonNull(paramValue);
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
