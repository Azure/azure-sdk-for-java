// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.retry;

import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

/**
 * Common retry properties for all Azure SDKs.
 */
public class RetryProperties implements RetryOptionsProvider.RetryOptions {

    /**
     * The mode for retry backoff.
     */
    private RetryOptionsProvider.RetryMode mode = RetryOptionsProvider.RetryMode.EXPONENTIAL;

    private final FixedRetryProperties fixed = new FixedRetryProperties();

    private final ExponentialRetryProperties exponential = new ExponentialRetryProperties();


    @Override
    public RetryOptionsProvider.RetryMode getMode() {
        return mode;
    }

    /**
     * Set the mode for retry backoff.
     *
     * @param mode the mode for retry backoff.
     */
    public void setMode(RetryOptionsProvider.RetryMode mode) {
        this.mode = mode;
    }

    @Override
    public FixedRetryProperties getFixed() {
        return fixed;
    }

    @Override
    public ExponentialRetryProperties getExponential() {
        return exponential;
    }

}
