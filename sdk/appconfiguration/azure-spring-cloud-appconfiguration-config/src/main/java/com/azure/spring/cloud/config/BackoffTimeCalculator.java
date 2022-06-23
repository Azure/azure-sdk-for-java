// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.util.Random;

/**
 * Calculates the amount of time to the next refresh, if a refresh fails.
 */
public final class BackoffTimeCalculator {

    private static final Long MAX_ATTEMPTS = (long) 63;

    private static final Long SECONDS_TO_NANO_SECONDS = (long) 1000000000;

    private static final Random RANDOM = new Random();

    /**
     * Calculates the new Backoff time for requests.
     * 
     * @param attempts Number of attempts so far
     * @param maxBackoff maximum amount of time between requests
     * @param minBackoff minimum amount of time between requests
     * @return Nano Seconds to the next request
     * @throws IllegalArgumentException when backofftime or attempt number is invalid
     */
    public static Long calculateBackoff(Integer attempts, Long maxBackoff, Long minBackoff) {

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
