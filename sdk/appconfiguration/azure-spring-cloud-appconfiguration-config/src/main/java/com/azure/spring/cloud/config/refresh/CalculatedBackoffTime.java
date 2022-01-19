// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.refresh;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

/**
 * Calculates the amount of time to the next refresh, if a refresh fails.
 */
public final class CalculatedBackoffTime {

    static int attempts = 0;
    
    /**
     * Sets the number of retry attempts back to 0.
     */
    public static void resetAttempts() {
        attempts = 0;
    }

    /**
     * Calculates the amount of time to the next refresh, if a refresh fails.
     * 
     * @param interval the Refresh Interval
     * @param properties App Configuration Provider Properties
     * @return new Refresh Date
     */
    public static Date calculate(Duration interval, AppConfigurationProviderProperties properties) {
        attempts += 1;

        if (interval == null) {
            return null;
        }
        int durationPeriod = Math.toIntExact(interval.getSeconds());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (durationPeriod <= properties.getDefaultMinBackoff()) {
            calendar.add(Calendar.SECOND, Math.toIntExact(interval.getSeconds()));
            return calendar.getTime();
        }
        
        int defaultMinBackoff = properties.getDefaultMinBackoff();
        double min = Math.min(Math.pow(2, attempts - 1), durationPeriod);
        
        long maxBackoff = Math.min(interval.getSeconds(), properties.getDefaultMaxBackoff());

        long calculatedBackoff = defaultMinBackoff * getRandomBackoff(1, min);
        
        calendar.add(Calendar.SECOND, Math.toIntExact(Math.min(maxBackoff, calculatedBackoff)));
        
        Date newRefresh = calendar.getTime();
        return newRefresh;
    }

    /**
     * Calculates the amount of time to the next refresh, if a refresh fails. Takes current Refresh date into account
     * for watch keys.
     * @param currentRefresh the current refresh date
     * @param interval the Refresh Interval
     * @param properties App Configuration Provider Properties
     * @return new Refresh Date
     */
    public static Date calculateBefore(Date currentRefresh, Duration interval,
        AppConfigurationProviderProperties properties) {
        Date calculatedDate = calculate(interval, properties);

        if (calculatedDate.before(currentRefresh)) {
            return currentRefresh;
        }

        return calculatedDate;
    }

    private static int getRandomBackoff(double min, double max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

}
