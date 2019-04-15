// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http.policy;

import com.azure.common.http.HttpPipelineCallContext;
import com.azure.common.http.HttpPipelineNextPolicy;
import com.azure.common.http.HttpResponse;
import com.azure.common.implementation.http.UrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

/**
 * The Pipeline policy that adds a given port to each HttpRequest.
 */
public class PortPolicy implements HttpPipelinePolicy {
    private final int port;
    private final boolean overwrite;
    private static final Logger LOGGER = LoggerFactory.getLogger(PortPolicy.class);

    /**
     * Create a new PortPolicy object.
     *
     * @param port The port to set.
     * @param overwrite Whether or not to overwrite a HttpRequest's port if it already has one.
     */
    public PortPolicy(int port, boolean overwrite) {
        this.port = port;
        this.overwrite = overwrite;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.httpRequest().url());
        if (overwrite || urlBuilder.port() == null) {
            LOGGER.info("Changing port to {0}", port);

            try {
                context.httpRequest().withUrl(urlBuilder.withPort(port).toURL());
            } catch (MalformedURLException e) {
                return Mono.error(e);
            }
        }
        return next.process();
    }
}
