// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.io;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Helper class for efficient parsing of various JSON numbers.
 *<p>
 * NOTE! Does NOT validate against maximum length limits: caller must
 * do that if and as necessary.
 */
@SuppressWarnings("fallthrough")
public final class NumberInput {

    /**
     * Constants needed for parsing longs from basic int parsing methods
     */
    final static long L_BILLION = 1000000000;

    final static String MIN_LONG_STR_NO_SIGN = String.valueOf(Long.MIN_VALUE).substring(1);
    final static String MAX_LONG_STR = String.valueOf(Long.MAX_VALUE);

    /**
     * Fast method for parsing unsigned integers that are known to fit into
     * regular 32-bit signed int type. This means that length is
     * between 1 and 9 digits (inclusive) and there is no sign character.
     *<p>
     * Note: public to let unit tests call it; not meant to be used by any
     * code outside this package.
     *
     * @param ch Buffer that contains integer value to decode
     * @param off Offset of the first digit character in buffer
     * @param len Length of the number to decode (in characters)
     *
     * @return Decoded {@code int} value
     */
    public static int parseInt(char[] ch, int off, int len) {
        if (len > 0 && ch[off] == '+') {
            off++;
            len--;
        }

        int num = ch[off + len - 1] - '0';

        switch (len) {
            case 9:
                num += (ch[off++] - '0') * 100000000;
            case 8:
                num += (ch[off++] - '0') * 10000000;
            case 7:
                num += (ch[off++] - '0') * 1000000;
            case 6:
                num += (ch[off++] - '0') * 100000;
            case 5:
                num += (ch[off++] - '0') * 10000;
            case 4:
                num += (ch[off++] - '0') * 1000;
            case 3:
                num += (ch[off++] - '0') * 100;
            case 2:
                num += (ch[off] - '0') * 10;
        }
        return num;
    }

    public static long parseLong(char[] ch, int off, int len) {
        // Note: caller must ensure length is [10, 18]
        int len1 = len - 9;
        long val = parseInt(ch, off, len1) * L_BILLION;
        return val + parseInt(ch, off + len1, 9);
    }

    /**
     * Parses an unsigned long made up of exactly 19 digits.
     * <p>
     * It is the callers responsibility to make sure the input is exactly 19 digits.
     * and fits into a 64bit long by calling {@link #inLongRange(char[], int, int, boolean)}
     * first.
     * <p>
     * Note that input String must NOT contain leading minus sign (even
     * if {@code negative} is set to true).
     *
     * @param ch Buffer that contains integer value to decode
     * @param off Offset of the first digit character in buffer
     * @param negative Whether original number had a minus sign
     * @return Decoded {@code long} value
     *
     * @since 2.15.0
     */
    public static long parseLong19(char[] ch, int off, boolean negative) {
        // Note: caller must ensure length is 19
        long num = 0L;
        for (int i = 0; i < 19; i++) {
            char c = ch[off + i];
            num = (num * 10) + (c - '0');
        }
        return negative ? -num : num;
    }

    /**
     * Helper method for determining if given String representation of
     * an integral number would fit in 64-bit Java long or not.
     * Note that input String must NOT contain leading minus sign (even
     * if 'negative' is set to true).
     *
     * @param ch Buffer that contains long value to check
     * @param off Offset of the first digit character in buffer
     * @param len Length of the number to decode (in characters)
     * @param negative Whether original number had a minus sign (which is
     *    NOT passed to this method) or not
     *
     * @return {@code True} if specified String representation is within Java
     *   {@code long} range; {@code false} if not.
     */
    public static boolean inLongRange(char[] ch, int off, int len, boolean negative) {
        String cmpStr = negative ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int cmpLen = cmpStr.length();
        if (len < cmpLen)
            return true;
        if (len > cmpLen)
            return false;

        for (int i = 0; i < cmpLen; ++i) {
            int diff = ch[off + i] - cmpStr.charAt(i);
            if (diff != 0) {
                return (diff < 0);
            }
        }
        return true;
    }

    /**
     * @param s a string representing a number to parse
     * @return closest matching double
     * @throws NumberFormatException if string cannot be represented by a double
     * @since v2.14
     */
    public static double parseDouble(final String s) throws NumberFormatException {
        return Double.parseDouble(s);
    }

    /**
     * @param s a string representing a number to parse
     * @return closest matching float
     * @throws NumberFormatException if string cannot be represented by a float
     * @since v2.14
     */
    public static float parseFloat(final String s) throws NumberFormatException {
        return Float.parseFloat(s);
    }

    /**
     * @param s a string representing a number to parse
     * @return a BigDecimal
     * @throws NumberFormatException if the char array cannot be represented by a BigDecimal
     * @since v2.15
     */
    public static BigDecimal parseBigDecimal(final String s) throws NumberFormatException {
        return BigDecimalParser.parse(s);
    }

    /**
     * @param s a string representing a number to parse
     * @return a BigInteger
     * @throws NumberFormatException if string cannot be represented by a BigInteger
     * @since v2.15
     */
    public static BigInteger parseBigInteger(final String s) throws NumberFormatException {
        return new BigInteger(s);
    }

}
