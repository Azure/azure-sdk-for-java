// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

/**
 * The {@code PortPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy is used
 * to add a specific port to each {@link HttpRequest}.
 *
 * <p>This class is useful when you need to set a specific port for all requests in a pipeline. It ensures that the
 * port is set correctly for each request.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code PortPolicy} is created with a port of 8080 and an overwrite flag set to true. The
 * policy can then be added to the pipeline. Once added to the pipeline, all requests will have their port set to 8080
 * by the {@code PortPolicy}.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.PortPolicy.constructor -->
 * <pre>
 * PortPolicy portPolicy = new PortPolicy&#40;8080, true&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.PortPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
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
                LOGGER.atVerbose().addKeyValue("port", port).log("Changing host");

                try {
                    context.getHttpRequest().setUrl(urlBuilder.setPort(port).toUrl());
                } catch (MalformedURLException e) {
                    throw LOGGER.logExceptionAsError(
                        new RuntimeException("Failed to set the HTTP request port to " + port + ".", e));
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
