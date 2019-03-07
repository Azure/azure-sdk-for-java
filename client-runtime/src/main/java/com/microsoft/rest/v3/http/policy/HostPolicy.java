/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3.http.policy;

import com.microsoft.rest.v3.http.HttpPipelineCallContext;
import com.microsoft.rest.v3.http.HttpPipelineLogLevel;
import com.microsoft.rest.v3.http.HttpPipelineOptions;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.HttpPipelineNextPolicy;
import com.microsoft.rest.v3.http.UrlBuilder;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

/**
 * The Pipeline policy that adds the given host to each HttpRequest.
 */
public class HostPolicy implements HttpPipelinePolicy {
    private final String host;
    private final HttpPipelineOptions options;

    /**
     * Create HostPolicy.
     *
     * @param host The host to set on every HttpRequest.
     */
    public HostPolicy(String host) {
        this(host, new HttpPipelineOptions(null));
    }

    /**
     * Create HostPolicy.
     *
     * @param host The host to set on every HttpRequest.
     * @param options the request options
     */
    public HostPolicy(String host, HttpPipelineOptions options) {
        this.host = host;
        this.options = options;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (options.shouldLog(HttpPipelineLogLevel.INFO)) {
            options.log(HttpPipelineLogLevel.INFO, "Setting host to {0}", host);
        }

        Mono<HttpResponse> result;
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.httpRequest().url());
        try {
            context.httpRequest().withUrl(urlBuilder.withHost(host).toURL());
            result = next.process();
        } catch (MalformedURLException e) {
            result = Mono.error(e);
        }
        return result;
    }
}