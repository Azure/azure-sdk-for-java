// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.converter;

import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AzureHttpRetryOptionsConverterTests {

    @Test
    void correctlyConvertedExponentialRetry() {
        RetryProperties source = new RetryProperties();
        source.getExponential().setMaxRetries(1);
        source.getExponential().setBaseDelay(Duration.ofSeconds(2));
        source.getExponential().setMaxDelay(Duration.ofSeconds(3));
        source.setMode(RetryOptionsProvider.RetryMode.EXPONENTIAL);

        RetryOptions target = AzureHttpRetryOptionsConverter.HTTP_RETRY_CONVERTER.convert(source);

        assertNotNull(target);
        ExponentialBackoffOptions exponentialBackoffOptions = target.getExponentialBackoffOptions();
        assertNotNull(exponentialBackoffOptions);
        assertNull(target.getFixedDelayOptions());
        assertEquals(1, exponentialBackoffOptions.getMaxRetries());
        assertEquals(2, exponentialBackoffOptions.getBaseDelay().getSeconds());
        assertEquals(3, exponentialBackoffOptions.getMaxDelay().getSeconds());
    }

    @Test
    void correctlyConvertedFixedRetry() {
        RetryProperties source = new RetryProperties();
        source.getFixed().setMaxRetries(1);
        source.getFixed().setDelay(Duration.ofSeconds(2));
        source.setMode(RetryOptionsProvider.RetryMode.FIXED);

        RetryOptions target = AzureHttpRetryOptionsConverter.HTTP_RETRY_CONVERTER.convert(source);

        assertNotNull(target);
        FixedDelayOptions fixedDelayOptions = target.getFixedDelayOptions();
        assertNotNull(fixedDelayOptions);
        assertNull(target.getExponentialBackoffOptions());
        assertEquals(1, fixedDelayOptions.getMaxRetries());
        assertEquals(2, fixedDelayOptions.getDelay().getSeconds());
    }

}
