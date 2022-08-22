// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class BackoffTimeCalculatorTest {

    /**
     * Testing the calculated time is some time in the future, multiple attempts don't guarantee longer wait times.
     */
    @Test
    public void testCalculate() {
        int testTime = 10;

        BackoffTimeCalculator.setDefaults((long) 600, (long) 30);
        Long testDate = BackoffTimeCalculator.calculateBackoff(1);

        assertNotNull(testDate);

        assertTrue(testDate > 1);

        Long calculatedTime = BackoffTimeCalculator.calculateBackoff(1);

        assertTrue(calculatedTime > testTime);

        calculatedTime = BackoffTimeCalculator.calculateBackoff(2);

        assertTrue(calculatedTime > testTime);

        calculatedTime = BackoffTimeCalculator.calculateBackoff(3);

        assertTrue(calculatedTime > testTime);
    }
}
