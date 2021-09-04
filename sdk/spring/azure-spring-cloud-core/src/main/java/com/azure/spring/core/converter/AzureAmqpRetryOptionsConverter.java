// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.converter;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.springframework.core.convert.converter.Converter;

import java.time.Duration;

/**
 * Converts a {@link RetryProperties} to a {@link AmqpRetryOptions}
 */
public final class AzureAmqpRetryOptionsConverter implements Converter<RetryProperties, AmqpRetryOptions> {

    @Override
    public AmqpRetryOptions convert(RetryProperties retryProperties) {
        AmqpRetryMode mode;
        if (retryProperties.getBackoff().getMultiplier() > 0) {
            mode = AmqpRetryMode.EXPONENTIAL;
        } else {
            mode = AmqpRetryMode.FIXED;
        }
        AmqpRetryOptions retryOptions = new AmqpRetryOptions();
        retryOptions.setDelay(Duration.ofMillis(retryProperties.getBackoff().getDelay()));
        retryOptions.setMaxDelay(Duration.ofMillis(retryProperties.getBackoff().getMaxDelay()));
        retryOptions.setMode(mode);
        retryOptions.setMaxRetries(retryProperties.getMaxAttempts());
        retryOptions.setTryTimeout(Duration.ofMillis(retryProperties.getTimeout()));
        return retryOptions;
    }
}
