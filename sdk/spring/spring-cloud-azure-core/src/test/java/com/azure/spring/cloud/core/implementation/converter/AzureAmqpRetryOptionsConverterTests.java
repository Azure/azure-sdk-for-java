// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.properties.retry.AmqpRetryProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AzureAmqpRetryOptionsConverterTests {

    @Test
    void correctlyConverted() {
        AmqpRetryProperties source = new AmqpRetryProperties();
        source.setMaxRetries(1);
        source.setBaseDelay(Duration.ofSeconds(2));
        source.setMaxDelay(Duration.ofSeconds(3));
        source.setTryTimeout(Duration.ofSeconds(4));
        source.setMode(RetryOptionsProvider.RetryMode.EXPONENTIAL);

        AmqpRetryOptions target = AzureAmqpRetryOptionsConverter.AMQP_RETRY_CONVERTER.convert(source);

        assertNotNull(target);
        assertEquals(1, target.getMaxRetries());
        assertEquals(2, target.getDelay().getSeconds());
        assertEquals(3, target.getMaxDelay().getSeconds());
        assertEquals(4, target.getTryTimeout().getSeconds());
        assertEquals(AmqpRetryMode.EXPONENTIAL, target.getMode());
    }

    @Test
    void correctlyConvertRetryMode() {
        AmqpRetryProperties source = new AmqpRetryProperties();
        AmqpRetryOptions target;

        target = AzureAmqpRetryOptionsConverter.AMQP_RETRY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(AmqpRetryMode.EXPONENTIAL, target.getMode());

        source.setMode(RetryOptionsProvider.RetryMode.EXPONENTIAL);
        target = AzureAmqpRetryOptionsConverter.AMQP_RETRY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(AmqpRetryMode.EXPONENTIAL, target.getMode());

        source.setMode(RetryOptionsProvider.RetryMode.FIXED);
        target = AzureAmqpRetryOptionsConverter.AMQP_RETRY_CONVERTER.convert(source);
        assertNotNull(target);
        assertEquals(AmqpRetryMode.FIXED, target.getMode());
    }

}

