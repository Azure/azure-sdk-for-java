// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

public class MurmurHash3_128 {

    public static UInt128 hash128(byte[] bytes) {
        return hash128(bytes, bytes.length, new UInt128(0, 0));
    }

    public static UInt128 hash128(byte[] bytes, int length, UInt128 seed) {
        final long c1 = 0x87c37b91114253d5L;
        final long c2 = 0x4cf5ad432745937fL;

        long h1 = seed.high;
        long h2 = seed.low;

        // body
        int position;
        for (position = 0; position < length - 15; position += 16) {
            long k1 = getLittleEndianLong(bytes, position);
            long k2 = getLittleEndianLong(bytes, position + 8);

            // k1, h1
            k1 *= c1;
            k1 = rotateLeft64(k1, 31);
            k1 *= c2;

            h1 ^= k1;
            h1 = rotateLeft64(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;

            // k2, h2
            k2 *= c2;
            k2 = rotateLeft64(k2, 33);
            k2 *= c1;

            h2 ^= k2;
            h2 = rotateLeft64(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }


        {
            // tail
            long k1 = 0;
            long k2 = 0;

            int n = length & 15;
            if (n >= 15) k2 ^= (bytes[position + 14] & 0xffL) << 48;
            if (n >= 14) k2 ^= (bytes[position + 13] & 0xffL) << 40;
            if (n >= 13) k2 ^= (bytes[position + 12] & 0xffL) << 32;
            if (n >= 12) k2 ^= (bytes[position + 11] & 0xffL) << 24;
            if (n >= 11) k2 ^= (bytes[position + 10] & 0xffL) << 16;
            if (n >= 10) k2 ^= (bytes[position + 9] & 0xffL) << 8;
            if (n >= 9) k2 ^= (bytes[position + 8] & 0xffL) << 0;

            k2 *= c2;
            k2 = rotateLeft64(k2, 33);
            k2 *= c1;
            h2 ^= k2;

            if (n >= 8) k1 ^= (bytes[position + 7] & 0xffL) << 56;
            if (n >= 7) k1 ^= (bytes[position + 6] & 0xffL) << 48;
            if (n >= 6) k1 ^= (bytes[position + 5] & 0xffL) << 40;
            if (n >= 5) k1 ^= (bytes[position + 4] & 0xffL) << 32;
            if (n >= 4) k1 ^= (bytes[position + 3] & 0xffL) << 24;
            if (n >= 3) k1 ^= (bytes[position + 2] & 0xffL) << 16;
            if (n >= 2) k1 ^= (bytes[position + 1] & 0xffL) << 8;
            if (n >= 1) k1 ^= (bytes[position + 0] & 0xffL) << 0;

            k1 *= c1;
            k1 = rotateLeft64(k1, 31);
            k1 *= c2;
            h1 ^= k1;
        }

        // finalization
        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        // h1
        h1 ^= h1 >>> 33;
        h1 *= 0xff51afd7ed558ccdL;
        h1 ^= h1 >>> 33;
        h1 *= 0xc4ceb9fe1a85ec53L;
        h1 ^= h1 >>> 33;

        // h2
        h2 ^= h2 >>> 33;
        h2 *= 0xff51afd7ed558ccdL;
        h2 ^= h2 >>> 33;
        h2 *= 0xc4ceb9fe1a85ec53L;
        h2 ^= h2 >>> 33;

        h1 += h2;
        h2 += h1;

        h1 = Long.reverseBytes(h1);
        h2 = Long.reverseBytes(h2);

        return new UInt128(h1, h2);
    }


    private static int rotateLeft32(int n, int numBits) {
        assert numBits < 32;
        return Integer.rotateLeft(n, numBits);
    }

    private static long rotateLeft64(long n, int numBits) {
        assert numBits < 64;
        return Long.rotateLeft(n, numBits);
    }

    private static final long getLittleEndianLong(byte[] bytes, int offset) {
        return ((long) bytes[offset + 7] << 56)   // no mask needed
                | ((bytes[offset + 6] & 0xffL) << 48)
                | ((bytes[offset + 5] & 0xffL) << 40)
                | ((bytes[offset + 4] & 0xffL) << 32)
                | ((bytes[offset + 3] & 0xffL) << 24)
                | ((bytes[offset + 2] & 0xffL) << 16)
                | ((bytes[offset + 1] & 0xffL) << 8)
                | ((bytes[offset] & 0xffL));
    }

    private static int intAsLittleIndian(byte[] bytes, int i) {
        return (bytes[i] & 0xff) | ((bytes[i + 1] & 0xff) << 8) | ((bytes[i + 2] & 0xff) << 16) | (bytes[i + 3] << 24);
    }
}
