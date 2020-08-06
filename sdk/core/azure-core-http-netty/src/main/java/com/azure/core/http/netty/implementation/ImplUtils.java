// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Helper class containing utility methods for the implementation package.
 */
final class ImplUtils {
    private static final long MINIMUM_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(1);

    /*
     * Helper function to convert the timeout duration into MILLISECONDS. If the duration is null, 0, or negative there
     * is no timeout period, so return 0. Otherwise, return the maximum of the duration and the minimum timeout period.
     */

    /**
     * Returns the timeout in milliseconds to use based on the passed {@link Duration}.
     * <p>
     * If the timeout is {@code null} a default of 60 seconds will be used. If the timeout is less than or equal to zero
     * no timeout will be used. If the timeout is less than one millisecond a timeout of one millisecond will be used.
     *
     * @param timeout The {@link Duration} to convert to timeout in milliseconds.
     * @return The timeout period in milliseconds, zero if no timeout.
     */
    static long getTimeoutMillis(Duration timeout) {
        // Timeout is null, use the 60 second default.
        if (timeout == null) {
            return TimeUnit.SECONDS.toMillis(60);
        }

        // Timeout is less than or equal to zero, return no timeout.
        if (timeout.isZero() || timeout.isNegative()) {
            return 0;
        }

        // Return the maximum of the timeout period and the minimum allowed timeout period.
        return Math.max(timeout.toMillis(), MINIMUM_TIMEOUT);
    }
}
