// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.HttpPipelineCallContext;
import com.generic.core.http.HttpPipelineNextSyncPolicy;
import com.generic.core.http.HttpResponse;
import com.generic.core.implementation.ImplUtils;
import com.generic.core.util.url.UrlBuilder;
import com.generic.core.util.logging.ClientLogger;

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

            final UrlBuilder urlBuilder = ImplUtils.parseUrl(context.getHttpRequest().getUrl(), true);
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
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return inner.processSync(context, next);
    }
}
