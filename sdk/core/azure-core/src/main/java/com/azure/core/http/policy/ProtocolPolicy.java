// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

/**
 * The pipeline policy that adds a given protocol to each HttpRequest.
 */
public class ProtocolPolicy implements HttpPipelinePolicy {
    private final String protocol;
    private final boolean overwrite;
    private final ClientLogger logger = new ClientLogger(ProtocolPolicy.class);

    /**
     * Creates a new ProtocolPolicy.
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
        final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
        if (overwrite || urlBuilder.getScheme() == null) {
            logger.info("Setting protocol to {}", protocol);

            try {
                context.getHttpRequest().setUrl(urlBuilder.setScheme(protocol).toUrl());
            } catch (MalformedURLException e) {
                return Mono.error(new RuntimeException(
                    String.format("Failed to set the HTTP request protocol to %d.", protocol), e));
            }
        }
        return next.process();
    }
}
