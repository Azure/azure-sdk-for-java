/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import static java.nio.charset.StandardCharsets.UTF_16LE;


/**
 * An encryption key that is used to encrypt and decrypt data.
 *
 */
public abstract class DataEncryptionKey {
    /**
     * Root key in hex format.
     */
    protected String rootKeyHexString;
    private final int KEY_SIZE_IN_BITS = 256;
    /**
     * Set size of the key in bytes
     */
    public final int KEY_SIZE_IN_BYTES = KEY_SIZE_IN_BITS / 8;

    /**
     * Name of the encryption key.
     */
    protected String name;
    private byte[] rootKeyBytes;
    private byte[] encryptionKeyBytes;
    private byte[] macKeyBytes;
    private byte[] ivKeyBytes;

    /**
     * Initializes a new instance of a DataEncryptionKey.
     *
     * @param name
     *        name of the key
     * @param rootKey
     *        rootKey in bytes
     * @throws MicrosoftDataEncryptionException
     *         if there was an error with either the name or rootKey
     * @throws InvalidKeyException
     *         if there was an error when performing the encryption
     * @throws NoSuchAlgorithmException
     *         if the runtime environment does not support the algorithms required
     */
    protected DataEncryptionKey(String name,
                                byte[] rootKey) throws MicrosoftDataEncryptionException, InvalidKeyException, NoSuchAlgorithmException {
        if (null == name || name.trim().isEmpty()) {
            throw new MicrosoftDataEncryptionException(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidDataEncryptionKey"));
        }

        if (KEY_SIZE_IN_BYTES != rootKey.length) {
            MessageFormat form = new MessageFormat(
                    MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidKeySize"));
            Object[] msgArgs = {KEY_SIZE_IN_BYTES};
            throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }

        this.name = name;

        String encryptionKeySalt = "Microsoft SQL Server cell encryption key with encryption algorithm:AEAD_AES_256_CBC_HMAC_SHA256 and key length:"
                + KEY_SIZE_IN_BITS;
        String macKeySalt = "Microsoft SQL Server cell MAC key with encryption algorithm:AEAD_AES_256_CBC_HMAC_SHA256 and key length:"
                + KEY_SIZE_IN_BITS;
        String ivKeySalt = "Microsoft SQL Server cell IV key with encryption algorithm:AEAD_AES_256_CBC_HMAC_SHA256 and key length:"
                + KEY_SIZE_IN_BITS;

        byte[] encryptionKeyBytes = SecurityUtility.getHMACWithSHA256(encryptionKeySalt.getBytes(UTF_16LE), rootKey,
                KEY_SIZE_IN_BYTES);
        byte[] macKeyBytes = SecurityUtility.getHMACWithSHA256(macKeySalt.getBytes(UTF_16LE), rootKey,
                KEY_SIZE_IN_BYTES);
        byte[] ivKeyBytes = SecurityUtility.getHMACWithSHA256(ivKeySalt.getBytes(UTF_16LE), rootKey, KEY_SIZE_IN_BYTES);

        this.rootKeyBytes = rootKey;
        this.encryptionKeyBytes = encryptionKeyBytes;
        this.macKeyBytes = macKeyBytes;
        this.ivKeyBytes = ivKeyBytes;
    }

    /**
     * Getter for name.
     *
     * @return name of this DataEncryptionKey object.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this key.
     *
     * @param value
     *        name to set
     */
    protected void setName(String value) {
        name = value;
    }

    /**
     * Retrieves rootKeyBytes.
     *
     * @return rootKeyBytes
     */
    protected byte[] getRootKeyBytes() {
        return rootKeyBytes;
    }

    /**
     * Sets the root key bytes.
     *
     * @param value
     *        rootKeyBytes
     */
    protected void setRootKeyBytes(byte[] value) {
        rootKeyBytes = value;
        rootKeyHexString = CryptographyExtensions.toBase64String(value);
    }

    /**
     * Gets the root key in hexstring.
     *
     * @return root key in hexstring
     */
    protected String getRootKeyHexString() {
        return rootKeyHexString;
    }

    /**
     * Gets the encryption key bytes.
     *
     * @return encryption key bytes
     */
    protected byte[] getEncryptionKeyBytes() {
        return encryptionKeyBytes;
    }

    /**
     * Sets the encryption key bytes.
     *
     * @param value
     *        encryption key bytes in byte[]
     */
    protected void setEncryptionKeyBytes(byte[] value) {
        encryptionKeyBytes = value;
    }

    /**
     * Gets the Mac key bytes.
     *
     * @return Mac key bytes
     */
    protected byte[] getMacKeyBytes() {
        return macKeyBytes;
    }

    /**
     * Sets the Mac key bytes.
     *
     * @param value
     *        Mac key bytes
     */
    protected void setMacKeyBytes(byte[] value) {
        macKeyBytes = value;
    }

    /**
     * Gets the IV key bytes
     *
     * @return IV key bytes
     */
    protected byte[] getIvKeyBytes() {
        return ivKeyBytes;
    }

    /**
     * Sets the IV key bytes
     *
     * @param value
     *        IV key bytes
     */
    protected void setIvKeyBytes(byte[] value) {
        ivKeyBytes = value;
    }

    /**
     * Determines if the current DataEncryptionKey's root key is equal to the specified DataEncryptionKey's root key
     * @param otherKey The DataEncryptionKey's to compare with the current DataEncryptionKey's
     * @return true or false
     */
    public boolean rootKeyEquals(DataEncryptionKey otherKey)
    {
        return rootKeyHexString.equals(otherKey.rootKeyHexString);
    }
}
