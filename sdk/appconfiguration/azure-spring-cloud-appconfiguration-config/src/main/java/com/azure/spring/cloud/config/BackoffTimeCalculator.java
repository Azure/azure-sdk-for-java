// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

/**
 * Calculates the amount of time to the next refresh, if a refresh fails.
 */
final class BackoffTimeCalculator {

    private static final Long MAX_ATTEMPTS = (long) 63;

    private static final Long SECONDS_TO_NANO_SECONDS = (long) 1000000000;

    /**
     * Calculates the new Backoff time for requests.
     * 
     * @param attempt Number of attempts so far
     * @param interval Base Interval of requests
     * @param maxBackoff maximum amount of time between requests
     * @param minBackoff minimum amount of time between requests
     * @return Nano Seconds to the next request
     */
    static Long calculateBackoff(Integer attempts, Long interval, Long maxBackoff, Long minBackoff) {

        if (minBackoff < 0) {
            throw new IllegalArgumentException("Minimum Backoff time needs to be greater than or equal to 0.");
        }

        if (maxBackoff < 0) {
            throw new IllegalArgumentException("Maximum Backoff time needs to be greater than or equal to 0.");
        }

        if (attempts < 0) {
            throw new IllegalArgumentException("Number of previous attempts needs to be a positive number.");
        }

        if (attempts <= 1 || maxBackoff <= minBackoff) {
            return minBackoff * SECONDS_TO_NANO_SECONDS;
        }

        double maxNanoSeconds = Math.max(1, minBackoff * SECONDS_TO_NANO_SECONDS)
            * ((long) 1 << Math.min(attempts, MAX_ATTEMPTS));

        if (maxNanoSeconds > maxBackoff * SECONDS_TO_NANO_SECONDS || maxNanoSeconds <= 0) {
            maxNanoSeconds = maxBackoff * SECONDS_TO_NANO_SECONDS;
        }

        return (long) ((minBackoff * SECONDS_TO_NANO_SECONDS)
            + getRandomBackoff(minBackoff * SECONDS_TO_NANO_SECONDS, maxNanoSeconds * SECONDS_TO_NANO_SECONDS));
    }

    private static double getRandomBackoff(double min, double max) {
        return ((Math.random() * (max - min)) + min);
    }

}
