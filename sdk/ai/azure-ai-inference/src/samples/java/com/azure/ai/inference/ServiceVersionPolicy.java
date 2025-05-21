// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inference;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import reactor.core.publisher.Mono;

/**
 * This class is used to set the API version query parameter in the request URL.
 */
public class ServiceVersionPolicy implements HttpPipelinePolicy {

    private final String apiVersion;

    /**
     * Creates a new instance of {@link ServiceVersionPolicy}.
     * @param apiVersion The API version to be set in the request URL.
     */
    public ServiceVersionPolicy(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext, HttpPipelineNextPolicy httpPipelineNextPolicy) {
        // Get the original URL
        UrlBuilder urlBuilder = UrlBuilder.parse(httpPipelineCallContext.getHttpRequest().getUrl());

        // Update the URL with the API version
        urlBuilder.setQueryParameter("api-version", this.apiVersion);

        // Set the updated URL back to the request
        httpPipelineCallContext.getHttpRequest().setUrl(urlBuilder.toString());

        return httpPipelineNextPolicy.process();
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        // This needs to be updated only once per call and doesn't change for each retry.
        return HttpPipelinePosition.PER_CALL;
    }
}
