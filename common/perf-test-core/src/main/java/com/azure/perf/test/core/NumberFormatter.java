// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.lang.Math;

public final class NumberFormatter {
    private NumberFormatter() { }

    public static String Format(double value, int minSignificantDigits) {
        if (minSignificantDigits < 0) {
            throw new IllegalArgumentException("minSignificantDigits must be greater than zero");
        }

        if (value == 0) {
            return "0";
        }

        double log = Math.log10(Math.abs(value));
        int significantDigits = (int)Math.ceil(Math.max(log, minSignificantDigits));

        double divisor = Math.pow(10, Math.ceil(log - significantDigits));
        double rounded = divisor * Math.round(value / divisor);

        int decimals = (int)Math.ceil(Math.max(0, significantDigits - log - 1));

        return String.format("%,." + decimals + "f", rounded);
    }
}
