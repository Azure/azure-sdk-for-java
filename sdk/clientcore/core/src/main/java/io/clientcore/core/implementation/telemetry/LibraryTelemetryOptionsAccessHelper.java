// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry;

import io.clientcore.core.telemetry.LibraryTelemetryOptions;

public final class LibraryTelemetryOptionsAccessHelper {

    private static LibraryTelemetryOptionsAccessor accessor;

    public interface LibraryTelemetryOptionsAccessor {
        LibraryTelemetryOptions disableSpanSuppression(LibraryTelemetryOptions options);

        boolean isSpanSuppressionDisabled(LibraryTelemetryOptions options);
    }

    public static LibraryTelemetryOptions disableSpanSuppression(LibraryTelemetryOptions options) {
        return accessor.disableSpanSuppression(options);
    }

    public static boolean isSpanSuppressionDisabled(LibraryTelemetryOptions options) {
        return accessor.isSpanSuppressionDisabled(options);
    }

    public static void setAccessor(LibraryTelemetryOptionsAccessor accessor) {
        LibraryTelemetryOptionsAccessHelper.accessor = accessor;
    }

    private LibraryTelemetryOptionsAccessHelper() {
    }
}
