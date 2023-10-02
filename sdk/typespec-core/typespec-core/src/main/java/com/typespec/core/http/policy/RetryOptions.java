// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import java.util.Objects;

/**
 * The configuration for retries.
 */
public class RetryOptions {
    private final ExponentialBackoffOptions exponentialBackoffOptions;
    private final FixedDelayOptions fixedDelayOptions;

    /**
     * Creates a new instance that uses {@link ExponentialBackoffOptions}.
     *
     * @param exponentialBackoffOptions The {@link ExponentialBackoffOptions}.
     */
    public RetryOptions(ExponentialBackoffOptions exponentialBackoffOptions) {
        this.exponentialBackoffOptions = Objects.requireNonNull(
            exponentialBackoffOptions, "'exponentialBackoffOptions' cannot be null.");
        fixedDelayOptions = null;
    }

    /**
     * Creates a new instance that uses {@link FixedDelayOptions}.
     *
     * @param fixedDelayOptions The {@link FixedDelayOptions}.
     */
    public RetryOptions(FixedDelayOptions fixedDelayOptions) {
        this.fixedDelayOptions = Objects.requireNonNull(
            fixedDelayOptions, "'fixedDelayOptions' cannot be null.");
        exponentialBackoffOptions = null;
    }

    /**
     * Gets the configuration for exponential backoff if configured.
     *
     * @return The {@link ExponentialBackoffOptions}.
     */
    public ExponentialBackoffOptions getExponentialBackoffOptions() {
        return exponentialBackoffOptions;
    }

    /**
     * Gets the configuration for exponential backoff if configured.
     *
     * @return The {@link FixedDelayOptions}.
     */
    public FixedDelayOptions getFixedDelayOptions() {
        return fixedDelayOptions;
    }
}
