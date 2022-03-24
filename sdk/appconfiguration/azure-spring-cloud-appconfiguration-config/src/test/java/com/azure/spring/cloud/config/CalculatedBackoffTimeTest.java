// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

public class CalculatedBackoffTimeTest {

    /**
     * Testing the calculated time is some time in the future, multiple attempts don't guarantee longer wait times.
     */
    @Test
    public void testCalculate() {
        int minInterval = 5;
        int testTime = 10;

        Long interval = (long) minInterval;
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        properties.setDefaultMaxBackoff((long) 600);
        properties.setDefaultMinBackoff((long) 30);

        Instant testDate = BackoffTimeCalculator.getNextRefreshCheck(Instant.now(), 1, (long) 1, properties);

        assertNotNull(testDate);
        Instant futureTime = Instant.now().plusSeconds(testTime);

        assertTrue(futureTime.isAfter(testDate));

        minInterval = 60;
        interval = (long) minInterval;

        Instant tenSecondsFromNow = Instant.now().plusSeconds(testTime);

        Instant calcuatedTime = BackoffTimeCalculator.getNextRefreshCheck(Instant.now().minusSeconds(1), 1,
            interval, properties);

        assertTrue(tenSecondsFromNow.isBefore(calcuatedTime));

        tenSecondsFromNow = Instant.now().plusSeconds(testTime);

        calcuatedTime = BackoffTimeCalculator.getNextRefreshCheck(Instant.now().minusSeconds(1), 1, interval,
            properties);

        assertTrue(tenSecondsFromNow.isBefore(calcuatedTime));

        tenSecondsFromNow = Instant.now().plusSeconds(testTime);

        calcuatedTime = BackoffTimeCalculator.getNextRefreshCheck(Instant.now().minusSeconds(1), 1, interval,
            properties);

        assertTrue(tenSecondsFromNow.isBefore(calcuatedTime));
    }

}
