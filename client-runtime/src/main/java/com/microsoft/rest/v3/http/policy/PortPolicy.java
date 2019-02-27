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
import com.microsoft.rest.v3.http.NextPolicy;
import com.microsoft.rest.v3.http.UrlBuilder;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

/**
 * The Pipeline policy that adds a given port to each HttpRequest.
 */
public class PortPolicy implements HttpPipelinePolicy {
    private final int port;
    private final boolean overwrite;
    private final HttpPipelineOptions options;

    /**
     * Create a new PortPolicy object.
     *
     * @param port The port to set on every HttpRequest.
     */
    public PortPolicy(int port) {
        this(port, true, new HttpPipelineOptions(null));
    }

    /**
     * Create a new PortPolicy object.
     *
     * @param port The port to set.
     * @param overwrite Whether or not to overwrite a HttpRequest's port if it already has one.
     * @param options the request options
     */
    public PortPolicy(int port, boolean overwrite, HttpPipelineOptions options) {
        this.port = port;
        this.overwrite = overwrite;
        this.options = options;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, NextPolicy next) {
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.httpRequest().url());
        if (overwrite || urlBuilder.port() == null) {
            if (options.shouldLog(HttpPipelineLogLevel.INFO)) {
                options.log(HttpPipelineLogLevel.INFO, "Changing port to {0}", port);
            }

            try {
                context.httpRequest().withUrl(urlBuilder.withPort(port).toURL());
            } catch (MalformedURLException e) {
                return Mono.error(e);
            }
        }
        return next.process();
    }
}
