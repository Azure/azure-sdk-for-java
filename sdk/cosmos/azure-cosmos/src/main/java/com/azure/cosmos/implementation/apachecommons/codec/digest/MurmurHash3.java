/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.apachecommons.codec.digest;

import com.azure.cosmos.implementation.apachecommons.codec.binary.StringUtils;


/**
 * Implementation of the MurmurHash3 32-bit and 128-bit hash functions.
 *
 * <p>
 * MurmurHash is a non-cryptographic hash function suitable for general hash-based lookup. The name comes from two basic
 * operations, multiply (MU) and rotate (R), used in its inner loop. Unlike cryptographic hash functions, it is not
 * specifically designed to be difficult to reverse by an adversary, making it unsuitable for cryptographic purposes.
 * </p>
 *
 * <p>
 * This contains a Java port of the 32-bit hash function {@code MurmurHash3_x86_32} and the 128-bit hash function
 * {@code MurmurHash3_x64_128} from Austin Applyby's original {@code c++} code in SMHasher.
 * </p>
 *
 * <p>
 * This is public domain code with no copyrights. From home page of
 * <a href="https://github.com/aappleby/smhasher">SMHasher</a>:
 * </p>
 *
 * <blockquote> "All MurmurHash versions are public domain software, and the author disclaims all copyright to their
 * code." </blockquote>
 *
 * <p>
 * Original adaption from Apache Hive. That adaption contains a {@code hash64} method that is not part of the original
 * MurmurHash3 code. It is not recommended to use these methods. They will be removed in a future release. To obtain a
 * 64-bit hash use half of the bits from the {@code hash128x64} methods using the input data converted to bytes.
 * <p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/MurmurHash">MurmurHash</a>
 * @see <a href="https://github.com/aappleby/smhasher/blob/master/src/MurmurHash3.cpp"> Original MurmurHash3 c++
 *      code</a>
 * @see <a href=
 *      "https://github.com/apache/hive/blob/master/storage-api/src/java/org/apache/hive/common/util/Murmur3.java">
 *      Apache Hive Murmer3</a>
 * @since 1.13
 */
/**
 * This class is shaded from version 1.14 of apache commons-codec library
 */
public final class MurmurHash3 {

    /**
     * A random number to use for a hash code.
     *
     * @deprecated This is not used internally and will be removed in a future release.
     */
    @Deprecated
    public static final long NULL_HASHCODE = 2862933555777941757L;

    /**
     * A default seed to use for the murmur hash algorithm.
     * Has the value {@code 104729}.
     */
    public static final int DEFAULT_SEED = 104729;

    /** TODO Replace on Java 8 with Long.BYTES. */
    static final int LONG_BYTES = Long.SIZE / Byte.SIZE;

    /** TODO Replace on Java 8 with Integer.BYTES. */
    static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;

    /** TODO Replace on Java 8 with Short.BYTES. */
    static final int SHORT_BYTES = Short.SIZE / Byte.SIZE;

    // Constants for 32-bit variant
    private static final int C1_32 = 0xcc9e2d51;
    private static final int C2_32 = 0x1b873593;
    private static final int R1_32 = 15;
    private static final int R2_32 = 13;
    private static final int M_32 = 5;
    private static final int N_32 = 0xe6546b64;

    // Constants for 128-bit variant
    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;
    private static final int R1 = 31;
    private static final int R2 = 27;
    private static final int R3 = 33;
    private static final int M = 5;
    private static final int N1 = 0x52dce729;
    private static final int N2 = 0x38495ab5;

    /** No instance methods. */
    private MurmurHash3() {
    }

    /**
     * Generates 32-bit hash from two longs with a default seed value.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(16)
     *                                            .putLong(data1)
     *                                            .putLong(data2)
     *                                            .array(), offset, 16, seed);
     * </pre>
     *
     * @param data1 The first long to hash
     * @param data2 The second long to hash
     * @return The 32-bit hash
     * @see #hash32x86(byte[], int, int, int)
     */
    public static int hash32(final long data1, final long data2) {
        return hash32(data1, data2, DEFAULT_SEED);
    }

    /**
     * Generates 32-bit hash from two longs with the given seed.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(16)
     *                                            .putLong(data1)
     *                                            .putLong(data2)
     *                                            .array(), offset, 16, seed);
     * </pre>
     *
     * @param data1 The first long to hash
     * @param data2 The second long to hash
     * @param seed The initial seed value
     * @return The 32-bit hash
     * @see #hash32x86(byte[], int, int, int)
     */
    public static int hash32(final long data1, final long data2, final int seed) {
        int hash = seed;
        final long r0 = Long.reverseBytes(data1);
        final long r1 = Long.reverseBytes(data2);

        hash = mix32((int) r0, hash);
        hash = mix32((int) (r0 >>> 32), hash);
        hash = mix32((int) (r1), hash);
        hash = mix32((int) (r1 >>> 32), hash);

        hash ^= LONG_BYTES * 2;
        return fmix32(hash);
    }

    /**
     * Generates 32-bit hash from a long with a default seed value.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(8)
     *                                            .putLong(data)
     *                                            .array(), offset, 8, seed);
     * </pre>
     *
     * @param data The long to hash
     * @return The 32-bit hash
     * @see #hash32x86(byte[], int, int, int)
     */
    public static int hash32(final long data) {
        return hash32(data, DEFAULT_SEED);
    }

    /**
     * Generates 32-bit hash from a long with the given seed.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(8)
     *                                            .putLong(data)
     *                                            .array(), offset, 8, seed);
     * </pre>
     *
     * @param data The long to hash
     * @param seed The initial seed value
     * @return The 32-bit hash
     * @see #hash32x86(byte[], int, int, int)
     */
    public static int hash32(final long data, final int seed) {
        int hash = seed;
        final long r0 = Long.reverseBytes(data);

        hash = mix32((int) r0, hash);
        hash = mix32((int) (r0 >>> 32), hash);

        hash ^= LONG_BYTES;
        return fmix32(hash);
    }

    /**
     * Generates 32-bit hash from the byte array with a default seed.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * int hash = MurmurHash3.hash32(data, offset, data.length, seed);
     * </pre>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.<p>
     *
     * @param data The input byte array
     * @return The 32-bit hash
     * @see #hash32(byte[], int, int, int)
     * @deprecated Use {@link #hash32x86(byte[], int, int, int)}. This corrects the processing of trailing bytes.
     */
    @Deprecated
    public static int hash32(final byte[] data) {
        return hash32(data, 0, data.length, DEFAULT_SEED);
    }

    /**
     * Generates 32-bit hash from a string with a default seed.
     * <p>
     * Before 1.14 the string was converted using default encoding.
     * Since 1.14 the string is converted to bytes using UTF-8 encoding.
     * </p>
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
     * int hash = MurmurHash3.hash32(bytes, offset, bytes.length, seed);
     * </pre>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.<p>
     *
     * @param data The input string
     * @return The 32-bit hash
     * @see #hash32(byte[], int, int, int)
     * @deprecated Use {@link #hash32x86(byte[], int, int, int)} with the bytes returned from
     * {@link String#getBytes(java.nio.charset.Charset)}. This corrects the processing of trailing bytes.
     */
    @Deprecated
    public static int hash32(final String data) {
        final byte[] bytes = StringUtils.getBytesUtf8(data);
        return hash32(bytes, 0, bytes.length, DEFAULT_SEED);
    }

    /**
     * Generates 32-bit hash from the byte array with the given length and a default seed.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * int hash = MurmurHash3.hash32(data, offset, length, seed);
     * </pre>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.<p>
     *
     * @param data The input byte array
     * @param length The length of array
     * @return The 32-bit hash
     * @see #hash32(byte[], int, int, int)
     * @deprecated Use {@link #hash32x86(byte[], int, int, int)}. This corrects the processing of trailing bytes.
     */
    @Deprecated
    public static int hash32(final byte[] data, final int length) {
        return hash32(data, length, DEFAULT_SEED);
    }

    /**
     * Generates 32-bit hash from the byte array with the given length and seed. This is a
     * helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int hash = MurmurHash3.hash32(data, offset, length, seed);
     * </pre>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.<p>
     *
     * @param data The input byte array
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 32-bit hash
     * @see #hash32(byte[], int, int, int)
     * @deprecated Use {@link #hash32x86(byte[], int, int, int)}. This corrects the processing of trailing bytes.
     */
    @Deprecated
    public static int hash32(final byte[] data, final int length, final int seed) {
        return hash32(data, 0, length, seed);
    }

    /**
     * Generates 32-bit hash from the byte array with the given offset, length and seed.
     *
     * <p>This is an implementation of the 32-bit hash function {@code MurmurHash3_x86_32}
     * from from Austin Applyby's original MurmurHash3 {@code c++} code in SMHasher.</p>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.<p>
     *
     * @param data The input byte array
     * @param offset The offset of data
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 32-bit hash
     * @deprecated Use {@link #hash32x86(byte[], int, int, int)}. This corrects the processing of trailing bytes.
     */
    @Deprecated
    @SuppressWarnings("fallthrough")
    public static int hash32(final byte[] data, final int offset, final int length, final int seed) {
        int hash = seed;
        final int nblocks = length >> 2;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int index = offset + (i << 2);
            final int k = getLittleEndianInt(data, index);
            hash = mix32(k, hash);
        }

        // tail
        // ************
        // Note: This fails to apply masking using 0xff to the 3 remaining bytes.
        // ************
        final int index = offset + (nblocks << 2);
        int k1 = 0;
        switch (offset + length - index) {
            case 3:
                k1 ^= data[index + 2] << 16;
            case 2:
                k1 ^= data[index + 1] << 8;
            case 1:
                k1 ^= data[index];

                // mix functions
                k1 *= C1_32;
                k1 = Integer.rotateLeft(k1, R1_32);
                k1 *= C2_32;
                hash ^= k1;
        }

        hash ^= length;
        return fmix32(hash);
    }

    /**
     * Generates 32-bit hash from the byte array with a seed of zero.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 0;
     * int hash = MurmurHash3.hash32x86(data, offset, data.length, seed);
     * </pre>
     *
     * @param data The input byte array
     * @return The 32-bit hash
     * @see #hash32x86(byte[], int, int, int)
     * @since 1.14
     */
    public static int hash32x86(final byte[] data) {
        return hash32x86(data, 0, data.length, 0);
    }

    /**
     * Generates 32-bit hash from the byte array with the given offset, length and seed.
     *
     * <p>This is an implementation of the 32-bit hash function {@code MurmurHash3_x86_32}
     * from from Austin Applyby's original MurmurHash3 {@code c++} code in SMHasher.</p>
     *
     * @param data The input byte array
     * @param offset The offset of data
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 32-bit hash
     * @since 1.14
     */
    @SuppressWarnings("fallthrough")
    public static int hash32x86(final byte[] data, final int offset, final int length, final int seed) {
        int hash = seed;
        final int nblocks = length >> 2;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int index = offset + (i << 2);
            final int k = getLittleEndianInt(data, index);
            hash = mix32(k, hash);
        }

        // tail
        final int index = offset + (nblocks << 2);
        int k1 = 0;
        switch (offset + length - index) {
            case 3:
                k1 ^= (data[index + 2] & 0xff) << 16;
            case 2:
                k1 ^= (data[index + 1] & 0xff) << 8;
            case 1:
                k1 ^= (data[index] & 0xff);

                // mix functions
                k1 *= C1_32;
                k1 = Integer.rotateLeft(k1, R1_32);
                k1 *= C2_32;
                hash ^= k1;
        }

        hash ^= length;
        return fmix32(hash);
    }

    /**
     * Generates 64-bit hash from a long with a default seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * The method does not produce the same result as either half of the hash bytes from
     * {@linkplain #hash128x64(byte[])} with the same byte data from the {@code long}.
     * This method will be removed in a future release.</p>
     *
     * <p>Note: The sign extension bug in {@link #hash64(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * <p>This is a helper method that will produce the same result as:</p>
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * long hash = MurmurHash3.hash64(ByteBuffer.allocate(8)
     *                                          .putLong(data)
     *                                          .array(), offset, 8, seed);
     * </pre>
     *
     * @param data The long to hash
     * @return The 64-bit hash
     * @see #hash64(byte[], int, int, int)
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[])} with the bytes from the {@code long}.
     */
    @Deprecated
    public static long hash64(final long data) {
        long hash = DEFAULT_SEED;
        long k = Long.reverseBytes(data);
        final int length = LONG_BYTES;
        // mix functions
        k *= C1;
        k = Long.rotateLeft(k, R1);
        k *= C2;
        hash ^= k;
        hash = Long.rotateLeft(hash, R2) * M + N1;
        // finalization
        hash ^= length;
        hash = fmix64(hash);
        return hash;
    }

    /**
     * Generates 64-bit hash from an int with a default seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * The method does not produce the same result as either half of the hash bytes from
     * {@linkplain #hash128x64(byte[])} with the same byte data from the {@code int}.
     * This method will be removed in a future release.</p>
     *
     * <p>Note: The sign extension bug in {@link #hash64(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * <p>This is a helper method that will produce the same result as:</p>
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * long hash = MurmurHash3.hash64(ByteBuffer.allocate(4)
     *                                          .putInt(data)
     *                                          .array(), offset, 4, seed);
     * </pre>
     *
     * @param data The int to hash
     * @return The 64-bit hash
     * @see #hash64(byte[], int, int, int)
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[])} with the bytes from the {@code int}.
     */
    @Deprecated
    public static long hash64(final int data) {
        long k1 = Integer.reverseBytes(data) & (-1L >>> 32);
        final int length = INTEGER_BYTES;
        long hash = DEFAULT_SEED;
        k1 *= C1;
        k1 = Long.rotateLeft(k1, R1);
        k1 *= C2;
        hash ^= k1;
        // finalization
        hash ^= length;
        hash = fmix64(hash);
        return hash;
    }

    /**
     * Generates 64-bit hash from a short with a default seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * The method does not produce the same result as either half of the hash bytes from
     * {@linkplain #hash128x64(byte[])} with the same byte data from the {@code short}.
     * This method will be removed in a future release.</p>
     *
     * <p>Note: The sign extension bug in {@link #hash64(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * <p>This is a helper method that will produce the same result as:</p>
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * long hash = MurmurHash3.hash64(ByteBuffer.allocate(2)
     *                                          .putShort(data)
     *                                          .array(), offset, 2, seed);
     * </pre>
     *
     * @param data The short to hash
     * @return The 64-bit hash
     * @see #hash64(byte[], int, int, int)
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[])} with the bytes from the {@code short}.
     */
    @Deprecated
    public static long hash64(final short data) {
        long hash = DEFAULT_SEED;
        long k1 = 0;
        k1 ^= ((long) data & 0xff) << 8;
        k1 ^= ((long) ((data & 0xFF00) >> 8) & 0xff);
        k1 *= C1;
        k1 = Long.rotateLeft(k1, R1);
        k1 *= C2;
        hash ^= k1;

        // finalization
        hash ^= SHORT_BYTES;
        hash = fmix64(hash);
        return hash;
    }

    /**
     * Generates 64-bit hash from a byte array with a default seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * The method does not produce the same result as either half of the hash bytes from
     * {@linkplain #hash128x64(byte[])} with the same byte data.
     * This method will be removed in a future release.</p>
     *
     * <p>Note: The sign extension bug in {@link #hash64(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * <p>This is a helper method that will produce the same result as:</p>
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * long hash = MurmurHash3.hash64(data, offset, data.length, seed);
     * </pre>
     *
     * @param data The input byte array
     * @return The 64-bit hash
     * @see #hash64(byte[], int, int, int)
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[])}.
     */
    @Deprecated
    public static long hash64(final byte[] data) {
        return hash64(data, 0, data.length, DEFAULT_SEED);
    }

    /**
     * Generates 64-bit hash from a byte array with the given offset and length and a default seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * The method does not produce the same result as either half of the hash bytes from
     * {@linkplain #hash128x64(byte[])} with the same byte data.
     * This method will be removed in a future release.</p>
     *
     * <p>Note: The sign extension bug in {@link #hash64(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * <p>This is a helper method that will produce the same result as:</p>
     *
     * <pre>
     * int seed = 104729;
     * long hash = MurmurHash3.hash64(data, offset, length, seed);
     * </pre>
     *
     * @param data The input byte array
     * @param offset The offset of data
     * @param length The length of array
     * @return The 64-bit hash
     * @see #hash64(byte[], int, int, int)
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[], int, int, int)}.
     */
    @Deprecated
    public static long hash64(final byte[] data, final int offset, final int length) {
        return hash64(data, offset, length, DEFAULT_SEED);
    }

    /**
     * Generates 64-bit hash from a byte array with the given offset, length and seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * This method will be removed in a future release.</p>
     *
     * <p>This implementation contains a sign-extension bug in the seed initialization.
     * This manifests if the seed is negative.</p>
     *
     * <p>This algorithm processes 8 bytes chunks of data in a manner similar to the 16 byte chunks
     * of data processed in the MurmurHash3 {@code MurmurHash3_x64_128} method. However the hash
     * is not mixed with a hash chunk from the next 8 bytes of data. The method will not return
     * the same value as the first or second 64-bits of the function
     * {@link #hash128(byte[], int, int, int)}.</p>
     *
     * <p>Use of this method is not advised. Use the first long returned from
     * {@link #hash128x64(byte[], int, int, int)}.<p>
     *
     * @param data The input byte array
     * @param offset The offset of data
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 64-bit hash
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[], int, int, int)}.
     */
    @Deprecated
    @SuppressWarnings("fallthrough")
    public static long hash64(final byte[] data, final int offset, final int length, final int seed) {
        // ************
        // Note: This fails to apply masking using 0xffffffffL to the seed.
        // ************
        long hash = seed;
        final int nblocks = length >> 3;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int index = offset + (i << 3);
            long k = getLittleEndianLong(data, index);

            // mix functions
            k *= C1;
            k = Long.rotateLeft(k, R1);
            k *= C2;
            hash ^= k;
            hash = Long.rotateLeft(hash, R2) * M + N1;
        }

        // tail
        long k1 = 0;
        final int index = offset + (nblocks << 3);
        switch (offset + length - index) {
            case 7:
                k1 ^= ((long) data[index + 6] & 0xff) << 48;
            case 6:
                k1 ^= ((long) data[index + 5] & 0xff) << 40;
            case 5:
                k1 ^= ((long) data[index + 4] & 0xff) << 32;
            case 4:
                k1 ^= ((long) data[index + 3] & 0xff) << 24;
            case 3:
                k1 ^= ((long) data[index + 2] & 0xff) << 16;
            case 2:
                k1 ^= ((long) data[index + 1] & 0xff) << 8;
            case 1:
                k1 ^= ((long) data[index] & 0xff);
                k1 *= C1;
                k1 = Long.rotateLeft(k1, R1);
                k1 *= C2;
                hash ^= k1;
        }

        // finalization
        hash ^= length;
        hash = fmix64(hash);

        return hash;
    }

    /**
     * Generates 128-bit hash from the byte array with a default seed.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * int hash = MurmurHash3.hash128(data, offset, data.length, seed);
     * </pre>
     *
     * <p>Note: The sign extension bug in {@link #hash128(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * @param data The input byte array
     * @return The 128-bit hash (2 longs)
     * @see #hash128(byte[], int, int, int)
     */
    public static long[] hash128(final byte[] data) {
        return hash128(data, 0, data.length, DEFAULT_SEED);
    }

    /**
     * Generates 128-bit hash from the byte array with a seed of zero.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 0;
     * int hash = MurmurHash3.hash128x64(data, offset, data.length, seed);
     * </pre>
     *
     * @param data The input byte array
     * @return The 128-bit hash (2 longs)
     * @see #hash128x64(byte[], int, int, int)
     * @since 1.14
     */
    public static long[] hash128x64(final byte[] data) {
        return hash128x64(data, 0, data.length, 0);
    }

    /**
     * Generates 128-bit hash from a string with a default seed.
     * <p>
     * Before 1.14 the string was converted using default encoding.
     * Since 1.14 the string is converted to bytes using UTF-8 encoding.
     * </p>
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
     * int hash = MurmurHash3.hash128(bytes, offset, bytes.length, seed);
     * </pre>
     *
     * <p>Note: The sign extension bug in {@link #hash128(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * @param data The input String
     * @return The 128-bit hash (2 longs)
     * @see #hash128(byte[], int, int, int)
     * @deprecated Use {@link #hash128x64(byte[])} using the bytes returned from
     * {@link String#getBytes(java.nio.charset.Charset)}.
     */
    @Deprecated
    public static long[] hash128(final String data) {
        final byte[] bytes = StringUtils.getBytesUtf8(data);
        return hash128(bytes, 0, bytes.length, DEFAULT_SEED);
    }

    /**
     * Generates 128-bit hash from the byte array with the given offset, length and seed.
     *
     * <p>This is an implementation of the 128-bit hash function {@code MurmurHash3_x64_128}
     * from from Austin Applyby's original MurmurHash3 {@code c++} code in SMHasher.</p>
     *
     * <p>This implementation contains a sign-extension bug in the seed initialization.
     * This manifests if the seed is negative.<p>
     *
     * @param data The input byte array
     * @param offset The first element of array
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 128-bit hash (2 longs)
     * @deprecated Use {@link #hash128x64(byte[], int, int, int)}. This corrects the seed initialization.
     */
    @Deprecated
    public static long[] hash128(final byte[] data, final int offset, final int length, final int seed) {
        // ************
        // Note: This fails to apply masking using 0xffffffffL to the seed.
        // ************
        return hash128x64(data, offset, length, seed);
    }

    /**
     * Generates 128-bit hash from the byte array with the given offset, length and seed.
     *
     * <p>This is an implementation of the 128-bit hash function {@code MurmurHash3_x64_128}
     * from from Austin Applyby's original MurmurHash3 {@code c++} code in SMHasher.</p>
     *
     * @param data The input byte array
     * @param offset The first element of array
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 128-bit hash (2 longs)
     * @since 1.14
     */
    public static long[] hash128x64(final byte[] data, final int offset, final int length, final int seed) {
        // Use an unsigned 32-bit integer as the seed
        return hash128x64(data, offset, length, seed & 0xffffffffL);
    }

    /**
     * Generates 128-bit hash from the byte array with the given offset, length and seed.
     *
     * <p>This is an implementation of the 128-bit hash function {@code MurmurHash3_x64_128}
     * from from Austin Applyby's original MurmurHash3 {@code c++} code in SMHasher.</p>
     *
     * @param data The input byte array
     * @param offset The first element of array
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 128-bit hash (2 longs)
     */
    @SuppressWarnings("fallthrough")
    private static long[] hash128x64(final byte[] data, final int offset, final int length, final long seed) {
        long h1 = seed;
        long h2 = seed;
        final int nblocks = length >> 4;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int index = offset + (i << 4);
            long k1 = getLittleEndianLong(data, index);
            long k2 = getLittleEndianLong(data, index + 8);

            // mix functions for k1
            k1 *= C1;
            k1 = Long.rotateLeft(k1, R1);
            k1 *= C2;
            h1 ^= k1;
            h1 = Long.rotateLeft(h1, R2);
            h1 += h2;
            h1 = h1 * M + N1;

            // mix functions for k2
            k2 *= C2;
            k2 = Long.rotateLeft(k2, R3);
            k2 *= C1;
            h2 ^= k2;
            h2 = Long.rotateLeft(h2, R1);
            h2 += h1;
            h2 = h2 * M + N2;
        }

        // tail
        long k1 = 0;
        long k2 = 0;
        final int index = offset + (nblocks << 4);
        switch (offset + length - index) {
            case 15:
                k2 ^= ((long) data[index + 14] & 0xff) << 48;
            case 14:
                k2 ^= ((long) data[index + 13] & 0xff) << 40;
            case 13:
                k2 ^= ((long) data[index + 12] & 0xff) << 32;
            case 12:
                k2 ^= ((long) data[index + 11] & 0xff) << 24;
            case 11:
                k2 ^= ((long) data[index + 10] & 0xff) << 16;
            case 10:
                k2 ^= ((long) data[index + 9] & 0xff) << 8;
            case 9:
                k2 ^= data[index + 8] & 0xff;
                k2 *= C2;
                k2 = Long.rotateLeft(k2, R3);
                k2 *= C1;
                h2 ^= k2;

            case 8:
                k1 ^= ((long) data[index + 7] & 0xff) << 56;
            case 7:
                k1 ^= ((long) data[index + 6] & 0xff) << 48;
            case 6:
                k1 ^= ((long) data[index + 5] & 0xff) << 40;
            case 5:
                k1 ^= ((long) data[index + 4] & 0xff) << 32;
            case 4:
                k1 ^= ((long) data[index + 3] & 0xff) << 24;
            case 3:
                k1 ^= ((long) data[index + 2] & 0xff) << 16;
            case 2:
                k1 ^= ((long) data[index + 1] & 0xff) << 8;
            case 1:
                k1 ^= data[index] & 0xff;
                k1 *= C1;
                k1 = Long.rotateLeft(k1, R1);
                k1 *= C2;
                h1 ^= k1;
        }

        // finalization
        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return new long[] { h1, h2 };
    }

    /**
     * Gets the little-endian long from 8 bytes starting at the specified index.
     *
     * @param data The data
     * @param index The index
     * @return The little-endian long
     */
    private static long getLittleEndianLong(final byte[] data, final int index) {
        return (((long) data[index    ] & 0xff)      ) |
            (((long) data[index + 1] & 0xff) <<  8) |
            (((long) data[index + 2] & 0xff) << 16) |
            (((long) data[index + 3] & 0xff) << 24) |
            (((long) data[index + 4] & 0xff) << 32) |
            (((long) data[index + 5] & 0xff) << 40) |
            (((long) data[index + 6] & 0xff) << 48) |
            (((long) data[index + 7] & 0xff) << 56);
    }

    /**
     * Gets the little-endian int from 4 bytes starting at the specified index.
     *
     * @param data The data
     * @param index The index
     * @return The little-endian int
     */
    private static int getLittleEndianInt(final byte[] data, final int index) {
        return ((data[index    ] & 0xff)      ) |
            ((data[index + 1] & 0xff) <<  8) |
            ((data[index + 2] & 0xff) << 16) |
            ((data[index + 3] & 0xff) << 24);
    }

    /**
     * Performs the intermediate mix step of the 32-bit hash function {@code MurmurHash3_x86_32}.
     *
     * @param k The data to add to the hash
     * @param hash The current hash
     * @return The new hash
     */
    private static int mix32(int k, int hash) {
        k *= C1_32;
        k = Integer.rotateLeft(k, R1_32);
        k *= C2_32;
        hash ^= k;
        return Integer.rotateLeft(hash, R2_32) * M_32 + N_32;
    }

    /**
     * Performs the final avalanche mix step of the 32-bit hash function {@code MurmurHash3_x86_32}.
     *
     * @param hash The current hash
     * @return The final hash
     */
    private static int fmix32(int hash) {
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        hash *= 0xc2b2ae35;
        hash ^= (hash >>> 16);
        return hash;
    }

    /**
     * Performs the final avalanche mix step of the 64-bit hash function {@code MurmurHash3_x64_128}.
     *
     * @param hash The current hash
     * @return The final hash
     */
    private static long fmix64(long hash) {
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdL;
        hash ^= (hash >>> 33);
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= (hash >>> 33);
        return hash;
    }

    /**
     * Generates 32-bit hash from input bytes. Bytes can be added incrementally and the new
     * hash computed.
     *
     * <p>This is an implementation of the 32-bit hash function {@code MurmurHash3_x86_32}
     * from from Austin Applyby's original MurmurHash3 {@code c++} code in SMHasher.</p>
     *
     * @since 1.14
     */
    public static class IncrementalHash32x86 {

        /** The size of byte blocks that are processed together. */
        private static final int BLOCK_SIZE = 4;

        /** Up to 3 unprocessed bytes from input data. */
        private final byte[] unprocessed = new byte[3];

        /** The number of unprocessed bytes in the tail data. */
        private int unprocessedLength;

        /** The total number of input bytes added since the start. */
        private int totalLen;

        /**
         * The current running hash.
         * This must be finalised to generate the 32-bit hash value.
         */
        private int hash;

        /**
         * Starts a new incremental hash.
         *
         * @param seed The initial seed value
         */
        public final void start(final int seed) {
            // Reset
            unprocessedLength = totalLen = 0;
            this.hash = seed;
        }

        /**
         * Adds the byte array to the current incremental hash.
         *
         * @param data The input byte array
         * @param offset The offset of data
         * @param length The length of array
         */
        @SuppressWarnings("fallthrough")
        public final void add(final byte[] data, final int offset, final int length) {
            if (length <= 0) {
                // Nothing to add
                return;
            }
            totalLen += length;

            // Process the bytes in blocks of 4.
            // New bytes must be added to any current unprocessed bytes,
            // then processed in blocks of 4 and the remaining bytes saved:
            //
            //    |--|---------------------------|--|
            // unprocessed
            //                main block
            //                                remaining

            // Check if the unprocessed bytes and new bytes can fill a block of 4.
            // Make this overflow safe in the event that length is Integer.MAX_VALUE.
            // Equivalent to: (unprocessedLength + length < BLOCK_SIZE)
            if (unprocessedLength + length - BLOCK_SIZE < 0) {
                // Not enough so add to the unprocessed bytes
                System.arraycopy(data, offset, unprocessed, unprocessedLength, length);
                unprocessedLength += length;
                return;
            }

            // Combine unprocessed bytes with new bytes.
            int newOffset;
            int newLength;
            if (unprocessedLength > 0) {
                int k = -1;
                switch (unprocessedLength) {
                    case 1:
                        k = orBytes(unprocessed[0], data[offset], data[offset + 1], data[offset + 2]);
                        break;
                    case 2:
                        k = orBytes(unprocessed[0], unprocessed[1], data[offset], data[offset + 1]);
                        break;
                    case 3:
                        k = orBytes(unprocessed[0], unprocessed[1], unprocessed[2], data[offset]);
                        break;
                    default:
                        throw new IllegalStateException("Unprocessed length should be 1, 2, or 3: " + unprocessedLength);
                }
                hash = mix32(k, hash);
                // Update the offset and length
                final int consumed = BLOCK_SIZE - unprocessedLength;
                newOffset = offset + consumed;
                newLength = length - consumed;
            } else {
                newOffset = offset;
                newLength = length;
            }

            // Main processing of blocks of 4 bytes
            final int nblocks = newLength >> 2;

            for (int i = 0; i < nblocks; i++) {
                final int index = newOffset + (i << 2);
                final int k = getLittleEndianInt(data, index);
                hash = mix32(k, hash);
            }

            // Save left-over unprocessed bytes
            final int consumed = (nblocks << 2);
            unprocessedLength = newLength - consumed;
            if (unprocessedLength != 0) {
                System.arraycopy(data, newOffset + consumed, unprocessed, 0, unprocessedLength);
            }
        }

        /**
         * Generate the 32-bit hash value. Repeat calls to this method with no additional data
         * will generate the same hash value.
         *
         * @return The 32-bit hash
         */
        public final int end() {
            // Allow calling end() again after adding no data to return the same result.
            return finalise(hash, unprocessedLength, unprocessed, totalLen);
        }

        /**
         * Finalize the running hash to the output 32-bit hash by processing remaining bytes
         * and performing final mixing.
         *
         * @param hash The running hash
         * @param unprocessedLength The number of unprocessed bytes in the tail data.
         * @param unprocessed Up to 3 unprocessed bytes from input data.
         * @param totalLen The total number of input bytes added since the start.
         * @return The 32-bit hash
         */
        @SuppressWarnings("fallthrough")
        int finalise(final int hash, final int unprocessedLength, final byte[] unprocessed, final int totalLen) {
            int result = hash;
            int k1 = 0;
            switch (unprocessedLength) {
                case 3:
                    k1 ^= (unprocessed[2] & 0xff) << 16;
                case 2:
                    k1 ^= (unprocessed[1] & 0xff) << 8;
                case 1:
                    k1 ^= (unprocessed[0] & 0xff);

                    // mix functions
                    k1 *= C1_32;
                    k1 = Integer.rotateLeft(k1, R1_32);
                    k1 *= C2_32;
                    result ^= k1;
            }

            // finalization
            result ^= totalLen;
            return fmix32(result);
        }

        /**
         * Combines the bytes using an Or operation ({@code | } in a little-endian representation
         * of a 32-bit integer; byte 1 will be the least significant byte, byte 4 the most
         * significant.
         *
         * @param b1 The first byte
         * @param b2 The second byte
         * @param b3 The third byte
         * @param b4 The fourth byte
         * @return The 32-bit integer
         */
        private static int orBytes(final byte b1, final byte b2, final byte b3, final byte b4) {
            return (b1 & 0xff) | ((b2 & 0xff) << 8) | ((b3 & 0xff) << 16) | ((b4 & 0xff) << 24);
        }
    }

    /**
     * Generates 32-bit hash from input bytes. Bytes can be added incrementally and the new
     * hash computed.
     *
     * <p>This is an implementation of the 32-bit hash function {@code MurmurHash3_x86_32}
     * from from Austin Applyby's original MurmurHash3 {@code c++} code in SMHasher.</p>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.<p>
     *
     * @deprecated Use IncrementalHash32x86. This corrects the processing of trailing bytes.
     */
    @Deprecated
    public static class IncrementalHash32 extends IncrementalHash32x86 {

        /**
         * {@inheritDoc}
         *
         * <p>This implementation contains a sign-extension bug in the finalization step of
         * any bytes left over from dividing the length by 4. This manifests if any of these
         * bytes are negative.<p>
         *
         * @deprecated Use IncrementalHash32x86. This corrects the processing of trailing bytes.
         */
        @Override
        @Deprecated
        @SuppressWarnings("fallthrough")
        int finalise(final int hash, final int unprocessedLength, final byte[] unprocessed, final int totalLen) {
            int result = hash;
            // ************
            // Note: This fails to apply masking using 0xff to the 3 remaining bytes.
            // ************
            int k1 = 0;
            switch (unprocessedLength) {
                case 3:
                    k1 ^= unprocessed[2] << 16;
                case 2:
                    k1 ^= unprocessed[1] << 8;
                case 1:
                    k1 ^= unprocessed[0];

                    // mix functions
                    k1 *= C1_32;
                    k1 = Integer.rotateLeft(k1, R1_32);
                    k1 *= C2_32;
                    result ^= k1;
            }

            // finalization
            result ^= totalLen;
            return fmix32(result);
        }
    }
}
