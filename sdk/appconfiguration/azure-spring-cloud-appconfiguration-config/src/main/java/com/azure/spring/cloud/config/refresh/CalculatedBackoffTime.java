// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.refresh;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

/**
 * Calculates the amount of time to the next refresh, if a refresh fails.
 */
public final class CalculatedBackoffTime {

    static HashMap<String, Integer> attempts = new HashMap<>();

    /**
     * Sets the number of retry attempts back to 0.
     */
    public static void resetAttempts() {
        attempts = new HashMap<>();
    }

    /**
     * Calculates the amount of time to the next refresh, if a refresh fails. Takes current Refresh date into account
     * for watch keys.
     * @param endpoint value for tracking the failed attempt number
     * @param currentRefresh the current refresh date
     * @param interval the Refresh Interval
     * @param properties App Configuration Provider Properties
     * @return new Refresh Date
     */
    public static Instant calculateBefore(String endpoint, Instant currentRefresh, Duration interval,
        AppConfigurationProviderProperties properties) {

        if (interval == null) {
            return null;
        }

        // The refresh interval is only updated if it is expired.
        if (Instant.now().isAfter(currentRefresh)) {
            Integer attempt = attempts.getOrDefault(endpoint, 1);
            attempts.put(endpoint, attempt + 1);

            int durationPeriod = Math.toIntExact(interval.getSeconds());

            Instant now = Instant.now();

            if (durationPeriod <= properties.getDefaultMinBackoff()) {
                return now.plusSeconds(interval.getSeconds());
            }

            long defaultMinBackoff = TimeUnit.NANOSECONDS.convert(properties.getDefaultMinBackoff(), TimeUnit.SECONDS);
            Integer maxRandomValue = Math.min((attempt - 1 << 2), Integer.MAX_VALUE);

            long maxBackoff = Math.min(
                TimeUnit.NANOSECONDS.convert(interval.getSeconds(), TimeUnit.SECONDS),
                TimeUnit.NANOSECONDS.convert(properties.getDefaultMaxBackoff(), TimeUnit.SECONDS));

            double randomValue = getRandomBackoff(1, maxRandomValue);

            double calculatedBackoff = defaultMinBackoff * randomValue;
            
            //  IMPORTANT: This can overflow, so set to max if less than 0
            if (calculatedBackoff <= 0) {
                calculatedBackoff = maxBackoff;
            }

            return now.plusNanos((long) Math.min(maxBackoff, calculatedBackoff));
        }

        return currentRefresh;
    }

    private static double getRandomBackoff(double min, double max) {
        return ((Math.random() * (max - min)) + min);
    }

}
