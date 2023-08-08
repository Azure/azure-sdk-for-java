// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage;

import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.service.implementation.storage.common.StorageRetryProperties;
import com.azure.storage.common.policy.RequestRetryOptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AzureStorageRetryOptionsConverterTests {

    @Test
    void correctlyConvertedExponential() {
        StorageRetryProperties source = new StorageRetryProperties();
        source.setSecondaryHost("localhost");
        source.getExponential().setMaxRetries(1);
        source.getExponential().setBaseDelay(Duration.ofSeconds(2));
        source.getExponential().setMaxDelay(Duration.ofSeconds(3));
        source.setTryTimeout(Duration.ofSeconds(4));
        source.setMode(RetryOptionsProvider.RetryMode.EXPONENTIAL);

        RequestRetryOptions target = AzureStorageRetryOptionsConverter.STORAGE_RETRY_CONVERTER.convert(source);

        assertNotNull(target);
        assertNotNull(target);
        assertEquals(1, target.getMaxTries());
        assertEquals(2, target.getRetryDelay().getSeconds());
        assertEquals(3, target.getMaxRetryDelay().getSeconds());
        assertEquals(4, target.getTryTimeoutDuration().getSeconds());
        assertEquals("localhost", target.getSecondaryHost());
    }

    @Test
    void correctlyConvertedFixed() {
        StorageRetryProperties source = new StorageRetryProperties();
        source.setSecondaryHost("localhost");
        source.getFixed().setMaxRetries(1);
        source.getFixed().setDelay(Duration.ofSeconds(2));
        source.setTryTimeout(Duration.ofSeconds(3));
        source.setMode(RetryOptionsProvider.RetryMode.FIXED);

        RequestRetryOptions target = AzureStorageRetryOptionsConverter.STORAGE_RETRY_CONVERTER.convert(source);

        assertNotNull(target);
        assertNotNull(target);
        assertEquals(1, target.getMaxTries());
        assertEquals(2, target.getRetryDelay().getSeconds());
        assertEquals(3, target.getTryTimeoutDuration().getSeconds());
        assertEquals("localhost", target.getSecondaryHost());
    }

}
