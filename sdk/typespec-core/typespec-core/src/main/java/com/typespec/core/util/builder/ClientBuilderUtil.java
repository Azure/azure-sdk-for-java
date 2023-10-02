// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.builder;

import com.typespec.core.http.policy.HttpPipelinePolicy;
import com.typespec.core.http.policy.RetryOptions;
import com.typespec.core.http.policy.RetryPolicy;
import com.typespec.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * This class contains utility methods useful for client builders.
 */
public final class ClientBuilderUtil {
    private ClientBuilderUtil() { }

    private static final ClientLogger LOGGER = new ClientLogger(ClientBuilderUtil.class);
    private static final RetryPolicy DEFAULT_RETRY_POLICY = new RetryPolicy();

    /**
     * This method validates that customized {@link HttpPipelinePolicy retry policy} and customized {@link RetryOptions}
     * are mutually exclusive.
     * If no customization was made then it falls back to the default.
     * @param retryPolicy a customized {@link HttpPipelinePolicy}.
     * @param retryOptions a customized {@link RetryOptions}.
     * @return final {@link RetryPolicy} to be used by the builder.
     * @throws IllegalStateException if both {@code retryPolicy} and {@code retryOptions} are not {@code null}.
     */
    public static HttpPipelinePolicy validateAndGetRetryPolicy(
        HttpPipelinePolicy retryPolicy, RetryOptions retryOptions) {
        return validateAndGetRetryPolicy(retryPolicy, retryOptions, DEFAULT_RETRY_POLICY);
    }

    /**
     * This method validates that customized {@link HttpPipelinePolicy retry policy} and customized {@link RetryOptions}
     * are mutually exclusive.
     * If no customization was made then it falls back to the default.
     * @param retryPolicy a customized {@link HttpPipelinePolicy}.
     * @param retryOptions a customized {@link RetryOptions}.
     * @param defaultPolicy a default {@link HttpPipelinePolicy}.
     * @return final {@link RetryPolicy} to be used by the builder.
     * @throws NullPointerException if {@code defaultPolicy} is {@code null}.
     * @throws IllegalStateException if both {@code retryPolicy} and {@code retryOptions} are not {@code null}.
     */
    public static HttpPipelinePolicy validateAndGetRetryPolicy(
        HttpPipelinePolicy retryPolicy, RetryOptions retryOptions, HttpPipelinePolicy defaultPolicy) {
        Objects.requireNonNull(defaultPolicy, "'defaultPolicy' cannot be null.");
        if (retryPolicy != null && retryOptions != null) {
            throw LOGGER.logExceptionAsWarning(
                new IllegalStateException("'retryPolicy' and 'retryOptions' cannot both be set"));
        }
        if (retryPolicy != null) {
            return retryPolicy;
        } else if (retryOptions != null) {
            return new RetryPolicy(retryOptions);
        } else {
            return defaultPolicy;
        }
    }
}
