// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import java.util.HexFormat;

/**
 * Fixed-width span identifier formatting selected for the running Java version.
 */
final class SpanIdFormatter {
    private static final HexFormat FORMAT = HexFormat.of();

    private SpanIdFormatter() {
    }

    static String format(long value) {
        return FORMAT.toHexDigits(value);
    }
}
