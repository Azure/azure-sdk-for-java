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
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
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
 * @see HttpPipelinePolicy
 * @see HttpPipeline
 * @see HttpRequest
 * @see Response
 */
public class HostPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(HostPolicy.class);

    private final String host;

    /**
     * Create HostPolicy.
     *
     * @param host The host to set on every HttpRequest.
     */
    public HostPolicy(String host) {
        this.host = host;
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        LOGGER.atVerbose().addKeyValue("host", host).log("Setting host");

        final UrlBuilder urlBuilder = UrlBuilder.parse(httpRequest.getUrl());
        try {
            httpRequest.setUrl(urlBuilder.setHost(host).toUrl());
        } catch (MalformedURLException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(String.format("Host URL '%s' is invalid.", host), e));
        }
        return next.process();
    }
}
