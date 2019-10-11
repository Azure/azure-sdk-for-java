// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.http.UrlBuilder;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The Pipeline policy that adds a service api version to HTTP requests.
 */
public class ServiceVersionPolicy implements HttpPipelinePolicy {
    private final String apiVersion;

    /**
     * Adds service api version to outgoing requests.
     *
     * @param apiVersion The api version to add to outgoing requests.
     */
    public ServiceVersionPolicy(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
        try {
            context.getHttpRequest().setUrl(urlBuilder.setQuery(apiVersion).toURL());
        } catch (MalformedURLException e) {
            return Mono.error(e);
        }
        return next.process();
    }
}
