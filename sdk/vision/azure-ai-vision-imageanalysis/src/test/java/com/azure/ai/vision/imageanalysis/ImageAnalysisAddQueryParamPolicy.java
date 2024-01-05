// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.imageanalysis.tests;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Mono;

// Add a query parameter to the request URL, key=value.
public class ImageAnalysisAddQueryParamPolicy implements HttpPipelinePolicy {
    private final String key;
    private final String value;

    public ImageAnalysisAddQueryParamPolicy(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        HttpRequest request = context.getHttpRequest();
        String url = request.getUrl().toString();
        url += (url.contains("?") ? "&" : "?") + key + "=" + value;
        request.setUrl(url);
        return next.process();
    }
}
