// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.converter;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.spring.core.properties.retry.BackoffProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.springframework.core.convert.converter.Converter;

import java.time.Duration;

/**
 * Converts a {@link RetryProperties} to a {@link AmqpRetryOptions}.
 */
public final class AzureAmqpRetryOptionsConverter implements Converter<RetryProperties, AmqpRetryOptions> {

    @Override
    public AmqpRetryOptions convert(RetryProperties retryProperties) {
        AmqpRetryOptions retryOptions = new AmqpRetryOptions();

        if (retryProperties.getMaxAttempts() != null) {
            retryOptions.setMaxRetries(retryProperties.getMaxAttempts());
        }

        if (retryProperties.getTimeout() != null) {
            retryOptions.setTryTimeout(Duration.ofMillis(retryProperties.getTimeout()));
        }

        AmqpRetryMode mode;
        final BackoffProperties backoffProperties = retryProperties.getBackoff();
        if (backoffProperties != null) {
            if (backoffProperties.getMultiplier() != null && backoffProperties.getMultiplier() > 0) {
                mode = AmqpRetryMode.EXPONENTIAL;
            } else {
                mode = AmqpRetryMode.FIXED;
            }
            retryOptions.setMode(mode);
            if (backoffProperties.getDelay() != null) {
                retryOptions.setDelay(Duration.ofMillis(backoffProperties.getDelay()));
            }
            if (backoffProperties.getMaxDelay() != null) {
                retryOptions.setMaxDelay(Duration.ofMillis(backoffProperties.getMaxDelay()));
            }
        }
        return retryOptions;
    }
}
