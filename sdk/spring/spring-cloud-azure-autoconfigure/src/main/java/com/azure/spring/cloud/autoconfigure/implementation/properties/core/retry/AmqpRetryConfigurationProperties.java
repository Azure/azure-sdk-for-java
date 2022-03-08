// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.retry;

import com.azure.spring.cloud.autoconfigure.properties.core.retry.RetryConfigurationProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;

import java.time.Duration;

/**
 * Amqp based client related retry properties.
 */
public class AmqpRetryConfigurationProperties extends RetryConfigurationProperties implements RetryOptionsProvider.AmqpRetryOptions {

    /**
     * Amount of time to wait until a timeout.
     */
    private Duration tryTimeout;

    @Override
    public Duration getTryTimeout() {
        return tryTimeout;
    }

    public void setTryTimeout(Duration tryTimeout) {
        this.tryTimeout = tryTimeout;
    }
}
