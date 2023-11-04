// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy.retry;

import com.generic.core.http.policy.retry.FixedDelayOptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FixedDelayOptionsTest {

    @Test
    public void testNullDelay() {
        assertThrows(NullPointerException.class,
            () -> new FixedDelayOptions(3, null));
    }

    @Test
    public void testNegativeMaxRetries() {
        assertThrows(IllegalArgumentException.class,
            () -> new FixedDelayOptions(-1, Duration.ofSeconds(1)));
    }
}
