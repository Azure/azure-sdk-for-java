// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.cryptography;

import java.util.Arrays;

public final class ByteExtensions {

    public static byte[] or(byte[] self, byte[] other) {
        return or(self, other, 0);
    }

    public static byte[] or(byte[] self, byte[] other, int offset) {
        if (self == null) {
            throw new IllegalArgumentException("self");
        }

        if (other == null) {
            throw new IllegalArgumentException("other");
        }

        if (self.length > other.length - offset) {
            throw new IllegalArgumentException("self and other lengths do not match");
        }

        byte[] result = new byte[self.length];

        for (int i = 0; i < self.length; i++) {
            result[i] = (byte) (self[i] | other[offset + i]);
        }

        return result;
    }

    public static byte[] xor(byte[] self, byte[] other) {
        return xor(self, other, 0);
    }

    static byte[] xor(byte[] self, byte[] other, int offset) {
        if (self == null) {
            throw new IllegalArgumentException("self");
        }

        if (other == null) {
            throw new IllegalArgumentException("other");
        }

        if (self.length > other.length - offset) {
            throw new IllegalArgumentException("self and other lengths do not match");
        }

        byte[] result = new byte[self.length];

        for (int i = 0; i < self.length; i++) {
            result[i] = (byte) (self[i] ^ other[offset + i]);
        }

        return result;
    }

    public static void zero(byte[] self) {
        if (self != null) {
            Arrays.fill(self, (byte) 0);
        }
    }

    /**
     * Compares two byte arrays in constant time.
     *
     * @param self
     *      The first byte array to compare
     * @param other
     *      The second byte array to compare
     * @return
     *      True if the two byte arrays are equal.
     */
    public static boolean sequenceEqualConstantTime(byte[] self, byte[] other) {
        if (self == null) {
            throw new IllegalArgumentException("self");
        }

        if (other == null) {
            throw new IllegalArgumentException("other");
        }

        // Constant time comparison of two byte arrays
        long difference = (self.length & 0xffffffffL) ^ (other.length & 0xffffffffL);

        for (int i = 0; i < self.length && i < other.length; i++) {
            difference |= (self[i] ^ other[i]) & 0xffffffffL;
        }

        return difference == 0;
    }

    /**
     * Creates a copy of the source array.
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    public static byte[] clone(byte[] source) {
        if (source == null) {
            return null;
        }

        byte[] copy = new byte[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);

        return copy;
    }
}
