// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.retry;

import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Retry properties.
 */
public class RetryConfigurationProperties implements RetryOptionsProvider.RetryOptions {

    /**
     * The retry backoff mode when retrying. Supported types are: FIXED, EXPONENTIAL.
     */
    private RetryOptionsProvider.RetryMode mode;

    @NestedConfigurationProperty
    private final ExponentialRetryConfigurationProperties exponential = new ExponentialRetryConfigurationProperties();

    @NestedConfigurationProperty
    private final FixedRetryConfigurationProperties fixed = new FixedRetryConfigurationProperties();


    @Override
    public RetryOptionsProvider.RetryMode getMode() {
        return mode;
    }

    public void setMode(RetryOptionsProvider.RetryMode mode) {
        this.mode = mode;
    }

    @Override
    public ExponentialRetryConfigurationProperties getExponential() {
        return exponential;
    }

    @Override
    public FixedRetryConfigurationProperties getFixed() {
        return fixed;
    }
}
