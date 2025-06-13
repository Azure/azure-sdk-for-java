// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.pipeline;

/**
 * This class is exactly the same as {@link HttpPipelinePolicy} but exists to provide a standard parent class for all
 * credential policies and to differentiate them from other forms of {@link HttpPipelinePolicy}s.
 */
public abstract class HttpCredentialPolicy implements HttpPipelinePolicy {
    /**
     * Constant used to confiure authentication metadata in Request Context.
     */
    public static final String IO_CLIENTCORE_AUTH_METADATA = "IO_CLIENTCORE_AUTH_METADATA";

    /**
     * Creates an instance of {@link HttpCredentialPolicy}.
     */
    public HttpCredentialPolicy() {
    }

    @Override
    public final HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.AUTHENTICATION;
    }
}
