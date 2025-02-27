// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package io.clientcore.core.serialization.json.implementation.jackson.core.io;

import java.math.BigDecimal;

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

    /**
     * Helper method to (more) efficiently parse integer numbers from
     * String values. Input String must be simple Java integer value.
     * No range checks are made to verify that the value fits in 32-bit Java {@code int}:
     * caller is expected to only calls this in cases where this can be guaranteed
     * (basically: number of digits does not exceed 9)
     *<p>
     * NOTE: semantics differ significantly from {@link #parseInt(char[], int, int)}.
     *
     * @param s String that contains integer value to decode
     *
     * @return Decoded {@code int} value
     */
    public static int parseInt(String s) {
        /*
         * Ok: let's keep strategy simple: ignoring optional minus sign,
         * we'll accept 1 - 9 digits and parse things efficiently;
         * otherwise just defer to JDK parse functionality.
         */
        char c = s.charAt(0);
        int len = s.length();
        boolean neg = (c == '-');
        int offset = 1;
        // must have 1 - 9 digits after optional sign:
        // negative?
        if (neg) {
            if (len == 1 || len > 10) {
                return Integer.parseInt(s);
            }
            c = s.charAt(offset++);
        } else {
            if (len > 9) {
                return Integer.parseInt(s);
            }
        }
        if (c > '9' || c < '0') {
            return Integer.parseInt(s);
        }
        int num = c - '0';
        if (offset < len) {
            c = s.charAt(offset++);
            if (c > '9' || c < '0') {
                return Integer.parseInt(s);
            }
            num = (num * 10) + (c - '0');
            if (offset < len) {
                c = s.charAt(offset++);
                if (c > '9' || c < '0') {
                    return Integer.parseInt(s);
                }
                num = (num * 10) + (c - '0');
                // Let's just loop if we have more than 3 digits:
                if (offset < len) {
                    do {
                        c = s.charAt(offset++);
                        if (c > '9' || c < '0') {
                            return Integer.parseInt(s);
                        }
                        num = (num * 10) + (c - '0');
                    } while (offset < len);
                }
            }
        }
        return neg ? -num : num;
    }

    public static long parseLong(char[] ch, int off, int len) {
        // Note: caller must ensure length is [10, 18]
        int len1 = len - 9;
        long val = parseInt(ch, off, len1) * L_BILLION;
        return val + (long) parseInt(ch, off + len1, 9);
    }

    /**
     * Similar to {@link #parseInt(String)} but for {@code long} values.
     *
     * @param s String that contains {@code long} value to decode
     *
     * @return Decoded {@code long} value
     */
    public static long parseLong(String s) {
        // Ok, now; as the very first thing, let's just optimize case of "fake longs";
        // that is, if we know they must be ints, call int parsing
        int length = s.length();
        if (length <= 9) {
            return parseInt(s);
        }
        // !!! TODO: implement efficient 2-int parsing...
        return Long.parseLong(s);
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

    public static BigDecimal parseBigDecimal(char[] ch, int off, int len) throws NumberFormatException {
        return BigDecimalParser.parse(ch, off, len);
    }

    public static BigDecimal parseBigDecimal(char[] ch) throws NumberFormatException {
        return BigDecimalParser.parse(ch);
    }
}
