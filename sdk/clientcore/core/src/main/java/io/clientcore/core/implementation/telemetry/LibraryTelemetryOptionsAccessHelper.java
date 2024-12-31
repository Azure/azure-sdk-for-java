// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry;

import io.clientcore.core.telemetry.LibraryTelemetryOptions;

/**
 * Helper class to access package-private members of {@link LibraryTelemetryOptions}.
 */
public final class LibraryTelemetryOptionsAccessHelper {

    private static LibraryTelemetryOptionsAccessor accessor;

    /**
     * Defines the methods that can be called on an instance of {@link LibraryTelemetryOptions}.
     */
    public interface LibraryTelemetryOptionsAccessor {

        /**
         * Disables span suppression for the given options.
         * @param options the options to disable span suppression for
         * @return the options with span suppression disabled
         */
        LibraryTelemetryOptions disableSpanSuppression(LibraryTelemetryOptions options);

        /**
         * Checks if span suppression is disabled for the given options.
         * @param options the options to check
         * @return true if span suppression is disabled, false otherwise
         */
        boolean isSpanSuppressionDisabled(LibraryTelemetryOptions options);
    }

    /**
     * Disables span suppression for the given options.
     * @param options the options to disable span suppression for
     * @return the options with span suppression disabled
     */
    public static LibraryTelemetryOptions disableSpanSuppression(LibraryTelemetryOptions options) {
        return accessor.disableSpanSuppression(options);
    }

    /**
     * Checks if span suppression is disabled for the given options.
     * @param options the options to check
     * @return true if span suppression is disabled, false otherwise
     */
    public static boolean isSpanSuppressionDisabled(LibraryTelemetryOptions options) {
        return accessor.isSpanSuppressionDisabled(options);
    }

    /**
     * Sets the accessor.
     * @param accessor the accessor
     */
    public static void setAccessor(LibraryTelemetryOptionsAccessor accessor) {
        LibraryTelemetryOptionsAccessHelper.accessor = accessor;
    }

    private LibraryTelemetryOptionsAccessHelper() {
    }
}
