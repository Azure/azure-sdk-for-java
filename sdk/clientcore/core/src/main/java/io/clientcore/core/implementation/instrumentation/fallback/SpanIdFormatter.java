// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

/**
 * Fixed-width span identifier formatting selected for the running Java version.
 */
final class SpanIdFormatter {
    private SpanIdFormatter() {
    }

    static String format(long value) {
        char[] result = new char[16];
        RandomIdUtils.longToBase16String(value, result, 0);
        return new String(result);
    }
}
