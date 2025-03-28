// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import io.clientcore.core.instrumentation.SdkInstrumentationOptions;

/**
 * Helper class to access package-private members of {@link SdkInstrumentationOptions}.
 */
public final class SdkInstrumentationOptionsAccessHelper {

    private static SdkInstrumentationOptionsAccessor accessor;

    /**
     * Defines the methods that can be called on an instance of {@link SdkInstrumentationOptions}.
     */
    public interface SdkInstrumentationOptionsAccessor {

        /**
         * Disables span suppression for the given options.
         * @param options the options to disable span suppression for
         * @return the options with span suppression disabled
         */
        SdkInstrumentationOptions disableSpanSuppression(SdkInstrumentationOptions options);

        /**
         * Checks if span suppression is disabled for the given options.
         * @param options the options to check
         * @return true if span suppression is disabled, false otherwise
         */
        boolean isSpanSuppressionDisabled(SdkInstrumentationOptions options);
    }

    /**
     * Disables span suppression for the given options.
     * @param options the options to disable span suppression for
     * @return the options with span suppression disabled
     */
    public static SdkInstrumentationOptions disableSpanSuppression(SdkInstrumentationOptions options) {
        return accessor.disableSpanSuppression(options);
    }

    /**
     * Checks if span suppression is disabled for the given options.
     * @param options the options to check
     * @return true if span suppression is disabled, false otherwise
     */
    public static boolean isSpanSuppressionDisabled(SdkInstrumentationOptions options) {
        return accessor.isSpanSuppressionDisabled(options);
    }

    /**
     * Sets the accessor.
     * @param accessor the accessor
     */
    public static void setAccessor(SdkInstrumentationOptionsAccessor accessor) {
        SdkInstrumentationOptionsAccessHelper.accessor = accessor;
    }

    private SdkInstrumentationOptionsAccessHelper() {
    }
}
