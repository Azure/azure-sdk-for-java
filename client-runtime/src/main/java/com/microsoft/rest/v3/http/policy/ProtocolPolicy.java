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
 * The Pipeline policy that adds a given protocol to each HttpRequest.
 */
public class ProtocolPolicy implements HttpPipelinePolicy {
    private final String protocol;
    private final boolean overwrite;
    private final HttpPipelineOptions options;

    /**
     * Create a new ProtocolPolicy.
     *
     * @param protocol The protocol to set on every HttpRequest.
     */
    public ProtocolPolicy(String protocol) {
        this(protocol, true, new HttpPipelineOptions(null));
    }

    /**
     * Create a new ProtocolPolicy.
     *
     * @param protocol The protocol to set.
     * @param overwrite Whether or not to overwrite a HttpRequest's protocol if it already has one.
     * @param options the request options
     */
    public ProtocolPolicy(String protocol, boolean overwrite, HttpPipelineOptions options) {
        this.protocol = protocol;
        this.overwrite = overwrite;
        this.options = options;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, NextPolicy next) {
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.httpRequest().url());
        if (overwrite || urlBuilder.scheme() == null) {
            if (options.shouldLog(HttpPipelineLogLevel.INFO)) {
                options.log(HttpPipelineLogLevel.INFO, "Setting protocol to {0}", protocol);
            }

            try {
                context.httpRequest().withUrl(urlBuilder.withScheme(protocol).toURL());
            } catch (MalformedURLException e) {
                return Mono.error(e);
            }
        }
        return next.process();
    }
}