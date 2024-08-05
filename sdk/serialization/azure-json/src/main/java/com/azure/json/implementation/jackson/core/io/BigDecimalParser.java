// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.io;

import java.math.BigDecimal;

/**
 * Internal Jackson Helper class used to implement more optimized parsing of {@link BigDecimal} for REALLY
 * big values (over 500 characters).
 *<p>
 * This class is not meant to be used directly. It is designed to be used by Jackson JSON parsers (and parsers
 * for other Jackson supported data formats). The parsers check for invalid characters and the length of the number.
 * Without these checks, this parser is susceptible to performing badly with invalid inputs. If you need to parse
 * numbers directly, please use JavaBigDecimalParser in <a href="https://github.com/wrandelshofer/FastDoubleParser">fastdoubleparser</a>
 * instead.
 *</p>
 *<p>
 * Based on ideas from this
 * <a href="https://github.com/eobermuhlner/big-math/commit/7a5419aac8b2adba2aa700ccf00197f97b2ad89f">this
 * git commit</a>.
 *</p>
 *
 * @since 2.13
 */
public final class BigDecimalParser {
    final static int MAX_CHARS_TO_REPORT = 1000;

    private BigDecimalParser() {
    }

    /**
     * Internal Jackson method. Please do not use.
     *<p>
     * Note: Caller MUST pre-validate that given String represents a valid representation
     * of {@link BigDecimal} value: parsers in {@code jackson-core} do that; other
     * code must do the same.
     *
     * @param valueStr
     * @return BigDecimal value
     * @throws NumberFormatException
     */
    public static BigDecimal parse(String valueStr) {
        return parse(valueStr.toCharArray());
    }

    /**
     * Internal Jackson method. Please do not use.
     *<p>
     * Note: Caller MUST pre-validate that given String represents a valid representation
     * of {@link BigDecimal} value: parsers in {@code jackson-core} do that; other
     * code must do the same.
     *
     * @return BigDecimal value
     * @throws NumberFormatException
     */
    public static BigDecimal parse(final char[] chars, final int off, final int len) {
        try {
            return new BigDecimal(chars, off, len);

            // 20-Aug-2022, tatu: Although "new BigDecimal(...)" only throws NumberFormatException
            //    operations by "parseBigDecimal()" can throw "ArithmeticException", so handle both:
        } catch (ArithmeticException | NumberFormatException e) {
            throw _parseFailure(e, new String(chars, off, len));
        }
    }

    /**
     * Internal Jackson method. Please do not use.
     *<p>
     * Note: Caller MUST pre-validate that given String represents a valid representation
     * of {@link BigDecimal} value: parsers in {@code jackson-core} do that; other
     * code must do the same.
     *
     * @param chars
     * @return BigDecimal value
     * @throws NumberFormatException
     */
    public static BigDecimal parse(char[] chars) {
        return parse(chars, 0, chars.length);
    }

    private static NumberFormatException _parseFailure(Exception e, String fullValue) {
        String desc = e.getMessage();
        // 05-Feb-2021, tatu: Alas, JDK mostly has null message so:
        if (desc == null) {
            desc = "Not a valid number representation";
        }
        String valueToReport = _getValueDesc(fullValue);
        return new NumberFormatException(
            "Value " + valueToReport + " can not be deserialized as `java.math.BigDecimal`, reason: " + desc);
    }

    private static String _getValueDesc(String fullValue) {
        final int len = fullValue.length();
        if (len <= MAX_CHARS_TO_REPORT) {
            return String.format("\"%s\"", fullValue);
        }
        return String.format("\"%s\" (truncated to %d chars (from %d))", fullValue.substring(0, MAX_CHARS_TO_REPORT),
            MAX_CHARS_TO_REPORT, len);
    }

}
