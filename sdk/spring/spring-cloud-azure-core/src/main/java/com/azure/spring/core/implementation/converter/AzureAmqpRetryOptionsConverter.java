// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.implementation.converter;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.spring.core.aware.RetryOptionsAware;
import com.azure.spring.core.implementation.properties.PropertyMapper;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a {@link RetryProperties} to a {@link AmqpRetryOptions}.
 */
public final class AzureAmqpRetryOptionsConverter implements Converter<RetryOptionsAware.AmqpRetry, AmqpRetryOptions> {

    public static final AzureAmqpRetryOptionsConverter AMQP_RETRY_CONVERTER = new AzureAmqpRetryOptionsConverter();

    private AzureAmqpRetryOptionsConverter() {

    }

    @Override
    public AmqpRetryOptions convert(RetryOptionsAware.AmqpRetry retry) {
        AmqpRetryOptions result = new AmqpRetryOptions();

        if (RetryOptionsAware.RetryMode.EXPONENTIAL.equals(retry.getMode())) {
            result.setMode(AmqpRetryMode.EXPONENTIAL);
        } else if (RetryOptionsAware.RetryMode.FIXED.equals(retry.getMode())) {
            result.setMode(AmqpRetryMode.FIXED);
        }

        PropertyMapper mapper = new PropertyMapper();
        mapper.from(retry.getMaxRetries()).to(result::setMaxRetries);
        mapper.from(retry.getTryTimeout()).to(result::setTryTimeout);
        mapper.from(retry.getBaseDelay()).to(result::setDelay);
        mapper.from(retry.getMaxDelay()).to(result::setMaxDelay);

        return result;
    }
}
