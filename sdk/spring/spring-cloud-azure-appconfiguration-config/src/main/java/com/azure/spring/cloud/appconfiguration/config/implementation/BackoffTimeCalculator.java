// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.Random;

/**
 * Utility class for calculating exponential backoff times for Azure App Configuration retry operations.
 */
final class BackoffTimeCalculator {

    /**
     * Maximum number of attempts to consider for exponential backoff calculation. This prevents integer overflow when
     * calculating 2^attempts. Value of 63 ensures that 2^63 is the largest power of 2 that fits in a long.
     */
    private static final long MAX_ATTEMPTS = 63;

    /**
     * Conversion factor from seconds to nanoseconds. Used to convert backoff times from seconds to nanoseconds for
     * precise timing.
     */
    private static final long SECONDS_TO_NANOSECONDS = 1_000_000_000L;

    /**
     * Generator for introducing jitter in backoff calculations. Jitter helps prevent multiple clients from retrying
     * simultaneously (thundering herd).
     */
    private static final Random RANDOM = new Random();

    /**
     * Maximum backoff time in seconds. Default: 600 seconds (10 minutes) - reasonable maximum to prevent excessively
     * long waits.
     */
    private static long maxBackoffSeconds = 600;

    /**
     * Minimum backoff time in seconds. Default: 30 seconds - ensures proper rate limiting and prevents rapid retry
     * loops.
     */
    private static long minBackoffSeconds = 30;

    /**
     *
     * @param maxBackoff maximum amount of time between requests
     * @param minBackoff minimum amount of time between requests
     */
    static void setDefaults(Long maxBackoff, Long minBackoff) {
        BackoffTimeCalculator.maxBackoffSeconds = maxBackoff != null ? maxBackoff : 600L;
        BackoffTimeCalculator.minBackoffSeconds = minBackoff != null ? minBackoff : 30L;
    }

    /**
     * Calculates the exponential backoff time with jitter for retry operations.
     * 
     * @param attempts the number of retry attempts made so far; must be non-negative
     * @return the calculated backoff time in nanoseconds; never negative
     */
    static long calculateBackoff(Integer attempts) {

        if (minBackoffSeconds < 0) {
            throw new IllegalArgumentException("Minimum Backoff time needs to be greater than or equal to 0.");
        }

        if (maxBackoffSeconds < 0) {
            throw new IllegalArgumentException("Maximum Backoff time needs to be greater than or equal to 0.");
        }

        if (attempts < 0) {
            throw new IllegalArgumentException("Number of previous attempts needs to be a positive number.");
        }

        final long minBackoffNano = minBackoffSeconds * SECONDS_TO_NANOSECONDS;
        final long maxBackoffNano = maxBackoffSeconds * SECONDS_TO_NANOSECONDS;

        // For first attempts or when min equals max, return minimum backoff
        if (attempts <= 1 || maxBackoffNano <= minBackoffNano) {
            return minBackoffNano;
        }

        double maxNanoSeconds = Math.max(1, minBackoffNano) * ((long) 1 << Math.min(attempts, MAX_ATTEMPTS));

        if (maxNanoSeconds > maxBackoffNano || maxNanoSeconds <= 0) {
            maxNanoSeconds = maxBackoffNano;
        }

        return (long) (minBackoffNano + ((RANDOM.nextDouble() * (maxNanoSeconds - minBackoffNano)) + minBackoffNano));
    }

}
