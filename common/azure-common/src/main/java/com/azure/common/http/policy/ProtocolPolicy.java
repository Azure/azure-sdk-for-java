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
 * The Pipeline policy that adds a given protocol to each HttpRequest.
 */
public class ProtocolPolicy implements HttpPipelinePolicy {
    private final String protocol;
    private final boolean overwrite;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolPolicy.class);

    /**
     * Create a new ProtocolPolicy.
     *
     * @param protocol The protocol to set.
     * @param overwrite Whether or not to overwrite a HttpRequest's protocol if it already has one.
     */
    public ProtocolPolicy(String protocol, boolean overwrite) {
        this.protocol = protocol;
        this.overwrite = overwrite;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.httpRequest().url());
        if (overwrite || urlBuilder.scheme() == null) {
            LOGGER.info("Setting protocol to {0}", protocol);

            try {
                context.httpRequest().withUrl(urlBuilder.withScheme(protocol).toURL());
            } catch (MalformedURLException e) {
                return Mono.error(e);
            }
        }
        return next.process();
    }
}
