// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.policy;

import com.client.core.http.HttpPipelineCallContext;
import com.client.core.http.HttpPipelineNextPolicy;
import com.client.core.http.HttpPipelineNextSyncPolicy;
import com.client.core.http.HttpRequest;
import com.client.core.http.HttpResponse;
import com.client.core.util.UrlBuilder;
import com.client.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

/**
 * The pipeline policy that adds a given port to each {@link HttpRequest}.
 */
public class PortPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(PortPolicy.class);

    private final int port;
    private final boolean overwrite;

    private final HttpPipelineSyncPolicy inner = new HttpPipelineSyncPolicy() {
        @Override
        protected void beforeSendingRequest(HttpPipelineCallContext context) {
            final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
            if (overwrite || urlBuilder.getPort() == null) {
                LOGGER.atVerbose()
                    .addKeyValue("port", port)
                    .log("Changing host");

                try {
                    context.getHttpRequest().setUrl(urlBuilder.setPort(port).toUrl());
                } catch (MalformedURLException e) {
                    throw LOGGER.logExceptionAsError(new
                        RuntimeException("Failed to set the HTTP request port to " + port + ".", e));
                }
            }
        }
    };

    /**
     * Creates a new PortPolicy object.
     *
     * @param port The port to set.
     * @param overwrite Whether to overwrite a {@link HttpRequest HttpRequest's} port if it already has one.
     */
    public PortPolicy(int port, boolean overwrite) {
        this.port = port;
        this.overwrite = overwrite;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return inner.process(context, next);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return inner.processSync(context, next);
    }
}
