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
        RequestRetryOptions storageOptions = RequestRetryOptions.fromRetryOptions(coreOptions, Duration.ofSeconds(12), "secondaryHost");

        assertEquals(storageOptions.getMaxTries(), coreOptions.getFixedDelayOptions().getMaxRetries() + 1);
        assertEquals(storageOptions.getRetryDelay(), coreOptions.getFixedDelayOptions().getDelay());
        assertEquals(storageOptions.getTryTimeoutDuration(), Duration.ofSeconds(12));
        assertEquals(storageOptions.getSecondaryHost(), "secondaryHost");
        assertEquals(storageOptions.calculateDelayInMs(2), coreOptions.getFixedDelayOptions().getDelay().toMillis());
        assertEquals(storageOptions.calculateDelayInMs(3), coreOptions.getFixedDelayOptions().getDelay().toMillis());

        coreOptions = new RetryOptions(
            new ExponentialBackoffOptions()
                .setMaxRetries(3)
                .setBaseDelay(Duration.ofSeconds(4))
                .setMaxDelay(Duration.ofSeconds(10)));
        storageOptions = RequestRetryOptions.fromRetryOptions(coreOptions, Duration.ofSeconds(12), "secondaryHost");

        assertEquals(storageOptions.getMaxTries(), coreOptions.getExponentialBackoffOptions().getMaxRetries() + 1);
        assertEquals(storageOptions.getRetryDelay(), coreOptions.getExponentialBackoffOptions().getBaseDelay());
        assertEquals(storageOptions.getMaxRetryDelay(), coreOptions.getExponentialBackoffOptions().getMaxDelay());
        assertEquals(storageOptions.getTryTimeoutDuration(), Duration.ofSeconds(12));
        assertEquals(storageOptions.getSecondaryHost(), "secondaryHost");
        assertEquals(storageOptions.calculateDelayInMs(2), coreOptions.getExponentialBackoffOptions().getBaseDelay().toMillis());
        assertEquals(storageOptions.calculateDelayInMs(20), coreOptions.getExponentialBackoffOptions().getMaxDelay().toMillis());
    }


}
