// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.serialization.json.implementation;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utility class containing helper methods for serialization.
 */
public final class JsonUtils {
    /**
     * Parses a number from a string.
     * <p>
     * Callers to this method should ensure that the number string isn't null or empty before calling this method.
     * <p>
     * For integer numbers, this method will return the smallest number type that can represent the number. Where
     * {@link Integer} is preferred over {@link Long} and {@link Long} is preferred over {@link BigInteger}.
     * <p>
     * For floating point numbers, {@link Double} will be preferred but {@link BigDecimal} will be used if the number
     * is too large to fit in a {@link Double}.
     * <p>
     * Unlike the JSON specification, this method will handle the special floating point representations
     * ({@code NaN}, {@code Infinity}, etc) and will return a {@link Double} for those values.
     *
     * @param numberString The string representation of the number.
     * @return The number represented by the string.
     * @throws NumberFormatException If the string is not a valid number.
     */
    public static Number parseNumber(String numberString) {
        int length = numberString.length();

        // Use the length of the number and checks for special values to determine if the number is a special
        // floating point representation.
        // The special floating point representations are: NaN, Infinity, +Infinity, and -Infinity.
        // These will be returned using Double.
        if (length == 3 && "NaN".equals(numberString)) {
            return Double.NaN;
        } else if (length == 8 && "Infinity".equals(numberString)) {
            return Double.POSITIVE_INFINITY;
        } else if (length == 9) {
            if ("+Infinity".equals(numberString)) {
                return Double.POSITIVE_INFINITY;
            } else if ("-Infinity".equals(numberString)) {
                return Double.NEGATIVE_INFINITY;
            }
        }

        boolean floatingPoint = false;
        for (int i = 0; i < length; i++) {
            char c = numberString.charAt(i);
            if (c == '.' || c == 'e' || c == 'E') {
                floatingPoint = true;
                break;
            }
        }

        return floatingPoint ? handleFloatingPoint(numberString) : handleInteger(numberString);
    }

    private static Number handleFloatingPoint(String value) {
        // Floating point parsing will return Infinity if the String value is larger than what can be contained by
        // the numeric type. Check if the String contains the Infinity representation to know when to scale up the
        // numeric type.
        // Additionally, due to the handling of values that can't fit into the numeric type, the only time floating
        // point parsing will throw is when the string value is invalid.
        double d = Double.parseDouble(value);
        if (!Double.isInfinite(d)) {
            return d;
        }

        return new BigDecimal(value);
    }

    private static Number handleInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException failedInteger) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException failedLong) {
                failedLong.addSuppressed(failedInteger);
                try {
                    return new BigInteger(value);
                } catch (NumberFormatException failedBigDecimal) {
                    failedBigDecimal.addSuppressed(failedLong);
                    throw failedBigDecimal;
                }
            }
        }
    }
}
