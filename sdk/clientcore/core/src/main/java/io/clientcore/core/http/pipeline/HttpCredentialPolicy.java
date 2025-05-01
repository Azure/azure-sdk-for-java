// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

/**
 * This class is exactly the same as {@link HttpPipelinePolicy} but exists to provide a standard parent class for all
 * credential policies and to differentiate them from other forms of {@link HttpPipelinePolicy}s.
 */
public abstract class HttpCredentialPolicy implements HttpPipelinePolicy {
    /**
     * Creates an instance of {@link HttpCredentialPolicy}.
     */
    public HttpCredentialPolicy() {
    }

    /**
     * Applies the credential to the request.
     *
     * @param httpRequest The HTTP request to apply the credential to.
     * @param next The next policy in the pipeline.
     * @return The response from the next policy in the pipeline.
     * @throws IllegalStateException If the request is not using the HTTPS scheme.
     */
    @Override
    public abstract Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next);

    @Override
    public final HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.AUTHENTICATION;
    }
}
