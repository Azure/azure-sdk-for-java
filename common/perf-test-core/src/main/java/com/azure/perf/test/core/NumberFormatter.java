// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.lang.Math;

public final class NumberFormatter {
    private NumberFormatter() { }

    // Formats a double with the specified minimum number of significant digits.
    // Digits to the left of the decimal point are never dropped.
    // Examples:
    // - Format(12345, 4) -> "12,345"
    // - Format(1.2345, 4) -> "1.235"
    // - Format(0.00012345, 4) -> "0.0001234"
    public static String Format(double value, int minSignificantDigits) {
        if (minSignificantDigits < 0) {
            throw new IllegalArgumentException("minSignificantDigits must be greater than zero");
        }

        // Signficant digits are undefined for the number zero, so hardcode to string "0".
        if (value == 0) {
            return "0";
        }

        double log = Math.log10(Math.abs(value));
        int significantDigits = (int)Math.max(Math.ceil(log), minSignificantDigits);

        double divisor = Math.pow(10, Math.ceil(log) - significantDigits);
        double rounded = divisor * Math.round(value / divisor);

        int decimals = (int)Math.max(0, significantDigits - Math.floor(log) - 1);

        return String.format("%,." + decimals + "f", rounded);
    }
}
