// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import org.junit.Test;

/**
 * Unit tests for {@link ExponentialBackoff}.
 */
public class ExponentialBackoffTest {

    @Test(expected = IllegalArgumentException.class)
    public void testZeroBaseDelay() {
        ExponentialBackoff expBackoff = new ExponentialBackoff(3, Duration.ofSeconds(0),
            Duration.ofMillis(1000));
    }

    @Test(expected = NullPointerException.class)
    public void testNullBaseDelay() {
        ExponentialBackoff expBackoff = new ExponentialBackoff(3, null, Duration.ofMillis(1000));
    }

    @Test(expected = NullPointerException.class)
    public void testNullMaxDelay() {
        ExponentialBackoff expBackoff = new ExponentialBackoff(3, Duration.ofSeconds(1), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseGreaterThanMaxDelay() {
        ExponentialBackoff expBackoff = new ExponentialBackoff(3, Duration.ofSeconds(1),
            Duration.ofMillis(500));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMaxRetries() {
        ExponentialBackoff expBackoff = new ExponentialBackoff(-1, Duration.ofSeconds(1),
            Duration.ofMillis(5000));
    }

    @Test
    public void testBaseEqualToMaxDelay() {
        ExponentialBackoff expBackoff = new ExponentialBackoff(3, Duration.ofSeconds(1),
            Duration.ofMillis(1000));
        assertEquals(expBackoff.getMaxRetries(), 3);
        assertTrue(expBackoff.calculateRetryDelay(0).toMillis() <= 1000);
        assertTrue(expBackoff.calculateRetryDelay(1).toMillis() == 1000);
        assertTrue(expBackoff.calculateRetryDelay(2).toMillis() == 1000);
    }

    @Test
    public void testDefaultExponentialBackoff() {
        ExponentialBackoff expBackoff = new ExponentialBackoff();
        assertEquals(3, expBackoff.getMaxRetries());

        // exponential backoff
        for (int i = 0; i < 3; i++) {
            long delayMillis = expBackoff.calculateRetryDelay(i).toMillis();
            assertTrue(delayMillis >= ((1 << i) * (800 * 0.95)) && delayMillis <= ((1 << i) * (800 * 1.05)));
        }
    }

    @Test
    public void testExponentialBackoff() {
        ExponentialBackoff expBackoff = new ExponentialBackoff(10, Duration.ofSeconds(1),
            Duration.ofSeconds(10));

        // exponential backoff
        for (int i = 0; i < 4; i++) {
            long delayMillis = expBackoff.calculateRetryDelay(i).toMillis();
            assertTrue(delayMillis >= ((1 << i) * 950) && delayMillis <= ((1 << i) * 1050));
        }

        // max delay
        for (int i = 4; i < 10; i++) {
            assertEquals(expBackoff.calculateRetryDelay(i).toMillis(), 10000);
        }
    }

}
