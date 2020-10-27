// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.DataEncryptionKey;
import com.azure.cosmos.encryption.EncryptionType;


import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class implements authenticated encryption algorithm with associated data as described in
 * http://tools.ietf.org/html/draft-mcgrew-aead-aes-cbc-hmac-sha2-05 - specifically this implements
 * AEAD_AES_256_CBC_HMAC_SHA256 algorithm.
 * This (and AeadAes256CbcHmac256EncryptionKey) implementation for Cosmos DB is same as the existing
 * SQL client implementation with StyleCop related changes - also, we restrict to randomized encryption to start with.
 */
class AeadAes256CbcHmac256Algorithm implements DataEncryptionKey {
    // TODO: moderakh is there any other library which we can use?
    public final static String ALGORITHM_NAME = "AEAD_AES_256_CBC_HMAC_SHA256";

    /**
     * Key size in bytes
     */
    private static final int KEY_SIZE_IN_BYTES = AeadAes256CbcHmac256EncryptionKey.KEY_SIZE / 8;

    /**
     * Block size in bytes. AES uses 16 byte blocks.
     */
    private static final int BLOCK_SIZE_IN_BYTES = 16;

    /**
     * Minimum Length of cipherText without authentication tag. This value is 1 (version byte) + 16 (IV) + 16 (minimum of 1 block of cipher Text)
     */
    private static final int MINIMUM_CIPHER_TEXT_LENGTH_IN_BYTES_NO_AUTHENTICATION_TAG = Bytes.ONE_BYTE_SIZE + BLOCK_SIZE_IN_BYTES + BLOCK_SIZE_IN_BYTES;

    /**
     * Minimum Length of cipherText. This value is 1 (version byte) + 32 (authentication tag) + 16 (IV) + 16 (minimum of 1 block of cipher Text)
     */
    private static final int MINIMUM_CIPHER_TEXT_LENGTH_IN_BYTES_WITH_AUTHENTICATION_TAG = MINIMUM_CIPHER_TEXT_LENGTH_IN_BYTES_NO_AUTHENTICATION_TAG + KEY_SIZE_IN_BYTES;

    /**
     * Cipher Mode. For this algorithm, we only use CBC mode.
     */
    private static final AesCryptoServiceProvider.CipherMode CIPHER_MODE = AesCryptoServiceProvider.CipherMode.CBC;

    /**
     * Padding mode. This algorithm uses PKCS7. // TODO:
     */
    private static final AesCryptoServiceProvider.PaddingMode PADDING_MODE = AesCryptoServiceProvider.PaddingMode.PKCS7;

    /**
     * Byte array with algorithm version used for authentication tag computation.
     */
    private static final byte[] VERSION = new byte[]{0x01};

    /**
     * Byte array with algorithm version size used for authentication tag computation.
     */
    private static final byte[] VERSION_SIZE = new byte[]{Bytes.ONE_BYTE_SIZE};

    /**
     * Variable indicating whether this algorithm should work in Deterministic mode or Randomized mode.
     * For deterministic encryption, we derive an IV from the plaintext data.
     * For randomized encryption, we generate a cryptographically random IV.
     */
    private final boolean isDeterministic;

    /**
     * Algorithm Version.
     */
    private final byte algorithmVersion;

    /**
     * Data Encryption Key. This has a root key and three derived keys.
     */
    private final AeadAes256CbcHmac256EncryptionKey dataEncryptionKey;

    /**
     * The pool of crypto providers to use for encrypt/decrypt operations.
     */
    private final ConcurrentLinkedQueue<AesCryptoServiceProvider> cryptoProviderPool;

    @Override
    public byte[] getRawKey() {
        return  this.dataEncryptionKey.getRootKey();
    }


    @Override
    public String getEncryptionAlgorithm() {
        return CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED;
    }

    /**
     * Initializes a new instance of AeadAes256CbcHmac256Algorithm algorithm with a given key and encryption type
     *
     * @param encryptionKey    Root encryption key from which three other keys will be derived
     * @param encryptionType   Encryption Type, accepted values are Deterministic and Randomized.
     *                         For Deterministic encryption, a synthetic IV will be genenrated during encryption
     *                         For Randomized encryption, a random IV will be generated during encryption.
     * @param algorithmVersion Algorithm version
     */
    public AeadAes256CbcHmac256Algorithm(AeadAes256CbcHmac256EncryptionKey encryptionKey, EncryptionType encryptionType, byte algorithmVersion) {
        this.dataEncryptionKey = encryptionKey;
        this.algorithmVersion = algorithmVersion;

        VERSION[0] = algorithmVersion;

        assert encryptionKey != null : "Null encryption key detected in AeadAes256CbcHmac256 algorithm";
        assert algorithmVersion == 0x01 : "Unknown algorithm version passed to AeadAes256CbcHmac256";

        // Validate encryption type for this algorithm
        // This algorithm can only provide randomized or deterministic encryption types.
        this.isDeterministic = (EncryptionType.DETERMINISTIC == encryptionType);
        this.cryptoProviderPool = new ConcurrentLinkedQueue<>();
    }

    /**
     * Encryption Algorithm
     * <p>
     * cell_iv = HMAC_SHA-2-256(iv_key, cell_data) truncated to 128 bits
     * cell_ciphertext = AES-CBC-256(enc_key, cell_iv, cell_data) with PKCS7 padding.
     * cell_tag = HMAC_SHA-2-256(mac_key, versionbyte + cell_iv + cell_ciphertext + versionbyte_length)
     * cell_blob = versionbyte + cell_tag + cell_iv + cell_ciphertext
     *
     * @param plainText Plain text value to be encrypted.
     * @return Returns the ciphertext corresponding to the plaintext.
     */
    @Override
    public byte[] encryptData(byte[] plainText) {
        return this.encryptData(plainText, true);
    }

    /**
     * Encryption Algorithm
     * <p>
     * cell_iv = HMAC_SHA-2-256(iv_key, cell_data) truncated to 128 bits
     * cell_ciphertext = AES-CBC-256(enc_key, cell_iv, cell_data) with PKCS7 padding.
     * (optional) cell_tag = HMAC_SHA-2-256(mac_key, versionbyte + cell_iv + cell_ciphertext + versionbyte_length)
     * cell_blob = versionbyte + [cell_tag] + cell_iv + cell_ciphertext
     *
     * @param plainText            Plaintext data to be encrypted
     * @param hasAuthenticationTag Does the algorithm require authentication tag.
     * @return Returns the ciphertext corresponding to the plaintext.
     */
    private byte[] encryptData(byte[] plainText, boolean hasAuthenticationTag) {
        // Empty values get encrypted and decrypted properly for both Deterministic and Randomized encryptions.
        assert (plainText != null);

        byte[] iv = new byte[BLOCK_SIZE_IN_BYTES];

        // Prepare IV
        // Should be 1 single block (16 bytes)
        if (this.isDeterministic) {
            SecurityUtility.getHMACWithSHA256(plainText, this.dataEncryptionKey.getIVKey(), iv);
        } else {
            SecurityUtility.generateRandomBytes(iv);
        }

        int numBlocks = (plainText.length / BLOCK_SIZE_IN_BYTES) + 1;

        // Final blob we return = version + HMAC + iv + cipherText
        final int hmacStartIndex = 1;
        int authenticationTagLen = hasAuthenticationTag ? KEY_SIZE_IN_BYTES : 0;
        int ivStartIndex = hmacStartIndex + authenticationTagLen;
        int cipherStartIndex = ivStartIndex + BLOCK_SIZE_IN_BYTES; // this is where hmac starts.

        // Output buffer size = size of VersionByte + Authentication Tag + IV + cipher Text blocks.
        int outputBufSize = Bytes.ONE_BYTE_SIZE + authenticationTagLen + iv.length + (numBlocks * BLOCK_SIZE_IN_BYTES);
        byte[] outBuffer = new byte[outputBufSize];

        // Store the version and IV rightaway
        outBuffer[0] = this.algorithmVersion;
        System.arraycopy(iv, 0, outBuffer, ivStartIndex, iv.length);

        AesCryptoServiceProvider aesAlg = this.cryptoProviderPool.poll();

        // Try to get a provider from the pool.
        // If no provider is available, create a new one.
        if (aesAlg == null) {
            aesAlg = new AesCryptoServiceProvider(this.dataEncryptionKey.getEncryptionKey(), PADDING_MODE, CIPHER_MODE);
        }

        try {
            // Always set the IV since it changes from cell to cell.
            aesAlg.setIv(iv);

            // Compute CipherText and authentication tag in a single pass
            try (AesCryptoServiceProvider.ICryptoTransform encryptor = aesAlg.createEncryptor()) {
                // TODO: assert encryptor.CanTransformMultipleBlocks : "AES Encryptor can transform multiple blocks";
                int count = 0;
                int cipherIndex = cipherStartIndex; // this is where cipherText starts
                if (numBlocks > 1) {
                    count = (numBlocks - 1) * BLOCK_SIZE_IN_BYTES;
                    cipherIndex += encryptor.transformBlock(plainText, 0, count, outBuffer, cipherIndex);
                }

                byte[] buffTmp = encryptor.transformFinalBlock(plainText, count, plainText.length - count); // done encrypting
                System.arraycopy(buffTmp, 0, outBuffer, cipherIndex, buffTmp.length);
                cipherIndex += buffTmp.length;
            }

            if (hasAuthenticationTag) {
                try (HMACSHA256 hmac = new HMACSHA256(this.dataEncryptionKey.getMACKey())) {
                    // TODO: always true assert(hmac.CanTransformMultipleBlocks, "HMAC can't transform multiple blocks");
                    hmac.transformBlock(VERSION, 0, VERSION.length, VERSION, 0);
                    hmac.transformBlock(iv, 0, iv.length, iv, 0);

                    // Compute HMAC on final block
                    hmac.transformBlock(outBuffer, cipherStartIndex, numBlocks * BLOCK_SIZE_IN_BYTES, outBuffer, cipherStartIndex);
                    hmac.transformFinalBlock(VERSION_SIZE, 0, VERSION_SIZE.length);
                    byte[] hash = hmac.getHash();
                    assert hash.length >= authenticationTagLen : "Unexpected hash size";
                    System.arraycopy(hash, 0, outBuffer, hmacStartIndex, authenticationTagLen);
                }
            }
        } finally {
            // Return the provider to the pool.
            this.cryptoProviderPool.add(aesAlg);
        }

        return outBuffer;
    }

    /**
     * Decryption steps
     * 1. Validate version byte
     * 2. Validate Authentication tag
     * 3. Decrypt the message
     *
     * @param cipherText Ciphertext value to be decrypted.
     * @return
     */
    @Override
    public byte[] decryptData(byte[] cipherText) {
        return this.decryptData(cipherText, /** hasAuthenticationTag */true);
    }

    /**
     * Decryption steps
     * 1. Validate version byte
     * 2. (optional) Validate Authentication tag
     * 3. Decrypt the message
     *
     * @param cipherText
     * @param hasAuthenticationTag
     * @return
     */
    private byte[] decryptData(byte[] cipherText, boolean hasAuthenticationTag) {
        assert cipherText != null;

        byte[] iv = new byte[BLOCK_SIZE_IN_BYTES];

        int minimumCipherTextLength = hasAuthenticationTag ? MINIMUM_CIPHER_TEXT_LENGTH_IN_BYTES_WITH_AUTHENTICATION_TAG : MINIMUM_CIPHER_TEXT_LENGTH_IN_BYTES_NO_AUTHENTICATION_TAG;
        if (cipherText.length < minimumCipherTextLength) {
            throw EncryptionExceptionFactory.invalidCipherTextSize(cipherText.length, minimumCipherTextLength);
        }

        // Validate the version byte
        int startIndex = 0;
        if (cipherText[startIndex] != this.algorithmVersion) {
            // Cipher text was computed with a different algorithm version than this.
            throw EncryptionExceptionFactory.invalidAlgorithmVersion(cipherText[startIndex], this.algorithmVersion);
        }

        startIndex += 1;
        int authenticationTagOffset = 0;

        // Read authentication tag
        if (hasAuthenticationTag) {
            authenticationTagOffset = startIndex;
            startIndex += KEY_SIZE_IN_BYTES; // authentication tag size is KeySizeInBytes
        }

        // Read cell IV
        System.arraycopy(cipherText, startIndex, iv, 0, iv.length);
        startIndex += iv.length;

        // Read encrypted text
        int cipherTextOffset = startIndex;
        int cipherTextCount = cipherText.length - startIndex;

        if (hasAuthenticationTag) {
            // Compute authentication tag
            byte[] authenticationTag = this.prepareAuthenticationTag(iv, cipherText, cipherTextOffset, cipherTextCount);
            if (!SecurityUtility.compareBytes(authenticationTag, cipherText, authenticationTagOffset, authenticationTag.length)) {
                // Potentially tampered data, throw an exception
                throw EncryptionExceptionFactory.invalidAuthenticationTag();
            }
        }

        // Decrypt the text and return
        return this.decryptData(iv, cipherText, cipherTextOffset, cipherTextCount);
    }

    /**
     * Decrypts plain text data using AES in CBC mode
     *
     * @param iv
     * @param cipherText
     * @param offset
     * @param count
     * @return
     */
    private byte[] decryptData(byte[] iv, byte[] cipherText, int offset, int count) {
        assert ((iv != null) && (cipherText != null));
        assert (offset > -1 && count > -1);
        assert ((count + offset) <= cipherText.length);

        byte[] plainText;

        AesCryptoServiceProvider aesAlg = this.cryptoProviderPool.poll();

        // Try to get a provider from the pool.
        // If no provider is available, create a new one.
        if (aesAlg == null) {
            aesAlg = new AesCryptoServiceProvider(this.dataEncryptionKey.getEncryptionKey(), PADDING_MODE, CIPHER_MODE);

        }

        try {
            // Always set the IV since it changes from cell to cell.
            aesAlg.setIv(iv);

            // Create the streams used for decryption.

            try (AesCryptoServiceProvider.ICryptoTransform decryptor = aesAlg.createDecryptor()) {
                plainText = decryptor.transformFinalBlock(cipherText, offset, count);
            }
        } finally {
            // Return the provider to the pool.
            this.cryptoProviderPool.add(aesAlg);
        }

        return plainText;
    }

    /**
     * Prepares an authentication tag.
     * Authentication Tag = HMAC_SHA-2-256(mac_key, versionbyte + cell_iv + cell_ciphertext + versionbyte_length)
     *
     * @param iv
     * @param cipherText
     * @param offset
     * @param length
     * @return
     */
    private byte[] prepareAuthenticationTag(byte[] iv, byte[] cipherText, int offset, int length) {
        assert (cipherText != null);

        byte[] computedHash;
        byte[] authenticationTag = new byte[KEY_SIZE_IN_BYTES];

        // Raw Tag Length:
        //              1 for the version byte
        //              1 block for IV (16 bytes)
        //              cipherText.Length
        //              1 byte for version byte length
        try (HMACSHA256 hmac = new HMACSHA256(this.dataEncryptionKey.getMACKey())) {
            int retVal = 0;
            retVal = hmac.transformBlock(VERSION, 0, VERSION.length, VERSION, 0);
            assert (retVal == VERSION.length);
            retVal = hmac.transformBlock(iv, 0, iv.length, iv, 0);
            assert (retVal == iv.length);
            retVal = hmac.transformBlock(cipherText, offset, length, cipherText, offset);
            assert (retVal == length);
            hmac.transformFinalBlock(VERSION_SIZE, 0, VERSION_SIZE.length);
            computedHash = hmac.getHash();
        }

        assert (computedHash.length >= authenticationTag.length);
        System.arraycopy(computedHash, 0, authenticationTag, 0, authenticationTag.length);
        return authenticationTag;
    }
}
