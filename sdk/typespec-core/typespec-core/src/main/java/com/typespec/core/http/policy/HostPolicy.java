// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.http.HttpPipelineCallContext;
import com.typespec.core.http.HttpPipelineNextPolicy;
import com.typespec.core.http.HttpPipelineNextSyncPolicy;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.util.UrlBuilder;
import com.typespec.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

/**
 * The pipeline policy that adds the given host to each HttpRequest.
 */
public class HostPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HostPolicy.class);

    private final String host;

    private final HttpPipelineSyncPolicy inner = new HttpPipelineSyncPolicy() {
        @Override
        protected void beforeSendingRequest(HttpPipelineCallContext context) {
            LOGGER.atVerbose()
                .addKeyValue("host", host)
                .log("Setting host");

            final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
            try {
                context.getHttpRequest().setUrl(urlBuilder.setHost(host).toUrl());
            } catch (MalformedURLException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(String.format("Host URL '%s' is invalid.", host),
                    e));
            }
        }
    };

    /**
     * Create HostPolicy.
     *
     * @param host The host to set on every HttpRequest.
     */
    public HostPolicy(String host) {
        this.host = host;
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
