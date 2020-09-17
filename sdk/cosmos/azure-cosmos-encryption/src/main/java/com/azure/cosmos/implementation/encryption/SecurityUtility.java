// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

class SecurityUtility {
    final static int MAX_SHA_256_HASH_BYTES = 32;

    /**
     * Computes a keyed hash of a given text and returns. It fills the buffer "hash" with computed hash value.
     *
     * @param plainText Plain text bytes whose hash has to be computed.
     * @param key       key used for the HMAC.
     * @param hash      Output buffer where the computed hash value is stored. If it is less than 32 bytes, the hash is truncated.
     */
    static void getHMACWithSHA256(byte[] plainText, byte[] key, byte[] hash) {

        assert (key != null && plainText != null);
        assert (hash.length != 0 && hash.length <= MAX_SHA_256_HASH_BYTES);

        try (HMACSHA256 hmac = new HMACSHA256(key)) {
            byte[] computedHash = hmac.computeHash(plainText);

            // Truncate the hash if needed
            System.arraycopy(computedHash, 0, hash, 0, hash.length);
        }
    }

    /**
     * Computes SHA256 hash of a given input.
     *
     * @param input input byte array which needs to be hashed.
     * @return Returns SHA256 hash in a string form.
     */
    static String getSHA256Hash(byte[] input) {
        assert (input != null);

        try (SHA256 sha256 = SHA256.create()) {
            byte[] hashValue = sha256.computeHash(input);
            return getHexString(hashValue);
        }
    }

    /**
     * Generates cryptographically random bytes.
     *
     * @param randomBytes Buffer into which cryptographically random bytes are to be generated.
     */
    public static void generateRandomBytes(byte[] randomBytes) {
        // Generate random bytes cryptographically.
        try (RNGCryptoServiceProvider rngCsp = new RNGCryptoServiceProvider()) {
            rngCsp.getBytes(randomBytes);
        }
    }

    /**
     * Compares two byte arrays and returns true if all bytes are equal.
     *
     * @param buffer1         input buffer
     * @param buffer2         another buffer to be compared against
     * @param buffer2Index
     * @param lengthToCompare
     * @return returns true if both the arrays have the same byte values else returns false
     */
    static boolean compareBytes(byte[] buffer1, byte[] buffer2, int buffer2Index, int lengthToCompare) {
        if (buffer1 == null || buffer2 == null) {
            return false;
        }

        assert buffer1.length >= lengthToCompare : "invalid lengthToCompare";
        assert buffer2Index > -1 && buffer2Index < buffer2.length : "invalid index";
        if ((buffer2.length - buffer2Index) < lengthToCompare) {
            return false;
        }

        for (int index = 0; index < buffer1.length && index < lengthToCompare; ++index) {
            if (buffer1[index] != buffer2[buffer2Index + index]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets hex representation of byte array.
     *
     * @param input input byte array
     * @return
     */
    private static String getHexString(byte[] input) {
        assert (input != null);

        return Bytes.toHex(input);
    }
}
