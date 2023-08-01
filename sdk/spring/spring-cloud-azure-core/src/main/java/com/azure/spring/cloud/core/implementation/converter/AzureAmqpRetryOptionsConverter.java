// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts a {@link RetryOptionsProvider.AmqpRetryOptions} to a {@link AmqpRetryOptions}.
 */
public final class AzureAmqpRetryOptionsConverter implements Converter<RetryOptionsProvider.AmqpRetryOptions, AmqpRetryOptions> {

    public static final AzureAmqpRetryOptionsConverter AMQP_RETRY_CONVERTER = new AzureAmqpRetryOptionsConverter();
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAmqpRetryOptionsConverter.class);


    private AzureAmqpRetryOptionsConverter() {

    }

    @Override
    public AmqpRetryOptions convert(RetryOptionsProvider.AmqpRetryOptions retry) {
        PropertyMapper mapper = new PropertyMapper();

        RetryOptionsProvider.RetryMode retryMode = retry.getMode();

        if (RetryOptionsProvider.RetryMode.EXPONENTIAL == retryMode) {
            RetryOptionsProvider.RetryOptions.ExponentialRetryOptions exponential = retry.getExponential();
            if (exponential != null && exponential.getMaxRetries() != null) {
                AmqpRetryOptions result = new AmqpRetryOptions();

                result.setMode(AmqpRetryMode.EXPONENTIAL);

                mapper.from(retry.getTryTimeout()).to(result::setTryTimeout);
                mapper.from(exponential.getMaxRetries()).to(result::setMaxRetries);
                mapper.from(exponential.getBaseDelay()).to(result::setDelay);
                mapper.from(exponential.getMaxDelay()).to(result::setMaxDelay);

                return result;
            } else {
                LOGGER.debug("The max-retries is not set, skip the convert.");
            }
        } else if (RetryOptionsProvider.RetryMode.FIXED == retryMode) {
            RetryOptionsProvider.RetryOptions.FixedRetryOptions fixed = retry.getFixed();
            if (fixed != null && fixed.getMaxRetries() != null) {
                AmqpRetryOptions result = new AmqpRetryOptions();

                result.setMode(AmqpRetryMode.FIXED);
                mapper.from(retry.getTryTimeout()).to(result::setTryTimeout);
                mapper.from(fixed.getMaxRetries()).to(result::setMaxRetries);
                mapper.from(fixed.getDelay()).to(result::setDelay);

                return result;
            } else {
                LOGGER.debug("The max-retries is not set, skip the convert.");
            }
        }
        return null;
    }
}
