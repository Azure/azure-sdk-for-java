// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

public class Integers {
    public static int tryParse(String value, int defaultValue) {
        if (Strings.isNullOrEmpty(value)) {
            return defaultValue;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer tryParse(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
