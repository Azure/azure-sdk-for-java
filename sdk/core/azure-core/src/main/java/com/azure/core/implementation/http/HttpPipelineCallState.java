// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Contexts;
import com.azure.core.util.logging.ClientLogger;

/**
 * Represents a class responsible for maintaining information related to request-specific context and pipeline data.
 */
public class HttpPipelineCallState {
    private static final ClientLogger LOGGER = new ClientLogger(HttpPipelineCallState.class);
    private final HttpPipeline pipeline;
    private final HttpPipelineCallContext callContext;
    private int currentPolicyIndex;

    /**
     * Constructor to create HttpPipelineCallState.
     *
     * @param pipeline {@link HttpPipeline} to use for sending service requests and receiving responses.
     * @param callContext The request context.
     */
    public HttpPipelineCallState(HttpPipeline pipeline, HttpPipelineCallContext callContext) {
        this.pipeline = pipeline;
        this.callContext = callContext;
        this.currentPolicyIndex = -1;
    }

    /**
     * Retrieves the next policy on the pipeline.
     *
     * @return A {@link HttpPipelinePolicy} next in queue in the {@link HttpPipeline}.
     * @throws IllegalStateException when there are no more policies to execute.
     */
    public HttpPipelinePolicy getNextPolicy() {
        final int size = this.pipeline.getPolicyCount();

        this.currentPolicyIndex++;

        if (this.currentPolicyIndex > size) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("There is no more policies to execute."));
        } else if (this.currentPolicyIndex == size) {
            return null;
        } else {
            return this.pipeline.getPolicy(this.currentPolicyIndex);
        }
    }

    /**
     * Returns the current {@link HttpPipeline}.
     *
     * @return the current {@link HttpPipeline}.
     */
    public HttpPipeline getPipeline() {
        return this.pipeline;
    }

    /**
     * Retrieves the current policy in the pipeline.
     *
     * @return The current {@link HttpPipelinePolicy} in queue in the {@link HttpPipeline}.
     */
    public HttpPipelinePolicy getCurrentPolicy() {
        return this.pipeline.getPolicy(this.currentPolicyIndex);
    }

    /**
     * Returns the current request specific contextual data.
     *
     * @return the current request specific contextual data.
     */
    public HttpPipelineCallContext getCallContext() {
        return this.callContext;
    }

    /**
     * Gets the error for a request whose access token is bound to a TLS client certificate that the pipeline's HTTP
     * client can't present, so that the request fails before it's sent.
     *
     * @return The error, or null if the HTTP client can send the request.
     */
    public IllegalStateException getClientCertificateBindingError() {
        HttpClient httpClient = this.pipeline.getHttpClient();
        if (httpClient.supportsClientCertificateBinding()
            || Contexts.with(this.callContext.getContext()).getClientCertificateBinding() == null) {
            return null;
        }

        return new IllegalStateException("The request's access token is bound to a TLS client certificate (mTLS "
            + "proof-of-possession), but the HTTP client '" + httpClient.getClass().getName() + "' can't present the "
            + "certificate, so the request wasn't sent. Use an HTTP client that supports certificate binding, such as "
            + "the one in azure-core-http-netty, or turn off mTLS proof-of-possession in the credential.");
    }

    @Override
    public HttpPipelineCallState clone() {
        HttpPipelineCallState cloned = new HttpPipelineCallState(this.pipeline, this.callContext);
        cloned.currentPolicyIndex = this.currentPolicyIndex;
        return cloned;
    }
}
