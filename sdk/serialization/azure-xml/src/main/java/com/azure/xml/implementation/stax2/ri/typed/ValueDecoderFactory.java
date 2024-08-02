// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Reference Implementation of
 * Stax2 extension API (for basic Stax API, JSR-173)
 *
 * Copyright (c) 2008- Tatu Saloranta, tatu.saloranta@iki.fi
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

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import com.azure.xml.implementation.stax2.typed.TypedArrayDecoder;
import com.azure.xml.implementation.stax2.typed.TypedValueDecoder;

/**
 * Factory class used to construct all
 * {@link com.azure.xml.implementation.stax2.typed.TypedValueDecoder}
 * (and {@link com.azure.xml.implementation.stax2.typed.TypedArrayDecoder})
 * instances needed by a
 * single stream reader instance. Some decoders are also recycled
 * (for the lifetime of an encoder, which is same as its owners,
 * i.e. stream reader or writer's) to minimize overhead.
 *<p>
 * Since encoders may be recycled, instances are not thread-safe.
 *
 * @since 3.0
 */
public final class ValueDecoderFactory {
    // // // Lazily-constructed, recycled decoder instances
    // // // (only for simple commonly needed types)

    private BooleanDecoder mBooleanDecoder = null;
    private IntDecoder mIntDecoder = null;
    private LongDecoder mLongDecoder = null;
    private FloatDecoder mFloatDecoder = null;
    private DoubleDecoder mDoubleDecoder = null;

    public ValueDecoderFactory() {
    }

    /*
    /////////////////////////////////////////////////////
    // Factory methods, scalar decoders
    /////////////////////////////////////////////////////
    */

    public BooleanDecoder getBooleanDecoder() {
        if (mBooleanDecoder == null) {
            mBooleanDecoder = new BooleanDecoder();
        }
        return mBooleanDecoder;
    }

    public IntDecoder getIntDecoder() {
        if (mIntDecoder == null) {
            mIntDecoder = new IntDecoder();
        }
        return mIntDecoder;
    }

    public LongDecoder getLongDecoder() {
        if (mLongDecoder == null) {
            mLongDecoder = new LongDecoder();
        }
        return mLongDecoder;
    }

    public FloatDecoder getFloatDecoder() {
        if (mFloatDecoder == null) {
            mFloatDecoder = new FloatDecoder();
        }
        return mFloatDecoder;
    }

    public DoubleDecoder getDoubleDecoder() {
        if (mDoubleDecoder == null) {
            mDoubleDecoder = new DoubleDecoder();
        }
        return mDoubleDecoder;
    }

    // // // Other scalar decoders: not recycled

    public IntegerDecoder getIntegerDecoder() {
        return new IntegerDecoder();
    }

    public DecimalDecoder getDecimalDecoder() {
        return new DecimalDecoder();
    }

    public QNameDecoder getQNameDecoder(NamespaceContext nsc) {
        return new QNameDecoder(nsc);
    }

    /*
    /////////////////////////////////////////////////////
    // Factory methods, array decoders
    /////////////////////////////////////////////////////
    */

    /**
     * Method for constructing
     * integer array value decoder
     * that uses provided fixed array for storing results.
     */
    public IntArrayDecoder getIntArrayDecoder(int[] result, int offset, int len) {
        return new IntArrayDecoder(result, offset, len, getIntDecoder());
    }

    /**
     * Method for constructing
     * integer array value decoder
     * that automatically allocates and resizes result array as necessary.
     */
    public IntArrayDecoder getIntArrayDecoder() {
        return new IntArrayDecoder(getIntDecoder());
    }

    public LongArrayDecoder getLongArrayDecoder(long[] result, int offset, int len) {
        return new LongArrayDecoder(result, offset, len, getLongDecoder());
    }

    public LongArrayDecoder getLongArrayDecoder()

    {
        return new LongArrayDecoder(getLongDecoder());
    }

    public FloatArrayDecoder getFloatArrayDecoder(float[] result, int offset, int len) {
        return new FloatArrayDecoder(result, offset, len, getFloatDecoder());
    }

    public FloatArrayDecoder getFloatArrayDecoder()

    {
        return new FloatArrayDecoder(getFloatDecoder());
    }

    public DoubleArrayDecoder getDoubleArrayDecoder(double[] result, int offset, int len) {
        return new DoubleArrayDecoder(result, offset, len, getDoubleDecoder());
    }

    public DoubleArrayDecoder getDoubleArrayDecoder() {
        return new DoubleArrayDecoder(getDoubleDecoder());
    }

    /*
    /////////////////////////////////////////////////////
    // Shared decoder base class
    /////////////////////////////////////////////////////
    */

    /**
     * There are some things common to all textual decoders (like
     * white space trimming).
     */
    public abstract static class DecoderBase extends TypedValueDecoder {
        final static long L_BILLION = 1000000000;

        final static long L_MAX_INT = Integer.MAX_VALUE;

        final static long L_MIN_INT = Integer.MIN_VALUE;

        final static BigInteger BD_MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
        final static BigInteger BD_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

        /**
         * Pointer to the next character to check, within lexical value
         */
        protected int mNextPtr;

        protected DecoderBase() {
        }

        public abstract String getType();

        /**
         * Method called if the value to decode does not contain
         * any non-white space characters (including the case where
         * typed accessor is called for an empty element).
         */
        @Override
        public void handleEmptyValue() {
            /* Defalt behavior for all types implemented within
             * this class is to just throw an exception
             */
            throw new IllegalArgumentException(
                "Empty value (all white space) not a valid lexical representation of " + getType());
        }

        /*
        //////////////////////////////////////////////////
        // Shared methods, trimming
        //////////////////////////////////////////////////
         */

        /**
         * Method called to check that remaining String consists of zero or
         * more digits
         */
        protected void verifyDigits(String lexical, int start, int end) {
            for (; start < end; ++start) {
                char ch = lexical.charAt(start);
                if (ch > '9' || ch < '0') {
                    throw constructInvalidValue(lexical);
                }
            }
        }

        protected void verifyDigits(char[] lexical, int start, int end, int ptr) {
            for (; ptr < end; ++ptr) {
                char ch = lexical[ptr];
                if (ch > '9' || ch < '0') {
                    throw constructInvalidValue(lexical, start, end);
                }
            }
        }

        /**
         * @return Numeric value of the first non-zero character (or, in
         *   case of a zero value, zero)
         */
        protected int skipSignAndZeroes(String lexical, char ch, boolean hasSign, final int end) {
            int ptr;
            // Then optional sign
            if (hasSign) {
                ptr = 1;
                if (ptr >= end) {
                    throw constructInvalidValue(lexical);
                }
                ch = lexical.charAt(ptr++);
            } else {
                ptr = 1;
            }

            // Has to start with a digit
            int value = ch - '0';
            if (value < 0 || value > 9) {
                throw constructInvalidValue(lexical);
            }

            // Then, leading zero(es) to skip? (or just value zero itself)
            while (value == 0 && ptr < end) {
                int v2 = lexical.charAt(ptr) - '0';
                if (v2 < 0 || v2 > 9) {
                    break;
                }
                ++ptr;
                value = v2;
            }
            mNextPtr = ptr;
            return value;
        }

        protected int skipSignAndZeroes(char[] lexical, char ch, boolean hasSign, final int start, final int end) {
            int ptr = start + 1;
            if (hasSign) {
                if (ptr >= end) {
                    throw constructInvalidValue(lexical, start, end);
                }
                ch = lexical[ptr++];
            }

            // Has to start with a digit
            int value = ch - '0';
            if (value < 0 || value > 9) {
                throw constructInvalidValue(lexical, start, end);
            }

            // Then leading zero(es) to skip? (or just value zero itself)
            while (value == 0 && ptr < end) {
                int v2 = lexical[ptr] - '0';
                if (v2 < 0 || v2 > 9) {
                    break;
                }
                ++ptr;
                value = v2;
            }
            mNextPtr = ptr;
            return value;
        }

        /*
        ///////////////////////////////////////////////
        // Shared methods, int conversions
        ///////////////////////////////////////////////
        */

        /**
         * Fast method for parsing integers that are known to fit into
         * regular 32-bit signed int type. This means that length is
         * between 1 and 9 digits (inclusive)
         *
         * @return Parsed integer value
         */
        protected static int parseInt(char[] digitChars, int start, int end) {
            /* This looks ugly, but appears to be the fastest way
             * (based on perf testing, profiling)
             */
            int num = digitChars[start] - '0';
            if (++start < end) {
                num = (num * 10) + (digitChars[start] - '0');
                if (++start < end) {
                    num = (num * 10) + (digitChars[start] - '0');
                    if (++start < end) {
                        num = (num * 10) + (digitChars[start] - '0');
                        if (++start < end) {
                            num = (num * 10) + (digitChars[start] - '0');
                            if (++start < end) {
                                num = (num * 10) + (digitChars[start] - '0');
                                if (++start < end) {
                                    num = (num * 10) + (digitChars[start] - '0');
                                    if (++start < end) {
                                        num = (num * 10) + (digitChars[start] - '0');
                                        if (++start < end) {
                                            num = (num * 10) + (digitChars[start] - '0');
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return num;
        }

        protected static int parseInt(int num, char[] digitChars, int start, int end) {
            num = (num * 10) + (digitChars[start] - '0');
            if (++start < end) {
                num = (num * 10) + (digitChars[start] - '0');
                if (++start < end) {
                    num = (num * 10) + (digitChars[start] - '0');
                    if (++start < end) {
                        num = (num * 10) + (digitChars[start] - '0');
                        if (++start < end) {
                            num = (num * 10) + (digitChars[start] - '0');
                            if (++start < end) {
                                num = (num * 10) + (digitChars[start] - '0');
                                if (++start < end) {
                                    num = (num * 10) + (digitChars[start] - '0');
                                    if (++start < end) {
                                        num = (num * 10) + (digitChars[start] - '0');
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return num;
        }

        protected static int parseInt(String digitChars, int start, int end) {
            int num = digitChars.charAt(start) - '0';
            if (++start < end) {
                num = (num * 10) + (digitChars.charAt(start) - '0');
                if (++start < end) {
                    num = (num * 10) + (digitChars.charAt(start) - '0');
                    if (++start < end) {
                        num = (num * 10) + (digitChars.charAt(start) - '0');
                        if (++start < end) {
                            num = (num * 10) + (digitChars.charAt(start) - '0');
                            if (++start < end) {
                                num = (num * 10) + (digitChars.charAt(start) - '0');
                                if (++start < end) {
                                    num = (num * 10) + (digitChars.charAt(start) - '0');
                                    if (++start < end) {
                                        num = (num * 10) + (digitChars.charAt(start) - '0');
                                        if (++start < end) {
                                            num = (num * 10) + (digitChars.charAt(start) - '0');
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return num;
        }

        protected static int parseInt(int num, String digitChars, int start, int end) {
            num = (num * 10) + (digitChars.charAt(start) - '0');
            if (++start < end) {
                num = (num * 10) + (digitChars.charAt(start) - '0');
                if (++start < end) {
                    num = (num * 10) + (digitChars.charAt(start) - '0');
                    if (++start < end) {
                        num = (num * 10) + (digitChars.charAt(start) - '0');
                        if (++start < end) {
                            num = (num * 10) + (digitChars.charAt(start) - '0');
                            if (++start < end) {
                                num = (num * 10) + (digitChars.charAt(start) - '0');
                                if (++start < end) {
                                    num = (num * 10) + (digitChars.charAt(start) - '0');
                                    if (++start < end) {
                                        num = (num * 10) + (digitChars.charAt(start) - '0');
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return num;
        }

        protected static long parseLong(char[] digitChars, int start, int end) {
            // Note: caller must ensure length is [10, 18]
            int start2 = end - 9;
            long val = parseInt(digitChars, start, start2) * L_BILLION;
            return val + (long) parseInt(digitChars, start2, end);
        }

        protected static long parseLong(String digitChars, int start, int end) {
            // Note: caller must ensure length is [10, 18]
            int start2 = end - 9;
            long val = parseInt(digitChars, start, start2) * L_BILLION;
            return val + (long) parseInt(digitChars, start2, end);
        }

        /*
        ///////////////////////////////////////////////
        // Shared methods, error reporting
        ///////////////////////////////////////////////
        */

        protected IllegalArgumentException constructInvalidValue(String lexical) {
            // !!! Should we escape ctrl+chars etc?
            return new IllegalArgumentException(
                "Value \"" + lexical + "\" not a valid lexical representation of " + getType());
        }

        protected IllegalArgumentException constructInvalidValue(char[] lexical, int startOffset, int end) {
            return new IllegalArgumentException("Value \"" + lexicalDesc(lexical, startOffset, end)
                + "\" not a valid lexical representation of " + getType());
        }

        protected String lexicalDesc(char[] lexical, int startOffset, int end) {
            return _clean(new String(lexical, startOffset, end - startOffset));
        }

        protected String lexicalDesc(String lexical) {
            return _clean(lexical);
        }

        protected String _clean(String str) {
            // !!! Should we escape ctrl+chars etc?
            return str.trim();
        }
    }

    /*
    /////////////////////////////////////////////////////
    // Decoders, scalar primitives
    /////////////////////////////////////////////////////
    */

    public final static class BooleanDecoder extends DecoderBase {
        private boolean mValue;

        public BooleanDecoder() {
        }

        @Override
        public String getType() {
            return "boolean";
        }

        public boolean getValue() {
            return mValue;
        }

        @Override
        public void decode(String lexical) throws IllegalArgumentException {
            int len = lexical.length();
            char c = lexical.charAt(0);
            if (c == 't') {
                if (len == 4 && lexical.charAt(1) == 'r' && lexical.charAt(2) == 'u' && lexical.charAt(3) == 'e') {
                    mValue = true;
                    return;
                }
            } else if (c == 'f') {
                if (len == 5
                    && lexical.charAt(1) == 'a'
                    && lexical.charAt(2) == 'l'
                    && lexical.charAt(3) == 's'
                    && lexical.charAt(4) == 'e') {
                    mValue = false;
                    return;
                }
            } else if (c == '0') {
                if (len == 1) {
                    mValue = false;
                    return;
                }
            } else if (c == '1') {
                if (len == 1) {
                    mValue = true;
                    return;
                }
            }
            throw constructInvalidValue(lexical);
        }

        @Override
        public void decode(char[] lexical, int start, int end) throws IllegalArgumentException {
            // First, skip leading ws if any
            int len = end - start;
            char c = lexical[start];
            if (c == 't') {
                if (len == 4 && lexical[start + 1] == 'r' && lexical[start + 2] == 'u' && lexical[start + 3] == 'e') {
                    mValue = true;
                    return;
                }
            } else if (c == 'f') {
                if (len == 5
                    && lexical[start + 1] == 'a'
                    && lexical[start + 2] == 'l'
                    && lexical[start + 3] == 's'
                    && lexical[start + 4] == 'e') {
                    mValue = false;
                    return;
                }
            } else if (c == '0') {
                if (len == 1) {
                    mValue = false;
                    return;
                }
            } else if (c == '1') {
                if (len == 1) {
                    mValue = true;
                    return;
                }
            }
            throw constructInvalidValue(lexical, start, end);
        }
    }

    public final static class IntDecoder extends DecoderBase {
        private int mValue;

        public IntDecoder() {
        }

        @Override
        public String getType() {
            return "int";
        }

        public int getValue() {
            return mValue;
        }

        @Override
        public void decode(String lexical) throws IllegalArgumentException {
            final int end = lexical.length();
            char ch = lexical.charAt(0);
            boolean neg = (ch == '-');
            int nr;

            if (neg || (ch == '+')) {
                nr = skipSignAndZeroes(lexical, ch, true, end);
            } else {
                nr = skipSignAndZeroes(lexical, ch, false, end);
            }
            int ptr = mNextPtr;

            // Otherwise, need to verify that is [digit*][ws*]
            int charsLeft = end - ptr;
            if (charsLeft == 0) {
                mValue = neg ? -nr : nr;
                return;
            }
            verifyDigits(lexical, ptr, end);
            // Note: charsLeft one less than total length (skipped first digit)
            if (charsLeft <= 8) { // no overflow
                int i = parseInt(nr, lexical, ptr, ptr + charsLeft);
                mValue = neg ? -i : i;
                return;
            }
            // Otherwise, may have overflow
            // Max 10 digits for a legal int
            if (charsLeft == 9 && nr < 3) { // min/max is ~2 billion (+/-)
                long base = L_BILLION;
                if (nr == 2) {
                    base += L_BILLION;
                }
                int i = parseInt(lexical, ptr, ptr + charsLeft);
                long l = base + (long) i;
                if (neg) {
                    l = -l;
                    if (l >= L_MIN_INT) {
                        mValue = (int) l;
                        return;
                    }
                } else {
                    if (l <= L_MAX_INT) {
                        mValue = (int) l;
                        return;
                    }
                }
            }
            throw new IllegalArgumentException(
                "value \"" + lexicalDesc(lexical) + "\" not a valid 32-bit integer: overflow.");

        }

        @Override
        public void decode(char[] lexical, final int start, final int end) throws IllegalArgumentException {
            char ch = lexical[start];
            boolean neg = (ch == '-');
            int nr;

            if (neg || (ch == '+')) {
                nr = skipSignAndZeroes(lexical, ch, true, start, end);
            } else {
                nr = skipSignAndZeroes(lexical, ch, false, start, end);
            }
            int ptr = mNextPtr;

            // Quick check for short (single-digit) values:
            int charsLeft = end - ptr;
            if (charsLeft == 0) {
                mValue = neg ? -nr : nr;
                return;
            }
            verifyDigits(lexical, start, end, ptr);
            // Note: charsLeft one less than total length (skipped first digit)
            // Can parse more cheaply, if it's really just an int...
            if (charsLeft <= 8) { // no overflow
                int i = parseInt(nr, lexical, ptr, ptr + charsLeft);
                mValue = neg ? -i : i;
                return;
            }
            // Otherwise, may have overflow
            // Max 10 digits for a legal int
            if (charsLeft == 9 && nr < 3) { // min/max is ~2 billion (+/-)
                long base = L_BILLION;
                if (nr == 2) {
                    base += L_BILLION;
                }
                int i = parseInt(lexical, ptr, ptr + charsLeft);
                long l = base + (long) i;
                if (neg) {
                    l = -l;
                    if (l >= L_MIN_INT) {
                        mValue = (int) l;
                        return;
                    }
                } else {
                    if (l <= L_MAX_INT) {
                        mValue = (int) l;
                        return;
                    }
                }
            }
            throw new IllegalArgumentException(
                "value \"" + lexicalDesc(lexical, start, end) + "\" not a valid 32-bit integer: overflow.");
        }
    }

    public final static class LongDecoder extends DecoderBase {
        private long mValue;

        public LongDecoder() {
        }

        @Override
        public String getType() {
            return "long";
        }

        public long getValue() {
            return mValue;
        }

        @Override
        public void decode(String lexical) throws IllegalArgumentException {
            final int end = lexical.length();
            char ch = lexical.charAt(0);
            boolean neg = (ch == '-');
            int nr;

            if (neg || (ch == '+')) {
                nr = skipSignAndZeroes(lexical, ch, true, end);
            } else {
                nr = skipSignAndZeroes(lexical, ch, false, end);
            }
            int ptr = mNextPtr;

            // Quick check for short (single-digit) values:
            int charsLeft = end - ptr;
            if (charsLeft == 0) {
                mValue = neg ? -nr : nr;
                return;
            }
            verifyDigits(lexical, ptr, end);
            // Note: charsLeft one less than total length (skipped first digit)
            // Can parse more cheaply, if it's really just an int...
            if (charsLeft <= 8) { // no overflow
                int i = parseInt(nr, lexical, ptr, ptr + charsLeft);
                mValue = neg ? -i : i;
                return;
            }
            // At this point, let's just push back the first digit... simpler
            --ptr;
            ++charsLeft;

            // Still simple long?
            if (charsLeft <= 18) {
                long l = parseLong(lexical, ptr, ptr + charsLeft);
                mValue = neg ? -l : l;
                return;
            }
            /* Otherwise, let's just fallback to an expensive option,
             * BigInteger. While relatively inefficient, it's simple
             * to use, reliable etc.
             */
            mValue = parseUsingBD(lexical.substring(ptr, ptr + charsLeft), neg);
        }

        @Override
        public void decode(char[] lexical, final int start, final int end) throws IllegalArgumentException {
            char ch = lexical[start];
            boolean neg = (ch == '-');
            int nr;

            if (neg || (ch == '+')) {
                nr = skipSignAndZeroes(lexical, ch, true, start, end);
            } else {
                nr = skipSignAndZeroes(lexical, ch, false, start, end);
            }
            int ptr = mNextPtr;

            // Quick check for short (single-digit) values:
            int charsLeft = end - ptr;
            if (charsLeft == 0) {
                mValue = neg ? -nr : nr;
                return;
            }
            verifyDigits(lexical, start, end, ptr);
            // Note: charsLeft one less than total length (skipped first digit)
            // Can parse more cheaply, if it's really just an int...
            if (charsLeft <= 8) { // no overflow
                int i = parseInt(nr, lexical, ptr, ptr + charsLeft);
                mValue = neg ? -i : i;
                return;
            }
            // At this point, let's just push back the first digit... simpler
            --ptr;
            ++charsLeft;

            // Still simple long?
            if (charsLeft <= 18) {
                long l = parseLong(lexical, ptr, ptr + charsLeft);
                mValue = neg ? -l : l;
                return;
            }

            // Otherwise, let's just fallback to an expensive option
            mValue = parseUsingBD(new String(lexical, ptr, charsLeft), neg);
        }

        private long parseUsingBD(String lexical, boolean neg) {
            BigInteger bi = new BigInteger(lexical);

            // But we may over/underflow, let's check:
            if (neg) {
                bi = bi.negate();
                if (bi.compareTo(BD_MIN_LONG) >= 0) {
                    return bi.longValue();
                }
            } else {
                if (bi.compareTo(BD_MAX_LONG) <= 0) {
                    return bi.longValue();
                }
            }

            throw new IllegalArgumentException("value \"" + lexicalDesc(lexical) + "\" not a valid long: overflow.");
        }
    }

    public final static class FloatDecoder extends DecoderBase {
        private float mValue;

        public FloatDecoder() {
        }

        @Override
        public String getType() {
            return "float";
        }

        public float getValue() {
            return mValue;
        }

        @Override
        public void decode(String lexical) throws IllegalArgumentException {
            /* Then, leading digit; or one of 3 well-known constants
             * (INF, -INF, NaN)
             */
            int len = lexical.length();
            if (len == 3) {
                char c = lexical.charAt(0);
                if (c == 'I') {
                    if (lexical.charAt(1) == 'N' && lexical.charAt(2) == 'F') {
                        mValue = Float.POSITIVE_INFINITY;
                        return;
                    }
                } else if (c == 'N') {
                    if (lexical.charAt(1) == 'a' && lexical.charAt(2) == 'N') {
                        mValue = Float.NaN;
                        return;
                    }
                }
            } else if (len == 4) {
                char c = lexical.charAt(0);
                if (c == '-') {
                    if (lexical.charAt(1) == 'I' && lexical.charAt(2) == 'N' && lexical.charAt(3) == 'F') {
                        mValue = Float.NEGATIVE_INFINITY;
                        return;
                    }
                }
            }

            try {
                mValue = Float.parseFloat(lexical);
            } catch (NumberFormatException nex) {
                throw constructInvalidValue(lexical);
            }
        }

        @Override
        public void decode(char[] lexical, int start, int end) throws IllegalArgumentException {
            int len = end - start;

            if (len == 3) {
                char c = lexical[start];
                if (c == 'I') {
                    if (lexical[start + 1] == 'N' && lexical[start + 2] == 'F') {
                        mValue = Float.POSITIVE_INFINITY;
                        return;
                    }
                } else if (c == 'N') {
                    if (lexical[start + 1] == 'a' && lexical[start + 2] == 'N') {
                        mValue = Float.NaN;
                        return;
                    }
                }
            } else if (len == 4) {
                char c = lexical[start];
                if (c == '-') {
                    if (lexical[start + 1] == 'I' && lexical[start + 2] == 'N' && lexical[start + 3] == 'F') {
                        mValue = Float.NEGATIVE_INFINITY;
                        return;
                    }
                }
            }

            String lexicalStr = new String(lexical, start, len);
            try {
                mValue = Float.parseFloat(lexicalStr);
            } catch (NumberFormatException nex) {
                throw constructInvalidValue(lexicalStr);
            }
        }
    }

    public final static class DoubleDecoder extends DecoderBase {
        private double mValue;

        public DoubleDecoder() {
        }

        @Override
        public String getType() {
            return "double";
        }

        public double getValue() {
            return mValue;
        }

        @Override
        public void decode(String lexical) throws IllegalArgumentException {
            /* Then, leading digit; or one of 3 well-known constants
             * (INF, -INF, NaN)
             */
            int len = lexical.length();
            if (len == 3) {
                char c = lexical.charAt(0);
                if (c == 'I') {
                    if (lexical.charAt(1) == 'N' && lexical.charAt(2) == 'F') {
                        mValue = Double.POSITIVE_INFINITY;
                        return;
                    }
                } else if (c == 'N') {
                    if (lexical.charAt(1) == 'a' && lexical.charAt(2) == 'N') {
                        mValue = Double.NaN;
                        return;
                    }
                }
            } else if (len == 4) {
                char c = lexical.charAt(0);
                if (c == '-') {
                    if (lexical.charAt(1) == 'I' && lexical.charAt(2) == 'N' && lexical.charAt(3) == 'F') {
                        mValue = Double.NEGATIVE_INFINITY;
                        return;
                    }
                }
            }

            try {
                mValue = Double.parseDouble(lexical);
            } catch (NumberFormatException nex) {
                throw constructInvalidValue(lexical);
            }
        }

        @Override
        public void decode(char[] lexical, int start, int end) throws IllegalArgumentException {
            int len = end - start;

            if (len == 3) {
                char c = lexical[start];
                if (c == 'I') {
                    if (lexical[start + 1] == 'N' && lexical[start + 2] == 'F') {
                        mValue = Double.POSITIVE_INFINITY;
                        return;
                    }
                } else if (c == 'N') {
                    if (lexical[start + 1] == 'a' && lexical[start + 2] == 'N') {
                        mValue = Double.NaN;
                        return;
                    }
                }
            } else if (len == 4) {
                char c = lexical[start];
                if (c == '-') {
                    if (lexical[start + 1] == 'I' && lexical[start + 2] == 'N' && lexical[start + 3] == 'F') {
                        mValue = Double.NEGATIVE_INFINITY;
                        return;
                    }
                }
            }

            String lexicalStr = new String(lexical, start, len);
            try {
                mValue = Double.parseDouble(lexicalStr);
            } catch (NumberFormatException nex) {
                throw constructInvalidValue(lexicalStr);
            }
        }
    }

    /*
    /////////////////////////////////////////////////////
    // Decoders, other scalars
    /////////////////////////////////////////////////////
    */

    public final static class IntegerDecoder extends DecoderBase {
        private BigInteger mValue;

        public IntegerDecoder() {
        }

        @Override
        public String getType() {
            return "integer";
        }

        public BigInteger getValue() {
            return mValue;
        }

        @Override
        public void decode(String lexical) throws IllegalArgumentException {
            try {
                mValue = new BigInteger(lexical);
            } catch (NumberFormatException nex) {
                throw constructInvalidValue(lexical);
            }
        }

        @Override
        public void decode(char[] lexical, int start, int end) throws IllegalArgumentException {
            String lexicalStr = new String(lexical, start, (end - start));
            try {
                mValue = new BigInteger(lexicalStr);
            } catch (NumberFormatException nex) {
                throw constructInvalidValue(lexicalStr);
            }
        }
    }

    public final static class DecimalDecoder extends DecoderBase {
        private BigDecimal mValue;

        public DecimalDecoder() {
        }

        @Override
        public String getType() {
            return "decimal";
        }

        public BigDecimal getValue() {
            return mValue;
        }

        @Override
        public void decode(String lexical) throws IllegalArgumentException {
            try {
                mValue = new BigDecimal(lexical);
            } catch (NumberFormatException nex) {
                throw constructInvalidValue(lexical);
            }
        }

        @Override
        public void decode(char[] lexical, int start, int end) throws IllegalArgumentException {
            int len = end - start;
            try {
                /* !!! 21-Nov-2008, TSa: This constructor was added in JDK1.5
                 *   so can't yet be used (As of Woodstox 4.x).
                 *   Need to use the older constructor for now
                 */
                //mValue = new BigDecimal(lexical, start, len);
                mValue = new BigDecimal(new String(lexical, start, len));
            } catch (NumberFormatException nex) {
                throw constructInvalidValue(new String(lexical, start, len));
            }
        }
    }

    public final static class QNameDecoder extends DecoderBase {
        final NamespaceContext mNsCtxt;

        private QName mValue;

        public QNameDecoder(NamespaceContext nsc) {
            mNsCtxt = nsc;
        }

        @Override
        public String getType() {
            return "QName";
        }

        public QName getValue() {
            return mValue;
        }

        @Override
        public void decode(String lexical) throws IllegalArgumentException {
            int ix = lexical.indexOf(':');
            if (ix >= 0) { // qualified name
                mValue = resolveQName(lexical.substring(0, ix), lexical.substring(ix + 1));
            } else {
                mValue = resolveQName(lexical);
            }
        }

        @Override
        public void decode(char[] lexical, int start, int end) throws IllegalArgumentException {
            int i = start;
            for (; i < end; ++i) {
                if (lexical[i] == ':') {
                    mValue
                        = resolveQName(new String(lexical, start, i - start), new String(lexical, i + 1, end - i - 1));
                    return;
                }
            }
            mValue = resolveQName(new String(lexical, start, end - start));
        }

        private QName resolveQName(String localName) throws IllegalArgumentException {
            // No prefix -> default namespace ("element rules")
            String uri = mNsCtxt.getNamespaceURI("");
            if (uri == null) { // some impls may return null
                uri = "";
            }
            return new QName(uri, localName);
        }

        private QName resolveQName(String prefix, String localName) throws IllegalArgumentException {
            if (prefix.isEmpty() || localName.isEmpty()) {
                // either prefix or local name is empty String, illegal
                throw constructInvalidValue(prefix + ":" + localName);
            }
            /* Explicit prefix, must map to a bound namespace; and that
             * namespace can not be empty (only "no prefix", i.e. 'default
             * namespace' has empty URI)
             */
            String uri = mNsCtxt.getNamespaceURI(prefix);
            if (uri == null || uri.isEmpty()) {
                throw new IllegalArgumentException("Value \"" + lexicalDesc(prefix + ":" + localName)
                    + "\" not a valid QName: prefix '" + prefix + "' not bound to a namespace");
            }
            return new QName(uri, localName, prefix);
        }
    }

    /*
    /////////////////////////////////////////////////////
    // Decoders, array
    /////////////////////////////////////////////////////
    */

    /**
     * Intermediate shared base class for token array decoders.
     * The most important additional part is the abstract method
     * that can be used to expand storage space; this is needed
     * when decoding attribute values when all values must fit
     * in the result array.
     */
    public abstract static class BaseArrayDecoder extends TypedArrayDecoder {
        /**
         * Let's use some modest array size for allocating initial
         * result buffer
         */
        protected final static int INITIAL_RESULT_BUFFER_SIZE = 40;

        /**
         * When expanding 'small' result buffers, we will expand
         * size by bigger factor than for larger ones.
         */
        protected final static int SMALL_RESULT_BUFFER_SIZE = 4000;

        protected int mStart;

        protected int mEnd;

        protected int mCount = 0;

        protected BaseArrayDecoder(int start, int maxCount) {
            mStart = start;
            // First, sanity check
            if (maxCount < 1) {
                throw new IllegalArgumentException("Number of elements to read can not be less than 1");
            }
            mEnd = maxCount;
        }

        @Override
        public final int getCount() {
            return mCount;
        }

        @Override
        public final boolean hasRoom() {
            return mCount < mEnd;
        }

        /**
         * Method that can be called if the internal result buffer
         * fills up (when {@link #hasRoom} returns false) and
         * will expand result buffer to hold at least one more value.
         */
        public abstract void expand();

        protected int calcNewSize(int currSize) {
            if (currSize < SMALL_RESULT_BUFFER_SIZE) {
                return currSize << 2; // 4 x current for small bufs
            }
            return currSize + currSize; // 2x for bigger
        }
    }

    public final static class IntArrayDecoder extends BaseArrayDecoder {
        int[] mResult;

        final IntDecoder mDecoder;

        /**
         * Constructor used for constructing decoders with fixed pre-allocated
         * result buffer.
         */
        public IntArrayDecoder(int[] result, int start, int maxCount, IntDecoder intDecoder) {
            super(start, maxCount);
            mResult = result;
            mDecoder = intDecoder;
        }

        /**
         * Constructor used for constructing decoders with automatically
         * adjusting result buffer
         */
        public IntArrayDecoder(IntDecoder intDecoder) {
            super(0, INITIAL_RESULT_BUFFER_SIZE);
            mResult = new int[INITIAL_RESULT_BUFFER_SIZE];
            mDecoder = intDecoder;
        }

        @Override
        public void expand() {
            int[] old = mResult;
            int oldLen = old.length;
            int newSize = calcNewSize(oldLen);
            mResult = new int[newSize];
            System.arraycopy(old, mStart, mResult, 0, oldLen);
            mStart = 0;
            mEnd = newSize;
        }

        public int[] getValues() {
            int[] result = new int[mCount];
            // !!! TBI: with JDK 6, use Arrays.copyOf:
            System.arraycopy(mResult, mStart, result, 0, mCount);
            return result;
        }

        @Override
        public boolean decodeValue(String input) throws IllegalArgumentException {
            mDecoder.decode(input);
            mResult[mStart + mCount] = mDecoder.getValue();
            return (++mCount >= mEnd);
        }

        @Override
        public boolean decodeValue(char[] buffer, int start, int end) throws IllegalArgumentException {
            mDecoder.decode(buffer, start, end);
            mResult[mStart + mCount] = mDecoder.getValue();
            return (++mCount >= mEnd);
        }

    }

    public final static class LongArrayDecoder extends BaseArrayDecoder {
        long[] mResult;

        final LongDecoder mDecoder;

        public LongArrayDecoder(long[] result, int start, int maxCount, LongDecoder longDecoder) {
            super(start, maxCount);
            mResult = result;
            mDecoder = longDecoder;
        }

        public LongArrayDecoder(LongDecoder longDecoder) {
            super(0, INITIAL_RESULT_BUFFER_SIZE);
            mResult = new long[INITIAL_RESULT_BUFFER_SIZE];
            mDecoder = longDecoder;
        }

        @Override
        public void expand() {
            long[] old = mResult;
            int oldLen = old.length;
            int newSize = calcNewSize(oldLen);
            mResult = new long[newSize];
            System.arraycopy(old, mStart, mResult, 0, oldLen);
            mStart = 0;
            mEnd = newSize;
        }

        public long[] getValues() {
            long[] result = new long[mCount];
            System.arraycopy(mResult, mStart, result, 0, mCount);
            return result;
        }

        @Override
        public boolean decodeValue(String input) throws IllegalArgumentException {
            mDecoder.decode(input);
            mResult[mStart + mCount] = mDecoder.getValue();
            return (++mCount >= mEnd);
        }

        @Override
        public boolean decodeValue(char[] buffer, int start, int end) throws IllegalArgumentException {
            mDecoder.decode(buffer, start, end);
            mResult[mStart + mCount] = mDecoder.getValue();
            return (++mCount >= mEnd);
        }
    }

    public final static class FloatArrayDecoder extends BaseArrayDecoder {
        float[] mResult;

        final FloatDecoder mDecoder;

        public FloatArrayDecoder(float[] result, int start, int maxCount, FloatDecoder floatDecoder) {
            super(start, maxCount);
            mResult = result;
            mDecoder = floatDecoder;
        }

        public FloatArrayDecoder(FloatDecoder floatDecoder) {
            super(0, INITIAL_RESULT_BUFFER_SIZE);
            mResult = new float[INITIAL_RESULT_BUFFER_SIZE];
            mDecoder = floatDecoder;
        }

        @Override
        public void expand() {
            float[] old = mResult;
            int oldLen = old.length;
            int newSize = calcNewSize(oldLen);
            mResult = new float[newSize];
            System.arraycopy(old, mStart, mResult, 0, oldLen);
            mStart = 0;
            mEnd = newSize;
        }

        public float[] getValues() {
            float[] result = new float[mCount];
            System.arraycopy(mResult, mStart, result, 0, mCount);
            return result;
        }

        @Override
        public boolean decodeValue(String input) throws IllegalArgumentException {
            mDecoder.decode(input);
            mResult[mStart + mCount] = mDecoder.getValue();
            return (++mCount >= mEnd);
        }

        @Override
        public boolean decodeValue(char[] buffer, int start, int end) throws IllegalArgumentException {
            mDecoder.decode(buffer, start, end);
            mResult[mStart + mCount] = mDecoder.getValue();
            return (++mCount >= mEnd);
        }
    }

    public final static class DoubleArrayDecoder extends BaseArrayDecoder {
        double[] mResult;

        final DoubleDecoder mDecoder;

        public DoubleArrayDecoder(double[] result, int start, int maxCount, DoubleDecoder doubleDecoder) {
            super(start, maxCount);
            mResult = result;
            mDecoder = doubleDecoder;
        }

        public DoubleArrayDecoder(DoubleDecoder doubleDecoder) {
            super(0, INITIAL_RESULT_BUFFER_SIZE);
            mResult = new double[INITIAL_RESULT_BUFFER_SIZE];
            mDecoder = doubleDecoder;
        }

        @Override
        public void expand() {
            double[] old = mResult;
            int oldLen = old.length;
            int newSize = calcNewSize(oldLen);
            mResult = new double[newSize];
            System.arraycopy(old, mStart, mResult, 0, oldLen);
            mStart = 0;
            mEnd = newSize;
        }

        public double[] getValues() {
            double[] result = new double[mCount];
            System.arraycopy(mResult, mStart, result, 0, mCount);
            return result;
        }

        @Override
        public boolean decodeValue(String input) throws IllegalArgumentException {
            mDecoder.decode(input);
            mResult[mStart + mCount] = mDecoder.getValue();
            return (++mCount >= mEnd);
        }

        @Override
        public boolean decodeValue(char[] buffer, int start, int end) throws IllegalArgumentException {
            mDecoder.decode(buffer, start, end);
            mResult[mStart + mCount] = mDecoder.getValue();
            return (++mCount >= mEnd);
        }
    }
}
