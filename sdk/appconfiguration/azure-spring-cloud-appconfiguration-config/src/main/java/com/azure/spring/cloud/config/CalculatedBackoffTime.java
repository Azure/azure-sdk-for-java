// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

/**
 * Calculates the amount of time to the next refresh, if a refresh fails.
 */
public final class CalculatedBackoffTime {

    /**
     * Calculates the amount of time to the next refresh, if a refresh fails. Takes current Refresh date into account
     * for watch keys.
     * @param state state being checked
     * @param properties App Configuration Provider Properties
     * @return new Refresh Date
     */
    public static Instant calculateBefore(State state, AppConfigurationProviderProperties properties) {
        // The refresh interval is only updated if it is expired.
        if (Instant.now().isAfter(state.getNextRefreshCheck())) {
            Integer attempt = state.getRefreshAttempt();
            state.addRefreshAttempt();

            return calculate(attempt, state.getRefreshInterval(), properties);
        }

        return state.getNextRefreshCheck();
    }

    /**
     * Calculates the amount of time to the next refresh, if a refresh fails. Takes current Refresh date into account
     * for watch keys. Used for checking client refresh-interval only.
     * @param nextRefreshCheck next refresh for the whole client
     * @param attempt refresh attempt for the client
     * @param interval the Refresh Interval
     * @param properties App Configuration Provider Properties
     * @return new Refresh Date
     */
    public static Instant calculateBefore(Instant nextRefreshCheck, Integer attempt, Duration interval,
        AppConfigurationProviderProperties properties) {

        if (interval == null) {
            return null;
        }

        // The refresh interval is only updated if it is expired.
        if (Instant.now().isAfter(nextRefreshCheck)) {
            return calculate(attempt, interval.getSeconds(), properties);
        }

        return nextRefreshCheck;
    }

    private static Instant calculate(Integer attempt, long interval, AppConfigurationProviderProperties properties) {
        int durationPeriod = Math.toIntExact(interval);

        Instant now = Instant.now();

        if (durationPeriod <= properties.getDefaultMinBackoff()) {
            return now.plusSeconds(interval);
        }

        long defaultMinBackoff = TimeUnit.NANOSECONDS.convert(properties.getDefaultMinBackoff(), TimeUnit.SECONDS);
        Integer maxRandomValue = Math.min((attempt << 2), Integer.MAX_VALUE);

        long maxBackoff = Math.min(
            TimeUnit.NANOSECONDS.convert(interval, TimeUnit.SECONDS),
            TimeUnit.NANOSECONDS.convert(properties.getDefaultMaxBackoff(), TimeUnit.SECONDS));

        double randomValue = getRandomBackoff(1, maxRandomValue);

        double calculatedBackoff = defaultMinBackoff * randomValue;

        // IMPORTANT: This can overflow, so set to max if less than 0
        if (calculatedBackoff <= 0) {
            calculatedBackoff = maxBackoff;
        }

        return now.plusNanos((long) Math.min(maxBackoff, calculatedBackoff));
    }

    private static double getRandomBackoff(double min, double max) {
        return ((Math.random() * (max - min)) + min);
    }

}
