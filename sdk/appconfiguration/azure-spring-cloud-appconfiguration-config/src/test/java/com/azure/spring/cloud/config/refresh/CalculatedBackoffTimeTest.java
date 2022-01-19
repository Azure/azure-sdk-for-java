// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.refresh;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

public class CalculatedBackoffTimeTest {

    @Test
    public void testCalculate() {
        Date testDate = CalculatedBackoffTime.calculate(null, null);

        assertNull(testDate);

        int minInterval = 5;
        int testTime = 10;

        Duration interval = Duration.ofSeconds(minInterval);
        AppConfigurationProviderProperties properties = new AppConfigurationProviderProperties();
        properties.setDefaultMaxBackoff(600);
        properties.setDefaultMinBackoff(30);

        testDate = CalculatedBackoffTime.calculate(interval, properties);

        assertNotNull(testDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, testTime);

        assertTrue(calendar.getTime().after(testDate));

        minInterval = 60;
        interval = Duration.ofSeconds(minInterval);

        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, testTime);

        assertTrue(calendar.getTime().before(CalculatedBackoffTime.calculate(interval, properties)));
    }

}
