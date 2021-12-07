// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.converter;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.spring.core.aware.RetryAware;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a {@link RetryProperties} to a {@link AmqpRetryOptions}.
 */
public final class AzureAmqpRetryOptionsConverter implements Converter<RetryAware.Retry, AmqpRetryOptions> {

    public static final AzureAmqpRetryOptionsConverter AMQP_RETRY_CONVERTER = new AzureAmqpRetryOptionsConverter();

    private AzureAmqpRetryOptionsConverter() {

    }

    @Override
    public AmqpRetryOptions convert(RetryAware.Retry retry) {
        AmqpRetryOptions retryOptions = new AmqpRetryOptions();

        if (retry.getMaxAttempts() != null) {
            retryOptions.setMaxRetries(retry.getMaxAttempts());
        }

        if (retry.getTimeout() != null) {
            retryOptions.setTryTimeout(retry.getTimeout());
        }

        AmqpRetryMode mode;
        final RetryAware.Backoff backoff = retry.getBackoff();
        if (backoff != null) {
            if (backoff.getMultiplier() != null && backoff.getMultiplier() > 0) {
                mode = AmqpRetryMode.EXPONENTIAL;
            } else {
                mode = AmqpRetryMode.FIXED;
            }
            retryOptions.setMode(mode);
            if (backoff.getDelay() != null) {
                retryOptions.setDelay(backoff.getDelay());
            }
            if (backoff.getMaxDelay() != null) {
                retryOptions.setMaxDelay(backoff.getMaxDelay());
            }
        }
        return retryOptions;
    }
}
