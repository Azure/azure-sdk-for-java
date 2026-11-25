// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

/**
 * Range-based decimal parsing selected for the running Java version.
 */
final class IntegerParser {
    private IntegerParser() {
    }

    static int parse(String value, int start, int end) {
        return Integer.parseInt(value.substring(start, end));
    }
}
