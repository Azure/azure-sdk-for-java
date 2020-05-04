// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import java.nio.ByteBuffer;
import java.util.Objects;

public class UInt128 {
    long low;
    long high;

    public UInt128(long x, long y) {
        this.low = x;
        this.high = y;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof UInt128) {
            return this.low == ((UInt128) other).low && this.high == ((UInt128) other).high;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer bf1 = ByteBuffer.allocate(8);
        bf1.putLong(low);
        ByteBuffer bf2 = ByteBuffer.allocate(8);
        bf2.putLong(high);
        ByteBuffer bf3 = ByteBuffer.allocate(bf1.capacity() + bf2.capacity());
        return bf3.put(bf1).put(bf2);
    }
}
