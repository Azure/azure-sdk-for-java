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
     * Generator for introducing jitter in backoff calculations. Jitter helps prevent multiple
     * clients from retrying simultaneously (thundering herd).
     */
    private static final Random RANDOM = new Random();

    private static Long maxBackoff = (long) 600;

    private static Long minBackoff = (long) 30;

    /**
     * Calculates the new Backoff time for requests.
     * @param attempts Number of attempts so far
     * @return Nano Seconds to the next request
     * @throws IllegalArgumentException when back off time or attempt number is invalid
     */
    static Long calculateBackoff(Integer attempts) {

        if (minBackoff < 0) {
            throw new IllegalArgumentException("Minimum Backoff time needs to be greater than or equal to 0.");
        }

        if (maxBackoff < 0) {
            throw new IllegalArgumentException("Maximum Backoff time needs to be greater than or equal to 0.");
        }

        if (attempts < 0) {
            throw new IllegalArgumentException("Number of previous attempts needs to be a positive number.");
        }

        long minBackoffNano = minBackoff * SECONDS_TO_NANO_SECONDS;
        long maxBackoffNano = maxBackoff * SECONDS_TO_NANO_SECONDS;

        if (attempts <= 1 || maxBackoff <= minBackoff) {
            return minBackoffNano;
        }

        double maxNanoSeconds = Math.max(1, minBackoffNano) * ((long) 1 << Math.min(attempts, MAX_ATTEMPTS));

        if (maxNanoSeconds > maxBackoffNano || maxNanoSeconds <= 0) {
            maxNanoSeconds = maxBackoffNano;
        }

        return (long) (minBackoffNano + ((RANDOM.nextDouble() * (maxNanoSeconds - minBackoffNano)) + minBackoffNano));
    }

}
