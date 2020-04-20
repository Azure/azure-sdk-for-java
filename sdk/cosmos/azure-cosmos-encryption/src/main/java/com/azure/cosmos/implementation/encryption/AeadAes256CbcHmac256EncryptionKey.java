// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava27.Strings;


/**
 * Encryption key class containing 4 keys. This class is used by AeadAes256CbcHmac256Algorithm
 * 1) root key - Main key that is used to derive the keys used in the encryption algorithm
 * 2) encryption key - A derived key that is used to encrypt the plain text and generate cipher text
 * 3) mac_key - A derived key that is used to compute HMAC of the cipher text
 * 4) iv_key - A derived key that is used to generate a synthetic IV from plain text data.
 */
class AeadAes256CbcHmac256EncryptionKey extends SymmetricKey {

    /**
     * Key size in bits
     */
    static final int KEY_SIZE = 256;

    /**
     * Encryption Key Salt format. This is used to derive the encryption key from the root key.
     */
    private static final String ENCRYPTION_KEY_SALT_FORMAT = "Microsoft Azure Cosmos DB encryption key with encryption algorithm:%s and key length:%s";

    /**
     * MAC Key Salt format. This is used to derive the MAC key from the root key.
     */
    private static final String MAC_KEY_SALT_FORMAT = "Microsoft Azure Cosmos DB MAC key with encryption algorithm:%s and key length:%s";

    /**
     * IV Key Salt format. This is used to derive the IV key from the root key. This is only used for Deterministic encryption.
     */
    private static final String IV_KEY_SALT_FORMAT = "Microsoft Azure Cosmos DB IV key with encryption algorithm:%s and key length:%s";

    /**
     * Encryption Key
     */
    private final SymmetricKey encryptionKey;

    /**
     * MAC key
     */
    private final SymmetricKey macKey;

    /**
     * IV Key
     */
    private final SymmetricKey ivKey;

    /**
     * The name of the algorithm this key will be used with.
     */
    private final String algorithmName;

    /**
     * Derives all the required keys from the given root key
     *
     * @param rootKey
     * @param algorithmName
     */
     public AeadAes256CbcHmac256EncryptionKey(byte[] rootKey, String algorithmName) {
        super(rootKey);
        this.algorithmName = algorithmName;

        int keySizeInBytes = KEY_SIZE / 8;

        // Key validation
        if (rootKey.length != keySizeInBytes) {
            throw EncryptionExceptionFactory.invalidKeySize(
                this.algorithmName,
                rootKey.length,
                keySizeInBytes);
        }

        // Derive keys from the root key
        //
        // Derive encryption key
        String encryptionKeySalt = Strings.lenientFormat(ENCRYPTION_KEY_SALT_FORMAT,
            this.algorithmName,
            KEY_SIZE);
        byte[] buff1 = new byte[keySizeInBytes];
        SecurityUtility.getHMACWithSHA256(Utils.getUtf16Bytes(encryptionKeySalt), this.getRootKey(), buff1);
        this.encryptionKey = new SymmetricKey(buff1);

        // Derive mac key
        String macKeySalt = Strings.lenientFormat(MAC_KEY_SALT_FORMAT, this.algorithmName, KEY_SIZE);
        byte[] buff2 = new byte[keySizeInBytes];
        SecurityUtility.getHMACWithSHA256(Utils.getUtf16Bytes(macKeySalt), this.getRootKey(), buff2);
        this.macKey = new SymmetricKey(buff2);

        // Derive iv key
        String ivKeySalt = Strings.lenientFormat(IV_KEY_SALT_FORMAT, this.algorithmName, KEY_SIZE);
        byte[] buff3 = new byte[keySizeInBytes];
        SecurityUtility.getHMACWithSHA256(Utils.getUtf16Bytes(ivKeySalt), this.getRootKey(), buff3);
        this.ivKey = new SymmetricKey(buff3);
    }

    /**
     * Gets Encryption key should be used for encryption and decryption
     *
     * @return encryption key
     */
    byte[] getEncryptionKey() {
        return this.encryptionKey.getRootKey();
    }

    /**
     * Gets MAC key should be used to compute and validate HMAC
     *
     * @return mac key
     */
    byte[] getMACKey() {
        return this.macKey.getRootKey();
    }

    /**
     * Gets IV key should be used to compute synthetic IV from a given plain text
     *
     * @return IV key
     */
    byte[] getIVKey() {
        return this.ivKey.getRootKey();
    }

}
