/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.routing;


import java.math.BigInteger;

public class Int128 {

    private final BigInteger value;

    private static final BigInteger MaxBigIntValue =
            new BigInteger(new byte[] {
                    (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                    (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
            });

    public static final Int128 MaxValue = new Int128(
            new BigInteger(new byte[] {
                    (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            })
    );

    private Int128(BigInteger value) {
        this.value = value.remainder(MaxBigIntValue);
    }

    public Int128(int n) {
        this(BigInteger.valueOf(n));
    }

    public Int128(byte[] data) {
        if (data.length != 16) {
            throw new IllegalArgumentException("data");
        }

        this.value = new BigInteger(data);

        if (this.value.compareTo(MaxValue.value) > 0) {
            throw new IllegalArgumentException();
        }
    }

    public static Int128 multiply(Int128 left, Int128 right) {
        return new Int128(left.value.multiply(right.value));
    }

    public static Int128 add(Int128 left, Int128 right) {
        return new Int128(left.value.add(right.value));
    }

    public static Int128 subtract(Int128 left, Int128 right) {
        return new Int128(left.value.subtract(right.value));
    }

    public static Int128 div (Int128 left, Int128 right) {
        return new Int128(left.value.divide(right.value));
    }

    public static boolean gt(Int128 left, Int128 right) {
        return left.value.compareTo(right.value) > 0;
    }

    public static boolean lt(Int128 left, Int128 right) {
        return left.value.compareTo(right.value) < 0;
    }

    public byte[] bytes() {
        byte[] bytes = this.value.toByteArray();
        if (bytes.length < 16) {
            byte[] paddedBytes = new byte[16];
            System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length);
            return paddedBytes;
        }

        return bytes;
    }

}
