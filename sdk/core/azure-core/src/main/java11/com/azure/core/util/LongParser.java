// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

/**
 * Range-based decimal parsing selected for the running Java version.
 */
final class LongParser {
    private LongParser() {
    }

    static long parse(String value, int start, int end) {
        return Long.parseLong(value, start, end, 10);
    }
}
