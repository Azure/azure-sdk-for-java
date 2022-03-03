// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.refresh;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

public class CalculatedBackoffTimeTest {

    /**
     * Testing the calculated time is some time in the future, multiple attempts don't guarantee longer wait times.
     */
    @Test
    public void testCalculate() {
        Instant testDate = CalculatedBackoffTime.calculateBefore("client", null, null, null);

        assertNull(testDate);

        int minInterval = 5;
        int testTime = 10;

        Duration interval = Duration.ofSeconds(minInterval);
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        properties.setDefaultMaxBackoff(600);
        properties.setDefaultMinBackoff(30);

        testDate = CalculatedBackoffTime.calculateBefore("client", Instant.now(), interval, properties);

        assertNotNull(testDate);
        Instant futureTime = Instant.now().plusSeconds(testTime);

        assertTrue(futureTime.isAfter(testDate));

        minInterval = 60;
        interval = Duration.ofSeconds(minInterval);

        Instant tenSecondsFromNow = Instant.now().plusSeconds(testTime);

        Instant calcuatedTime = CalculatedBackoffTime.calculateBefore("client", Instant.now().minusSeconds(1), interval,
            properties);

        assertTrue(tenSecondsFromNow.isBefore(calcuatedTime));

        tenSecondsFromNow = Instant.now().plusSeconds(testTime);

        calcuatedTime = CalculatedBackoffTime.calculateBefore("client", Instant.now().minusSeconds(1), interval,
            properties);

        assertTrue(tenSecondsFromNow.isBefore(calcuatedTime));

        tenSecondsFromNow = Instant.now().plusSeconds(testTime);

        calcuatedTime = CalculatedBackoffTime.calculateBefore("client", Instant.now().minusSeconds(1), interval,
            properties);

        assertTrue(tenSecondsFromNow.isBefore(calcuatedTime));
    }

}
