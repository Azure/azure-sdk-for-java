// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

public class BackoffTimeCalculatorTest {

    /**
     * Testing the calculated time is some time in the future, multiple attempts don't guarantee longer wait times.
     */
    @Test
    public void testCalculate() {
        int testTime = 10;

        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        properties.setDefaultMaxBackoff((long) 600);
        properties.setDefaultMinBackoff((long) 30);

        Long testDate = BackoffTimeCalculator.calculateBackoff(1, (long) 600, (long) 30);

        assertNotNull(testDate);

        assertTrue(testDate > 1);

        Long calcuatedTime = BackoffTimeCalculator.calculateBackoff(1, (long) 600, (long) 30);

        assertTrue(calcuatedTime > testTime);

        calcuatedTime = BackoffTimeCalculator.calculateBackoff(2, (long) 600, (long) 30);

        assertTrue(calcuatedTime > testTime);

        calcuatedTime = BackoffTimeCalculator.calculateBackoff(3, (long) 600, (long) 30);

        assertTrue(calcuatedTime > testTime);
    }

}
