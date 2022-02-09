// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import java.nio.ByteBuffer;
import java.util.Objects;

public class UInt128 implements Comparable<UInt128>{
    private static final int SIZE  = Long.SIZE + Long.SIZE;
    private static final int BYTES = SIZE / Byte.SIZE;
    public static final UInt128 ZERO = new UInt128(0l, 0l);

    final long low;
    final long high;

    public UInt128(long x, long y) {
        this.low = x;
        this.high = y;
    }

    public UInt128 (ByteBuffer byteBuffer) {
        byteBuffer.rewind();
        this.low = byteBuffer.getLong();
        this.high = byteBuffer.getLong();
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof UInt128) {
            final UInt128 uInt128Other = (UInt128) other;
            return this.low == uInt128Other.low && this.high == uInt128Other.high;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(BYTES);
        byteBuffer.putLong(low).putLong(high);
        return byteBuffer;
    }

    @Override
    public String toString() {
        return toByteBuffer().toString();
    }

    public long getLow() {
        return low;
    }

    public long getHigh() {
        return high;
    }

    public UInt128 add(int value) {
        UInt128 add = new UInt128(value, 0);
        long low = this.low + add.low;
        long high = this.high + add.high;

        if (low < add.low)
        {
            high++;
        }

        return new UInt128(low, high);
    }

    public UInt128 xor(UInt128 other) {
        return new UInt128(this.low ^ other.low, this.high ^ other.high);
    }

    @Override
    public int compareTo(UInt128 other) {
        if (this.equals(other)) {
            return 0;
        }
        if ((this.high < other.high) || ((this.high == other.high) && (this.low < other.low))) {
            return -1;
        }
        return 1;
    }
}
