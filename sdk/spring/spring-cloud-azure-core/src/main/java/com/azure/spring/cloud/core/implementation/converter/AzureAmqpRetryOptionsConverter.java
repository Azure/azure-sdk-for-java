// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a {@link RetryOptionsProvider.AmqpRetryOptions} to a {@link AmqpRetryOptions}.
 */
public final class AzureAmqpRetryOptionsConverter implements Converter<RetryOptionsProvider.AmqpRetryOptions, AmqpRetryOptions> {

    public static final AzureAmqpRetryOptionsConverter AMQP_RETRY_CONVERTER = new AzureAmqpRetryOptionsConverter();

    private AzureAmqpRetryOptionsConverter() {

    }

    @Override
    public AmqpRetryOptions convert(RetryOptionsProvider.AmqpRetryOptions retry) {
        AmqpRetryOptions result = new AmqpRetryOptions();

        if (RetryOptionsProvider.RetryMode.EXPONENTIAL.equals(retry.getMode())) {
            result.setMode(AmqpRetryMode.EXPONENTIAL);
        } else if (RetryOptionsProvider.RetryMode.FIXED.equals(retry.getMode())) {
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
