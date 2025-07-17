// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpPipelineCallState;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;

/**
 * A type that invokes next policy in the pipeline.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public class HttpPipelineNextPolicy {
    private final HttpPipelineCallState state;

    /**
     * Package-private constructor. Creates an HttpPipelineNextPolicy instance.
     *
     * @param state The pipeline call state.
     */
    HttpPipelineNextPolicy(HttpPipelineCallState state) {
        this.state = state;
    }

    /**
     * Invokes the next {@link HttpPipelinePolicy}.
     *
     * @return The response.
     * @throws CoreException If an error occurs when sending the request or receiving the response.
     */
    public Response<BinaryData> process() {
        HttpPipelinePolicy nextPolicy = state.getNextPolicy();

        if (nextPolicy == null) {
            return this.state.getPipeline().getHttpClient().send(this.state.getHttpRequest());
        } else {
            return nextPolicy.process(this.state.getHttpRequest(), this);
        }
    }

    /**
     * Copies the current state of the {@link HttpPipelineNextPolicy}.
     * <p>
     * This method must be used when a re-request is made in the pipeline.
     *
     * @return A new instance of this next pipeline policy.
     */
    public HttpPipelineNextPolicy copy() {
        return new HttpPipelineNextPolicy(this.state.copy());
    }
}
