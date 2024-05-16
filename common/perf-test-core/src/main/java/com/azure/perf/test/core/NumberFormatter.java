// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.lang.Math;

public final class NumberFormatter {
    private NumberFormatter() { }

    // Formats a number with a minimum number of significant digits.
    // Digits to the left of the decimal point are always significant.
    // Examples:
    // - Format(0, 4) -> "0.000"
    // - Format(12345, 4) -> "12,345"
    // - Format(1.2345, 4) -> "1.235"
    // - Format(0.00012345, 4) -> "0.0001235"
    public static String Format(double value, int minSignificantDigits) {
        if (minSignificantDigits < 0) {
            throw new IllegalArgumentException("minSignificantDigits must be greater than zero");
        }

        // Special case since log(0) is undefined
        if (value == 0) {
            return String.format("%." + (minSignificantDigits - 1) + "f", value);
        }

        double log = Math.log10(Math.abs(value));
        int significantDigits = (int)Math.max(Math.ceil(log), minSignificantDigits);

        double divisor = Math.pow(10, Math.ceil(log) - significantDigits);
        double rounded = divisor * Math.round(value / divisor);

        int decimals = (int)Math.max(0, significantDigits - Math.floor(log) - 1);

        return String.format("%,." + decimals + "f", rounded);
    }
}
