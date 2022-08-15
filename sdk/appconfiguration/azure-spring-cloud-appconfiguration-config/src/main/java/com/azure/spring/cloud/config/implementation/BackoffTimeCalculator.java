// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.util.Random;

/**
 * Calculates the amount of time to the next refresh, if a refresh fails.
 */
final class BackoffTimeCalculator {

    private static final Long MAX_ATTEMPTS = (long) 63;

    private static final Long SECONDS_TO_NANO_SECONDS = (long) 1000000000;

    private static final Random RANDOM = new Random();

    private static Long maxBackoff = (long) 600;

    private static Long minBackoff = (long) 30;

    /**
     * 
     * @param maxBackoff maximum amount of time between requests
     * @param minBackoff minimum amount of time between requests
     */
    static void setDefaults(Long maxBackoffValue, Long minBackoffValue) {
        maxBackoff = maxBackoffValue;
        minBackoff = minBackoffValue;
    }

    /**
     * Calculates the new Backoff time for requests.
     * 
     * @param attempts Number of attempts so far
     * @return Nano Seconds to the next request
     * @throws IllegalArgumentException when backofftime or attempt number is invalid
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

        Long minBackoffNano = minBackoff * SECONDS_TO_NANO_SECONDS;
        Long maxBackoffNano = maxBackoff * SECONDS_TO_NANO_SECONDS;

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
