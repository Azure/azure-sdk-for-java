// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.HttpPipelineCallContext;
import com.generic.core.http.HttpPipelineNextSyncPolicy;
import com.generic.core.http.HttpResponse;
import com.generic.core.util.url.UrlBuilder;
import com.generic.core.util.logging.ClientLogger;

import java.net.MalformedURLException;

/**
 * The pipeline policy that adds a given protocol to each HttpRequest.
 */
public class ProtocolPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(ProtocolPolicy.class);
    private final String protocol;
    private final boolean overwrite;

    private final HttpPipelineSyncPolicy inner = new HttpPipelineSyncPolicy() {
        @Override
        protected void beforeSendingRequest(HttpPipelineCallContext context) {
            final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
            if (overwrite || urlBuilder.getScheme() == null) {
                LOGGER.atVerbose()
                    .addKeyValue("protocol", protocol)
                    .log("Setting protocol");

                try {
                    context.getHttpRequest().setUrl(urlBuilder.setScheme(protocol).toUrl());
                } catch (MalformedURLException e) {
                    throw LOGGER.logExceptionAsError(new RuntimeException("Failed to set the HTTP request protocol to " + protocol + ".",
                        e));
                }
            }
        }
    };

    /**
     * Creates a new ProtocolPolicy.
     *
     * @param protocol The protocol to set.
     * @param overwrite Whether to overwrite a HttpRequest's protocol if it already has one.
     */
    public ProtocolPolicy(String protocol, boolean overwrite) {
        this.protocol = protocol;
        this.overwrite = overwrite;
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return inner.processSync(context, next);
    }
}
