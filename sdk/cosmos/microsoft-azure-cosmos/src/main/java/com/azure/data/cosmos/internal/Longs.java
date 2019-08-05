// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

public class Longs {
    public static long tryParse(String value, long defaultValue) {
        if (Strings.isNullOrEmpty(value)) {
            return defaultValue;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
