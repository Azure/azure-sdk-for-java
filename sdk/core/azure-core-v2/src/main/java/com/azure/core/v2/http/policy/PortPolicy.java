// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http.policy;

import io.clientcore.core.implementation.util.UrlBuilder;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
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
 * <!-- end com.azure.core.http.policy.PortPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
public class PortPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(PortPolicy.class);

    private final int port;
    private final boolean overwrite;

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
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        final UrlBuilder urlBuilder = UrlBuilder.parse(httpRequest.getUrl());
        if (overwrite || urlBuilder.getPort() == null) {
            LOGGER.atVerbose().addKeyValue("port", port).log("Changing host");

            try {
                httpRequest.setUrl(urlBuilder.setPort(port).toUrl());
            } catch (MalformedURLException e) {
                throw LOGGER.logThrowableAsError(
                    new RuntimeException("Failed to set the HTTP request port to " + port + ".", e));
            }
        }
        return next.process();
    }
}
