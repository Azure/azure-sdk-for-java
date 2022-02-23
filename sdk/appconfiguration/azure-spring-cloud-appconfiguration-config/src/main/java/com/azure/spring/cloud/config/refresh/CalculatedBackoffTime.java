// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.refresh;

import java.time.Duration;
import java.time.Instant;

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
     * Adds one to the refresh attempt. Should be called every time a refresh attempt fails to increase the backoff
     * time.
     */
    public static void addAttempt() {
        attempts += 1;
    }

    /**
     * Calculates the amount of time to the next refresh, if a refresh fails.
     * 
     * @param interval the Refresh Interval
     * @param properties App Configuration Provider Properties
     * @return new Refresh Date
     */
    public static Instant calculate(Duration interval, AppConfigurationProviderProperties properties) {
        if (interval == null) {
            return null;
        }
        int durationPeriod = Math.toIntExact(interval.getSeconds());

        Instant now = Instant.now();

        if (durationPeriod <= properties.getDefaultMinBackoff()) {
            return now.plusSeconds(interval.getSeconds());
        }

        int defaultMinBackoff = properties.getDefaultMinBackoff();
        double min = Math.min(Math.pow(2, attempts - 1), durationPeriod);

        long maxBackoff = Math.min(interval.getSeconds(), properties.getDefaultMaxBackoff());

        double calculatedBackoff = defaultMinBackoff * getRandomBackoff(1, min);

        return now.plusSeconds((int) Math.floor(Math.min(maxBackoff, calculatedBackoff)));
    }

    /**
     * Calculates the amount of time to the next refresh, if a refresh fails. Takes current Refresh date into account
     * for watch keys.
     * @param currentRefresh the current refresh date
     * @param interval the Refresh Interval
     * @param properties App Configuration Provider Properties
     * @return new Refresh Date
     */
    public static Instant calculateBefore(Instant currentRefresh, Duration interval,
        AppConfigurationProviderProperties properties) {
        Instant calculatedDate = calculate(interval, properties);

        if (calculatedDate.isBefore(currentRefresh)) {
            return currentRefresh;
        }

        return calculatedDate;
    }

    private static double getRandomBackoff(double min, double max) {
        return ((Math.random() * (max - min)) + min);
    }

}
