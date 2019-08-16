// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import java.util.Arrays;

class ByteExtensions {

    static byte[] or(byte[] self, byte[] other) {
        return or(self, other, 0);
    }

    static byte[] or(byte[] self, byte[] other, int offset) {
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

    static byte[] xor(byte[] self, byte[] other) {
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

    static void zero(byte[] self) {
        if (self != null) {
            Arrays.fill(self, (byte) 0);
        }
    }

    /*
     * Compares two byte arrays in constant time.
     *
     * @param self
     *      The first byte array to compare
     * @param other
     *      The second byte array to compare
     * @return
     *      True if the two byte arrays are equal.
     */
    static boolean sequenceEqualConstantTime(byte[] self, byte[] other) {
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
}
