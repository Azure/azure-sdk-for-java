// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;

/**
 * Helper class to access package-private members of {@link LibraryInstrumentationOptions}.
 */
public final class LibraryInstrumentationOptionsAccessHelper {

    private static LibraryInstrumentationOptionsAccessor accessor;

    /**
     * Defines the methods that can be called on an instance of {@link LibraryInstrumentationOptions}.
     */
    public interface LibraryInstrumentationOptionsAccessor {

        /**
         * Disables span suppression for the given options.
         * @param options the options to disable span suppression for
         * @return the options with span suppression disabled
         */
        LibraryInstrumentationOptions disableSpanSuppression(LibraryInstrumentationOptions options);

        /**
         * Checks if span suppression is disabled for the given options.
         * @param options the options to check
         * @return true if span suppression is disabled, false otherwise
         */
        boolean isSpanSuppressionDisabled(LibraryInstrumentationOptions options);
    }

    /**
     * Disables span suppression for the given options.
     * @param options the options to disable span suppression for
     * @return the options with span suppression disabled
     */
    public static LibraryInstrumentationOptions disableSpanSuppression(LibraryInstrumentationOptions options) {
        return accessor.disableSpanSuppression(options);
    }

    /**
     * Checks if span suppression is disabled for the given options.
     * @param options the options to check
     * @return true if span suppression is disabled, false otherwise
     */
    public static boolean isSpanSuppressionDisabled(LibraryInstrumentationOptions options) {
        return accessor.isSpanSuppressionDisabled(options);
    }

    /**
     * Sets the accessor.
     * @param accessor the accessor
     */
    public static void setAccessor(LibraryInstrumentationOptionsAccessor accessor) {
        LibraryInstrumentationOptionsAccessHelper.accessor = accessor;
    }

    private LibraryInstrumentationOptionsAccessHelper() {
    }
}
