/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * A DataEncryptionKey, protected by a KeyEncryptionKey, that is used to encrypt and decrypt data.
 *
 */
public class ProtectedDataEncryptionKey extends DataEncryptionKey {

    private KeyEncryptionKey keyEncryptionKey;
    private byte[] encryptedValue;

    /**
     * Getter for keyEncryptionKey.
     *
     * @return keyEncryptionKey value.
     */
    public KeyEncryptionKey getKeyEncryptionKey() {
        return keyEncryptionKey;
    }

    /**
     * Getter for encryptedValue.
     *
     * @return encryptedValue value.
     */
    public byte[] getEncryptedValue() {
        return encryptedValue;
    }

    /**
     * Returns an instance of ProtectedDataEncryptionKey.
     *
     * @param name
     *        The name by which the ProtectedDataEncryptionKey will be known.
     * @param keyEncryptionKey
     *        Specifies the KeyEncryptionKey used for encrypting and decrypting the ProtectedDataEncryptionKey.
     * @param encryptedKey
     *        The encrypted ProtectedDataEncryptionKey value.
     * @return A ProtectedDataEncryptionKey object.
     * @throws MicrosoftDataEncryptionException
     *         when one or more fields provided are null
     * @throws InvalidKeyException
     *         when the keyEncryptionKey provided is invalid
     * @throws NoSuchAlgorithmException
     *         when the required encryption algorithms are not available in the user JDK
     */
    public static ProtectedDataEncryptionKey getOrCreate(String name, KeyEncryptionKey keyEncryptionKey,
                                                         byte[] encryptedKey) throws MicrosoftDataEncryptionException, InvalidKeyException, NoSuchAlgorithmException {
        Utils.validateNotNullOrWhitespace(name, "name");
        Utils.validateNotNull(keyEncryptionKey, "keyEncryptionKey");
        Utils.validateNotNull(encryptedKey, "encryptedKey");

        return new ProtectedDataEncryptionKey(name, keyEncryptionKey, encryptedKey);
    }

    /**
     * Initializes a new instance of the ProtectedDataEncryptionKey class derived from generating an array of bytes with
     * a cryptographically strong random sequence of values.
     *
     * @param name
     *        The name by which the ProtectedDataEncryptionKey will be known.
     * @param keyEncryptionKey
     *        Specifies the the KeyEncryptionKey used for encrypting and decrypting the ProtectedDataEncryptionKey.
     * @throws NoSuchAlgorithmException
     *         on error
     * @throws MicrosoftDataEncryptionException
     *         on error
     * @throws InvalidKeyException
     *         on error
     */
    public ProtectedDataEncryptionKey(String name,
                                      KeyEncryptionKey keyEncryptionKey) throws NoSuchAlgorithmException, MicrosoftDataEncryptionException, InvalidKeyException {
        this(name, keyEncryptionKey, generateNewColumnEncryptionKey(keyEncryptionKey));
    }

    /**
     * Initializes a new instance of the ProtectedDataEncryptionKey class derived from decrypting the encryptedKey.
     *
     * @param name
     *        The name by which the ProtectedDataEncryptionKey will be known.
     * @param keyEncryptionKey
     *        Specifies the the KeyEncryptionKey used for encrypting and decrypting the ProtectedDataEncryptionKey.
     * @param encryptedKey
     *        The encrypted ProtectedDataEncryptionKey value.
     * @throws MicrosoftDataEncryptionException
     *         when one or more fields provided are null
     * @throws InvalidKeyException
     *         when the keyEncryptionKey provided is invalid
     * @throws NoSuchAlgorithmException
     *         when the required encryption algorithms are not available in the user JDK
     */
    public ProtectedDataEncryptionKey(String name, KeyEncryptionKey keyEncryptionKey,
                                      byte[] encryptedKey) throws MicrosoftDataEncryptionException, InvalidKeyException, NoSuchAlgorithmException {
        super(name, keyEncryptionKey.decryptEncryptionKey(encryptedKey));

        Utils.validateNotNullOrWhitespace(name, "name");
        Utils.validateNotNull(keyEncryptionKey, "keyEncryptionKey");

        this.keyEncryptionKey = keyEncryptionKey;
        encryptedValue = encryptedKey;
    }

    /**
     * Initializes a new instance of the ProtectedDataEncryptionKey class derived from enerating an array of bytes with
     * a cryptographically strong random sequence of values.
     *
     * @param name
     *        the name by which the ProtectedDataEncryptionKey will be known.
     * @param rootKey
     *        value of the root key.
     * @throws MicrosoftDataEncryptionException
     *         on error
     * @throws NoSuchAlgorithmException
     *         on error
     * @throws InvalidKeyException
     *         on error
     */
    public ProtectedDataEncryptionKey(String name,
                                      byte[] rootKey) throws MicrosoftDataEncryptionException, InvalidKeyException, NoSuchAlgorithmException {
        super(name, rootKey);

        Utils.validateNotNullOrWhitespace(name, "name");
        Utils.validateNotNull(rootKey, "rootkey");
    }

    private static byte[] generateNewColumnEncryptionKey(
            KeyEncryptionKey kek) throws NoSuchAlgorithmException, MicrosoftDataEncryptionException {
        byte[] plainTextColumnEncryptionKey = new byte[32];
        SecureRandom.getInstanceStrong().nextBytes(plainTextColumnEncryptionKey);
        return kek.encryptEncryptionKey(plainTextColumnEncryptionKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || !(obj instanceof ProtectedDataEncryptionKey)) {
            return false;
        }

        ProtectedDataEncryptionKey other = (ProtectedDataEncryptionKey) obj;

        if (null == keyEncryptionKey && null == other.keyEncryptionKey) {
            return true;
        } else if (null != keyEncryptionKey && null != name && null != rootKeyHexString) {
            return keyEncryptionKey.equals(other.keyEncryptionKey) && name.equals(other.name)
                    && rootKeyHexString.equals(other.rootKeyHexString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new Triple<String, KeyEncryptionKey, String>(name, keyEncryptionKey, rootKeyHexString).hashCode();
    }
}
