// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* StAX2 extension for StAX API (JSR-173).
 *
 * Copyright (c) 2005- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in file LICENSE, included with
 * the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.stax2.ri.typed;

/**
 * Helper class that contains method for converting numeric
 * values to and from String representations.
 */
public final class NumberUtil {
    /**
     * Maximum number of characters in a serialized integer is
     * 11; one for (minus) sign, and then up to 10 digits
     */
    public final static int MAX_INT_CLEN = 11;

    /**
     * Maximum number of characters in a serialized long is
     * 21; one for (minus) sign, and then up to 20 digits
     */
    public final static int MAX_LONG_CLEN = 21;

    /**
     * Maximum number of characters in a serialized double is
     * 26 (at least for Sun JDK; 19 digits for mantissa, 3 for exponent,
     * signs for mantissa and exponent, decimal point, 'E'):
     * but let's pad it up a little bit just to play it safe.
     */
    public final static int MAX_DOUBLE_CLEN = 32;

    /**
     * JDK serializes floats same way as doubles, so let's
     * reserve as much space
     */
    public final static int MAX_FLOAT_CLEN = MAX_DOUBLE_CLEN;

    private final static char NULL_CHAR = (char) 0;

    private final static int MILLION = 1000000;
    private final static int BILLION = 1000000000;
    private final static long TEN_BILLION_L = 10000000000L;
    private final static long THOUSAND_L = 1000L;

    private final static byte BYTE_HYPHEN = (byte) '-';
    private final static byte BYTE_1 = (byte) '1';
    private final static byte BYTE_2 = (byte) '2';

    /**
     *<p>
     * Note: we'll increase value since Integer.MIN_VALUE can not
     * actually be output using simple int-serialization mechanism
     * (since its negation does not fit in 32-bit signed int range)
     */
    private static long MIN_INT_AS_LONG = Integer.MIN_VALUE + 1;

    private static long MAX_INT_AS_LONG = Integer.MAX_VALUE;

    final static char[] LEADING_TRIPLETS = new char[4000];
    final static char[] FULL_TRIPLETS = new char[4000];
    static {
        /* Let's fill it with NULLs for ignorable leading digits,
         * and digit chars for others
         */
        int ix = 0;
        for (int i1 = 0; i1 < 10; ++i1) {
            char f1 = (char) ('0' + i1);
            char l1 = (i1 == 0) ? NULL_CHAR : f1;
            for (int i2 = 0; i2 < 10; ++i2) {
                char f2 = (char) ('0' + i2);
                char l2 = (i1 == 0 && i2 == 0) ? NULL_CHAR : f2;
                for (int i3 = 0; i3 < 10; ++i3) {
                    // Last is never to be empty
                    char f3 = (char) ('0' + i3);
                    LEADING_TRIPLETS[ix] = l1;
                    LEADING_TRIPLETS[ix + 1] = l2;
                    LEADING_TRIPLETS[ix + 2] = f3;
                    FULL_TRIPLETS[ix] = f1;
                    FULL_TRIPLETS[ix + 1] = f2;
                    FULL_TRIPLETS[ix + 2] = f3;
                    ix += 4;
                }
            }
        }
    }

    /*
    ///////////////////////////////////////////////////////////////
    // Public API
    ///////////////////////////////////////////////////////////////
     */

    /**
     *<p>
     * Note: caller must ensure that there is room for least 11 characters
     * (leading sign, and up to 10 digits) in buffer passed.
     *
     * @return Offset within buffer after outputting int
     */
    public static int writeInt(int value, char[] buffer, int offset) {
        if (value < 0) {
            // In general, can just output sign, negate, handle as positives
            if (value == Integer.MIN_VALUE) {
                /* But one special case: no matching positive value within
                 * range; let's just output as long
                 */
                return writeLong(value, buffer, offset);
            }
            buffer[offset++] = '-';
            value = -value;
        }

        if (value < MILLION) { // at most 2 triplets...
            if (value < 1000) {
                if (value < 10) {
                    buffer[offset++] = (char) ('0' + value);
                } else {
                    offset = writeLeadingTriplet(value, buffer, offset);
                }
            } else {
                int thousands = value / 1000;
                value -= (thousands * 1000); // == value % 1000
                offset = writeLeadingTriplet(thousands, buffer, offset);
                offset = writeFullTriplet(value, buffer, offset);
            }
            return offset;
        }

        // ok, all 3 triplets included
        /* Let's first hand possible billions separately before
         * handling 3 triplets. This is possible since we know we
         * can have at most '2' as billion count.
         */
        boolean hasBillions = (value >= BILLION);
        if (hasBillions) {
            value -= BILLION;
            if (value >= BILLION) {
                value -= BILLION;
                buffer[offset++] = '2';
            } else {
                buffer[offset++] = '1';
            }
        }
        int newValue = value / 1000;
        int ones = (value - (newValue * 1000)); // == value % 1000
        value = newValue;
        newValue /= 1000;
        int thousands = (value - (newValue * 1000));

        // value now has millions, which have 1, 2 or 3 digits
        if (hasBillions) {
            offset = writeFullTriplet(newValue, buffer, offset);
        } else {
            offset = writeLeadingTriplet(newValue, buffer, offset);
        }
        offset = writeFullTriplet(thousands, buffer, offset);
        offset = writeFullTriplet(ones, buffer, offset);
        return offset;
    }

    // Cut'n pasted from above
    public static int writeInt(int value, byte[] buffer, int offset) {
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                return writeLong(value, buffer, offset);
            }
            buffer[offset++] = BYTE_HYPHEN;
            value = -value;
        }
        if (value < MILLION) {
            if (value < 1000) {
                if (value < 10) {
                    buffer[offset++] = (byte) ('0' + value);
                } else {
                    offset = writeLeadingTriplet(value, buffer, offset);
                }
            } else {
                int thousands = value / 1000;
                value -= (thousands * 1000); // == value % 1000
                offset = writeLeadingTriplet(thousands, buffer, offset);
                offset = writeFullTriplet(value, buffer, offset);
            }
            return offset;
        }
        boolean hasBillions = (value >= BILLION);
        if (hasBillions) {
            value -= BILLION;
            if (value >= BILLION) {
                value -= BILLION;
                buffer[offset++] = BYTE_2;
            } else {
                buffer[offset++] = BYTE_1;
            }
        }
        int newValue = value / 1000;
        int ones = (value - (newValue * 1000)); // == value % 1000
        value = newValue;
        newValue /= 1000;
        int thousands = (value - (newValue * 1000));

        // value now has millions, which have 1, 2 or 3 digits
        if (hasBillions) {
            offset = writeFullTriplet(newValue, buffer, offset);
        } else {
            offset = writeLeadingTriplet(newValue, buffer, offset);
        }
        offset = writeFullTriplet(thousands, buffer, offset);
        offset = writeFullTriplet(ones, buffer, offset);
        return offset;
    }

    /**
     *<p>
     * Note: caller must ensure that there is room for least 21 characters
     * (leading sign, and up to 20 digits ) in buffer passed.
     *
     * @return Offset within buffer after outputting int
     */
    public static int writeLong(long value, char[] buffer, int offset) {
        // First: does it actually fit in an int?
        if (value < 0L) {
            if (value >= MIN_INT_AS_LONG) {
                return writeInt((int) value, buffer, offset);
            }
            if (value == Long.MIN_VALUE) {
                // Special case: no matching positive value within range
                return getChars(String.valueOf(value), buffer, offset);
            }
            buffer[offset++] = '-';
            value = -value;
        } else {
            if (value <= MAX_INT_AS_LONG) {
                return writeInt((int) value, buffer, offset);
            }
        }

        /* Ok: real long print. Need to first figure out length
         * in characters, and then print in from end to beginning
         */
        int origOffset = offset;
        offset += calcLongStrLength(value);
        int ptr = offset;

        // First, with long arithmetics:
        while (value > MAX_INT_AS_LONG) { // full triplet
            ptr -= 3;
            long newValue = value / THOUSAND_L;
            int triplet = (int) (value - newValue * THOUSAND_L);
            writeFullTriplet(triplet, buffer, ptr);
            value = newValue;
        }
        // Then with int arithmetics:
        int ivalue = (int) value;
        while (ivalue >= 1000) { // still full triplet
            ptr -= 3;
            int newValue = ivalue / 1000;
            int triplet = ivalue - (newValue * 1000);
            writeFullTriplet(triplet, buffer, ptr);
            ivalue = newValue;
        }
        // And finally, if anything remains, partial triplet
        writeLeadingTriplet(ivalue, buffer, origOffset);

        return offset;
    }

    // Cut'n pasted from above
    public static int writeLong(long value, byte[] buffer, int offset) {
        if (value < 0L) {
            if (value >= MIN_INT_AS_LONG) {
                return writeInt((int) value, buffer, offset);
            }
            if (value == Long.MIN_VALUE) { // shouldn't be common...
                return getAsciiBytes(String.valueOf(value), buffer, offset);
            }
            buffer[offset++] = BYTE_HYPHEN;
            value = -value;
        } else {
            if (value <= MAX_INT_AS_LONG) {
                return writeInt((int) value, buffer, offset);
            }
        }
        int origOffset = offset;
        offset += calcLongStrLength(value);
        int ptr = offset;

        while (value > MAX_INT_AS_LONG) { // full triplet
            ptr -= 3;
            long newValue = value / THOUSAND_L;
            int triplet = (int) (value - newValue * THOUSAND_L);
            writeFullTriplet(triplet, buffer, ptr);
            value = newValue;
        }
        int ivalue = (int) value;
        while (ivalue >= 1000) { // still full triplet
            ptr -= 3;
            int newValue = ivalue / 1000;
            int triplet = ivalue - (newValue * 1000);
            writeFullTriplet(triplet, buffer, ptr);
            ivalue = newValue;
        }
        writeLeadingTriplet(ivalue, buffer, origOffset);
        return offset;
    }

    public static int writeFloat(float value, char[] buffer, int offset) {
        // No real efficient method exposed by JDK, so let's keep it simple
        return getChars(String.valueOf(value), buffer, offset);
    }

    public static int writeFloat(float value, byte[] buffer, int offset) {
        // No real efficient method exposed by JDK, so let's keep it simple
        return getAsciiBytes(String.valueOf(value), buffer, offset);
    }

    public static int writeDouble(double value, char[] buffer, int offset) {
        // No real efficient method exposed by JDK, so let's keep it simple
        return getChars(String.valueOf(value), buffer, offset);
    }

    public static int writeDouble(double value, byte[] buffer, int offset) {
        return getAsciiBytes(String.valueOf(value), buffer, offset);
    }

    /*
    ////////////////////////////////////////
    // Internal methods
    ////////////////////////////////////////
     */

    private static int writeLeadingTriplet(int triplet, char[] buffer, int offset) {
        int digitOffset = (triplet << 2);
        char c = LEADING_TRIPLETS[digitOffset++];
        if (c != NULL_CHAR) {
            buffer[offset++] = c;
        }
        c = LEADING_TRIPLETS[digitOffset++];
        if (c != NULL_CHAR) {
            buffer[offset++] = c;
        }
        // Last is required to be non-empty
        buffer[offset++] = LEADING_TRIPLETS[digitOffset];
        return offset;
    }

    private static int writeLeadingTriplet(int triplet, byte[] buffer, int offset) {
        int digitOffset = (triplet << 2);
        char c = LEADING_TRIPLETS[digitOffset++];
        if (c != NULL_CHAR) {
            buffer[offset++] = (byte) c;
        }
        c = LEADING_TRIPLETS[digitOffset++];
        if (c != NULL_CHAR) {
            buffer[offset++] = (byte) c;
        }
        // Last is required to be non-empty
        buffer[offset++] = (byte) LEADING_TRIPLETS[digitOffset];
        return offset;
    }

    private static int writeFullTriplet(int triplet, char[] buffer, int offset) {
        int digitOffset = (triplet << 2);
        buffer[offset++] = FULL_TRIPLETS[digitOffset++];
        buffer[offset++] = FULL_TRIPLETS[digitOffset++];
        buffer[offset++] = FULL_TRIPLETS[digitOffset];
        return offset;
    }

    private static int writeFullTriplet(int triplet, byte[] buffer, int offset) {
        int digitOffset = (triplet << 2);
        buffer[offset++] = (byte) FULL_TRIPLETS[digitOffset++];
        buffer[offset++] = (byte) FULL_TRIPLETS[digitOffset++];
        buffer[offset++] = (byte) FULL_TRIPLETS[digitOffset];
        return offset;
    }

    /**
     *<p>
     * Pre-conditions: posValue is positive, and larger than
     * Integer.MAX_VALUE (about 2 billions).
     */
    private static int calcLongStrLength(long posValue) {
        int len = 10;
        long comp = TEN_BILLION_L;

        // 19 is longest, need to worry about overflow
        while (posValue >= comp) {
            if (len == 19) {
                break;
            }
            ++len;
            comp = (comp << 3) + (comp << 1); // 10x
        }
        return len;
    }

    private static int getChars(String str, char[] buffer, int ptr) {
        int len = str.length();
        str.getChars(0, len, buffer, ptr);
        return ptr + len;
    }

    private static int getAsciiBytes(String str, byte[] buffer, int ptr) {
        for (int i = 0, len = str.length(); i < len; ++i) {
            buffer[ptr++] = (byte) str.charAt(i);
        }
        return ptr;
    }
}
