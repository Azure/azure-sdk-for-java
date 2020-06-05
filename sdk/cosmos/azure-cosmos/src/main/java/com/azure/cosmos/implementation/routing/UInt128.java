// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import java.nio.ByteBuffer;
import java.util.Objects;

public class UInt128 {
    private static final int SIZE  = Long.SIZE + Long.SIZE;
    public static final int BYTES = SIZE / Byte.SIZE;
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
}
