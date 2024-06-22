// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.builder;

import io.clientcore.core.http.models.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * This class contains utility methods useful for client builders.
 */
public final class ClientBuilderUtil {
    private ClientBuilderUtil() {
    }

    private static final ClientLogger LOGGER = new ClientLogger(ClientBuilderUtil.class);
    private static final HttpRetryPolicy DEFAULT_RETRY_POLICY = new HttpRetryPolicy();

    /**
     * This method validates that customized {@link HttpPipelinePolicy retry policy} and customized {@link HttpRetryOptions}
     * are mutually exclusive.
     * If no customization was made then it falls back to the default.
     * @param retryPolicy a customized {@link HttpPipelinePolicy}.
     * @param retryOptions a customized {@link HttpRetryOptions}.
     * @return final {@link HttpRetryPolicy} to be used by the builder.
     * @throws IllegalStateException if both {@code retryPolicy} and {@code retryOptions} are not {@code null}.
     */
    public static HttpPipelinePolicy validateAndGetRetryPolicy(HttpPipelinePolicy retryPolicy,
        HttpRetryOptions retryOptions) {
        return validateAndGetRetryPolicy(retryPolicy, retryOptions, DEFAULT_RETRY_POLICY);
    }

    /**
     * This method validates that customized {@link HttpPipelinePolicy retry policy} and customized {@link HttpRetryOptions}
     * are mutually exclusive.
     * If no customization was made then it falls back to the default.
     * @param retryPolicy a customized {@link HttpPipelinePolicy}.
     * @param retryOptions a customized {@link HttpRetryOptions}.
     * @param defaultPolicy a default {@link HttpPipelinePolicy}.
     * @return final {@link HttpRetryPolicy} to be used by the builder.
     * @throws NullPointerException if {@code defaultPolicy} is {@code null}.
     * @throws IllegalStateException if both {@code retryPolicy} and {@code retryOptions} are not {@code null}.
     */
    public static HttpPipelinePolicy validateAndGetRetryPolicy(HttpPipelinePolicy retryPolicy,
        HttpRetryOptions retryOptions, HttpPipelinePolicy defaultPolicy) {
        Objects.requireNonNull(defaultPolicy, "'defaultPolicy' cannot be null.");
        if (retryPolicy != null && retryOptions != null) {
            throw LOGGER.logExceptionAsWarning(
                new IllegalStateException("'retryPolicy' and 'retryOptions' cannot both be set"));
        }
        if (retryPolicy != null) {
            return retryPolicy;
        } else if (retryOptions != null) {
            return new HttpRetryPolicy(retryOptions);
        } else {
            return defaultPolicy;
        }
    }
}
