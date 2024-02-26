// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

/**
 * The {@code HostPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy is used
 * to add a specific host to each HTTP request.
 *
 * <p>This class is useful when you need to set a specific host for all requests in a pipeline. It ensures that the
 * host is set correctly for each request.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code HostPolicy} is created with a host of "www.example.com". Once added to the pipeline,
 * all requests will have their host set to "www.example.com" by the {@code HostPolicy}.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.HostPolicy.constructor -->
 * <pre>
 * HostPolicy hostPolicy = new HostPolicy&#40;&quot;www.example.com&quot;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.HostPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 */
public class HostPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HostPolicy.class);

    private final String host;

    private final HttpPipelineSyncPolicy inner = new HttpPipelineSyncPolicy() {
        @Override
        protected void beforeSendingRequest(HttpPipelineCallContext context) {
            LOGGER.atVerbose().addKeyValue("host", host).log("Setting host");

            final UrlBuilder urlBuilder = UrlBuilder.parse(context.getHttpRequest().getUrl());
            try {
                context.getHttpRequest().setUrl(urlBuilder.setHost(host).toUrl());
            } catch (MalformedURLException e) {
                throw LOGGER
                    .logExceptionAsError(new RuntimeException(String.format("Host URL '%s' is invalid.", host), e));
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
