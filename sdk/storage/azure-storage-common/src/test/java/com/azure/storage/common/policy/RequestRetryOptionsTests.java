// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.policy;

import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RequestRetryOptionsTests {
    @Test
    public void retryOptionsToRequestRetryOptionsMapping() {
        assertThrows(NullPointerException.class, () -> RequestRetryOptions.fromRetryOptions(null, null, null));

        RetryOptions coreOptions = new RetryOptions(new FixedDelayOptions(3, Duration.ofSeconds(4)));
        RequestRetryOptions storageOptions
            = RequestRetryOptions.fromRetryOptions(coreOptions, Duration.ofSeconds(12), "secondaryHost");

        assertEquals(coreOptions.getFixedDelayOptions().getMaxRetries() + 1, storageOptions.getMaxTries());
        assertEquals(coreOptions.getFixedDelayOptions().getDelay(), storageOptions.getRetryDelay());
        assertEquals(Duration.ofSeconds(12), storageOptions.getTryTimeoutDuration());
        assertEquals("secondaryHost", storageOptions.getSecondaryHost());
        assertEquals(coreOptions.getFixedDelayOptions().getDelay().toMillis(), storageOptions.calculateDelayInMs(2));
        assertEquals(coreOptions.getFixedDelayOptions().getDelay().toMillis(), storageOptions.calculateDelayInMs(3));

        coreOptions = new RetryOptions(new ExponentialBackoffOptions().setMaxRetries(3)
            .setBaseDelay(Duration.ofSeconds(4))
            .setMaxDelay(Duration.ofSeconds(10)));
        storageOptions = RequestRetryOptions.fromRetryOptions(coreOptions, Duration.ofSeconds(12), "secondaryHost");

        assertEquals(coreOptions.getExponentialBackoffOptions().getMaxRetries() + 1, storageOptions.getMaxTries());
        assertEquals(coreOptions.getExponentialBackoffOptions().getBaseDelay(), storageOptions.getRetryDelay());
        assertEquals(coreOptions.getExponentialBackoffOptions().getMaxDelay(), storageOptions.getMaxRetryDelay());
        assertEquals(Duration.ofSeconds(12), storageOptions.getTryTimeoutDuration());
        assertEquals("secondaryHost", storageOptions.getSecondaryHost());
        assertEquals(coreOptions.getExponentialBackoffOptions().getBaseDelay().toMillis(),
            storageOptions.calculateDelayInMs(2));
        assertEquals(coreOptions.getExponentialBackoffOptions().getMaxDelay().toMillis(),
            storageOptions.calculateDelayInMs(20));
    }
}
