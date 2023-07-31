// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.json;

import java.util.Objects;

//todo : capitalize function names
/**
 * Class that represents either a double or a 64-bit int.
 */
public final class Number64 implements Comparable<Number64> { //todo see if "Equatable" is a thing

    /**
     * Maximum Number64.
     */
    public static final Number64 MaxValue = new Number64(Double.MAX_VALUE);

    /**
     * Minimum Number64.
     */
    public static final Number64 MinValue = new Number64(Double.MIN_VALUE);

    /**
     * The double if the value is a double.
     */
    private final Double doubleValue;

    /**
     * The long if the value is a long.
     */
    private final Long longValue;

    private Number64(double value) {
        this.doubleValue = value;
        this.longValue = null;
    }

    private Number64(long value) {
        this.longValue = value;
        this.doubleValue = null;
    }

    /**
     * Check if the value is an integer.
     *
     * @return true if the value is an integer, false otherwise.
     */
    public boolean isInteger() {
        return this.longValue != null;
    }

    /**
     * Check if the value is a double.
     *
     * @return true if the value is a double, false otherwise.
     */
    public boolean isDouble() {
        return this.doubleValue != null;
    }

    /**
     * Check if the value is positive infinity or negative infinity.
     *
     * @return true if the value is positive infinity or negative infinity, false otherwise.
     */
    public boolean isInfinity() {
        return !isInteger() && Double.isInfinite(this.doubleValue);
    }

    /**
     * Check if the value is NaN (Not a Number).
     *
     * @return true if the value is NaN, false otherwise.
     */
    public boolean isNaN() {
        return !isInteger() && Double.isNaN(this.doubleValue);
    }

    /**
     * Convert the value to a string.
     *
     * @return The string representation of the value.
     */
    @Override
    public String toString() {
        String toString;
        if (isDouble()) {
            toString = Double.toString(this.doubleValue);
        } else {
            toString = Long.toString(this.longValue);
        }
        return toString;
    }

    /**
     * Returns whether this Number64 is less than another Number64.
     *
     * @param other the other Number64 to compare to.
     * @return true if this Number64 is less than the other, false otherwise.
     */
    public boolean lessThan(Number64 other) {
        return compareTo(other) < 0;
    }

    /**
     * Returns whether this Number64 is greater than another Number64.
     *
     * @param other the other Number64 to compare to.
     * @return true if this Number64 is greater than the other, false otherwise.
     */
    public boolean greaterThan(Number64 other) {
        return compareTo(other) > 0;
    }

    /**
     * Returns whether this Number64 is less than or equal to another Number64.
     *
     * @param other the other Number64 to compare to.
     * @return true if this Number64 is less than or equal to the other, false otherwise.
     */
    public boolean lessThanOrEqual(Number64 other) {
        return !(this.greaterThan(other));
    }

    /**
     * Returns whether this Number64 is greater than or equal to another Number64.
     *
     * @param other the other Number64 to compare to.
     * @return true if this Number64 is greater than or equal to the other, false otherwise.
     */
    public boolean greaterThanOrEqual(Number64 other) {
        return !(this.lessThan(other));
    }

    /**
     * Returns whether this Number64 is equal to another Number64.
     *
     * @param other the other Number64 to compare to.
     * @return true if this Number64 is equal to the other, false otherwise.
     */
    public boolean equals(Number64 other) {
        return compareTo(other) == 0;
    }

    /**
     * Implicitly converts a long to Number64.
     *
     * @param value the double to convert.
     * @return the Number64 for the provided long.
     */
    public static Number64 fromLong(long value)
    {
        return new Number64(value);
    }

    /**
     * Implicitly converts a double to Number64.
     *
     * @param value the double to convert.
     * @return the Number64 for the provided double.
     */
    public static Number64 fromDouble(double value)
    {
        return new Number64(value);
    }

    public static long toLong(Number64 number64)
    {
        long value;
        if(number64.isInteger())
        {
            value = number64.longValue;
        }
        else
        {
            value = number64.doubleValue.longValue();
        }
        return value;
    }

    public static double toDouble(Number64 number64)
    {
        double value;
        if(number64.isDouble())
        {
            value = number64.doubleValue;
        }
        else
        {
            value = number64.longValue.doubleValue();
        }
        return value;
    }

    //todo: everything past this line needs work
    public static DoubleEx toDoubleEx(Number64 number64)
    {
        DoubleEx doubleEx;
        if (number64.isDouble())
        {
            doubleEx = number64.doubleValue;
        }
        else
        {
            doubleEx = number64.longValue;
        }

        return doubleEx;
    }

    /**
     * Compare this Number64 to another instance of the Number64 type.
     *
     * @param other The other instance to compare to.
     * @return A negative number if this instance is less than the other instance,
     *         zero if they are the same,
     *         a positive number if this instance is greater than the other instance.
     */
    @Override
    public int compareTo(Number64 other) {
        int comparison;
        if (isInteger() && other.isInteger()) {
            comparison = this.longValue.compareTo(other.longValue);
        } else if (isDouble() && other.isDouble()) {
            comparison = this.doubleValue.compareTo(other.doubleValue);
        } else {
            // Convert both to DoubleEx and compare
            DoubleEx first = isDouble() ? new DoubleEx(this.doubleValue, 0) : new DoubleEx(this.longValue.doubleValue(), 0);
            DoubleEx second = other.isDouble() ? new DoubleEx(other.doubleValue, 0) : new DoubleEx(other.longValue.doubleValue(), 0);
            comparison = first.compareTo(second);
        }
        return comparison;
    }

    /**
     * Returns whether this Number64 equals another Number64.
     *
     * @param other The Number64 to compare to.
     * @return Whether this Number64 equals another Number64.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Number64) {
            return this.compareTo((Number64) other) == 0;
        }
        return false;
    }

    /**
     * Gets a hash code for this instance.
     *
     * @return The hash code for this instance.
     */
    @Override
    public int hashCode() { //todo: get hash code?
        DoubleEx doubleEx = toDoubleEx();
        return Objects.hash(doubleEx.doubleValue, doubleEx.extraBits);
    }

    /**
     * Represents an extended double number with 62-bit mantissa
     * which is capable of representing a 64-bit integer with no precision loss.
     */
    public final class DoubleEx implements Comparable<DoubleEx> {

        private final double doubleValue;
        private final short extraBits;

        private DoubleEx(double doubleValue, short extraBits) {
            this.doubleValue = doubleValue;
            this.extraBits = extraBits;
        }
        /**
         * Get the double value.
         */
        private double getDouble() { return this.doubleValue; }

        /**
         * Get the extra bits.
         */
        private short getExtraBits() { return this.extraBits; }

        @Override
        public int compareTo(DoubleEx other) {
            int compare = Double.compare(this.doubleValue, other.doubleValue);
            if (compare == 0) {
                compare = Short.compare(this.extraBits, other.extraBits) * Double.compare(this.doubleValue, 0);
            }
            return compare;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other instanceof DoubleEx) {
                DoubleEx otherEx = (DoubleEx) other;
                return Double.compare(this.doubleValue, otherEx.doubleValue) == 0
                    && this.extraBits == otherEx.extraBits;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.doubleValue, this.extraBits);
        }

        // Implicit conversions to/from long and double (Similar to the implicit C# operators)
        public static DoubleEx fromLong(long value) {
            if (value == Long.MIN_VALUE) {
                // Special case for Long.MIN_VALUE
                return new DoubleEx(value, (short) 0);
            }

            double doubleValue;
            short extraBits;

            long absValue = Math.abs(value);
            int msbIndex = Documents.BitUtils.getMostSignificantBitIndex((absValue & 0x7FFFFFFFFFFFFFFFL));

            // Check if the integer value spans more than 52 bits (meaning it won't fit in a double's mantissa at full precision)
            if (msbIndex > 52 && (msbIndex - Documents.BitUtils.getLeastSignificantBitIndex(absValue)) > 52) {
                // Retrieve the most significant bit index which is the double exponent value
                int exponentValue = msbIndex;

                long exponentBits = ((long) (exponentValue + 1023)) << 52;

                // Set the mantissa as a 62 bit value (i.e. represents 63-bit number)
                long mantissa = (absValue << (62 - exponentValue)) & 0x3FFFFFFFFFFFFFFFL;

                // Retrieve the least significant 10 bits
                extraBits = (short) ((mantissa & 0x3FF) << 6);

                // Adjust the mantissa to 52 bits
                mantissa = mantissa >> 10;

                long valueBits = exponentBits | mantissa;
                if (value != absValue) {
                    valueBits |= 0x8000000000000000L;
                }

                doubleValue = Double.longBitsToDouble(valueBits);
            } else {
                doubleValue = value;
                extraBits = 0;
            }

            return new DoubleEx(doubleValue, extraBits);
        }

        public long toLong(DoubleEx value) {
            long integerValue;

            if (value.extraBits != 0) {
                long valueBits = Double.doubleToLongBits(value.doubleValue);

                // Retrieve and clear the sign bit
                boolean isNegative = Documents.BitUtils.bitTestAndReset64(valueBits, 63, valueBits);

                // Retrieve the exponent value
                int exponentValue = (int) ((valueBits >> 52) - 1023L);

                // Extend the value to 62 bits
                valueBits <<= 10;

                // Set MSB (i.e. bit 62) and clear the sign bit (left over from the exponent)
                valueBits = (valueBits | 0x4000000000000000L) & 0x7FFFFFFFFFFFFFFFL;

                // Set the extra bits
                valueBits |= (((long) value.extraBits) & 0xFFFF) << 6;

                // Adjust for the exponent
                valueBits >>= (62 - exponentValue);
                if (isNegative) {
                    valueBits = -valueBits;
                }

                integerValue = valueBits;
            } else {
                integerValue = (long) value.doubleValue;
            }

            return integerValue;
        }

        // Implicit conversions to/from long and double (Similar to the implicit C# operators)
        public static DoubleEx fromDouble(double value) {
            return new DoubleEx(value, (short) 0);
        }

        public static double toDouble(DoubleEx value) {
            return value.doubleValue;
        }
    }
}
