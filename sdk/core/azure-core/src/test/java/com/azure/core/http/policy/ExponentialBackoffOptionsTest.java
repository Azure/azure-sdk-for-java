// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExponentialBackoffOptionsTest {
    @Test
    public void testZeroBaseDelay() {
        assertThrows(IllegalArgumentException.class,
            () -> new ExponentialBackoffOptions().setBaseDelay(Duration.ofSeconds(0)));
    }

    @Test
    public void testBaseGreaterThanMaxDelay() {
        assertThrows(IllegalArgumentException.class,
            () -> new ExponentialBackoffOptions().setBaseDelay(Duration.ofSeconds(1))
                .setMaxDelay(Duration.ofMillis(500)));

        assertThrows(IllegalArgumentException.class,
            () -> new ExponentialBackoffOptions().setMaxDelay(Duration.ofMillis(500))
                .setBaseDelay(Duration.ofSeconds(1)));
    }

    @Test
    public void testNegativeMaxRetries() {
        assertThrows(IllegalArgumentException.class, () -> new ExponentialBackoffOptions().setMaxRetries(-1));
    }

    @Test
    public void testNegativeBaseDelay() {
        assertThrows(IllegalArgumentException.class,
            () -> new ExponentialBackoffOptions().setBaseDelay(Duration.ofSeconds(-1)));
    }

    @Test
    public void testNegativeMaxDelay() {
        assertThrows(IllegalArgumentException.class,
            () -> new ExponentialBackoffOptions().setMaxDelay(Duration.ofSeconds(-1)));
    }
}
