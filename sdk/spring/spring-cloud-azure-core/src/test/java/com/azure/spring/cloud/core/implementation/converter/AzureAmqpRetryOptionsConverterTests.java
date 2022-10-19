// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.spring.cloud.core.properties.retry.AmqpRetryProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AzureAmqpRetryOptionsConverterTests {

    @Test
    void correctlyConvertExponentialMode() {
        AmqpRetryProperties source = new AmqpRetryProperties();
        source.getExponential().setMaxRetries(1);
        source.getExponential().setBaseDelay(Duration.ofSeconds(2));
        source.getExponential().setMaxDelay(Duration.ofSeconds(3));
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
    void correctlyConvertFixedMode() {
        AmqpRetryProperties source = new AmqpRetryProperties();
        source.getFixed().setMaxRetries(1);
        source.getFixed().setDelay(Duration.ofSeconds(2));
        source.setTryTimeout(Duration.ofSeconds(3));
        source.setMode(RetryOptionsProvider.RetryMode.FIXED);

        AmqpRetryOptions target = AzureAmqpRetryOptionsConverter.AMQP_RETRY_CONVERTER.convert(source);

        assertNotNull(target);
        assertEquals(1, target.getMaxRetries());
        assertEquals(2, target.getDelay().getSeconds());
        assertEquals(3, target.getTryTimeout().getSeconds());
        assertEquals(AmqpRetryMode.FIXED, target.getMode());
    }

    @Test
    void correctlyConvertRetryMode() {
        AmqpRetryProperties source = new AmqpRetryProperties();
        source.getExponential().setMaxRetries(3);
        source.getFixed().setMaxRetries(3);
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

