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
 * The {@code ProtocolPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy is
 * used to add a specific protocol to each {@code HttpRequest}.
 *
 * <p>This class is useful when you need to set a specific protocol for all requests in a pipeline. It ensures that the
 * protocol is set correctly for each request.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code ProtocolPolicy} is created with a protocol of "https" and an overwrite flag set to
 * true. The policy can then be added to the pipeline. Once added to the pipeline, requests have their protocol set to
 * "https" by the {@code ProtocolPolicy}.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.ProtocolPolicy.constructor -->
 * <pre>
 * ProtocolPolicy protocolPolicy = new ProtocolPolicy&#40;&quot;https&quot;, true&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.ProtocolPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
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
                LOGGER.atVerbose().addKeyValue("protocol", protocol).log("Setting protocol");

                try {
                    context.getHttpRequest().setUrl(urlBuilder.setScheme(protocol).toUrl());
                } catch (MalformedURLException e) {
                    throw LOGGER.logExceptionAsError(
                        new RuntimeException("Failed to set the HTTP request protocol to " + protocol + ".", e));
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
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return inner.process(context, next);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        return inner.processSync(context, next);
    }
}
