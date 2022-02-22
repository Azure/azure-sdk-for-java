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

    @Test
    public void testCalculate() {
        Instant testDate = CalculatedBackoffTime.calculate(null, null);

        assertNull(testDate);

        int minInterval = 5;
        int testTime = 10;

        Duration interval = Duration.ofSeconds(minInterval);
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        properties.setDefaultMaxBackoff(600);
        properties.setDefaultMinBackoff(30);

        testDate = CalculatedBackoffTime.calculate(interval, properties);

        assertNotNull(testDate);
        Instant futureTime = Instant.now().plusSeconds(testTime);

        assertTrue(futureTime.isAfter(testDate));

        minInterval = 60;
        interval = Duration.ofSeconds(minInterval);

        Instant pastTime = Instant.now().plusSeconds(testTime);

        Instant calcuatedTime = CalculatedBackoffTime.calculate(interval, properties);

        Duration cbt1 = Duration.between(pastTime, calcuatedTime);

        assertTrue(pastTime.isBefore(calcuatedTime));

        pastTime = Instant.now().plusSeconds(testTime);

        CalculatedBackoffTime.addAttempt();
        calcuatedTime = CalculatedBackoffTime.calculate(interval, properties);

        Duration cbt2 = Duration.between(pastTime, calcuatedTime);

        assertTrue(pastTime.isBefore(calcuatedTime));
        assertTrue(cbt1.compareTo(cbt2) < 1);

        pastTime = Instant.now().plusSeconds(testTime);

        CalculatedBackoffTime.addAttempt();
        calcuatedTime = CalculatedBackoffTime.calculate(interval, properties);

        Duration cbt3 = Duration.between(pastTime, calcuatedTime);

        assertTrue(pastTime.isBefore(calcuatedTime));
        assertTrue(cbt2.compareTo(cbt3) < 1);
    }

}
