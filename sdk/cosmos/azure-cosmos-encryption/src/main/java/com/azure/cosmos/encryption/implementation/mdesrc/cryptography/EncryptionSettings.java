/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * Contains the settings to configure how encryption operations are performed on data.
 *
 */
public abstract class EncryptionSettings {
    /**
     * The data encryption key.
     */
    protected DataEncryptionKey dataEncryptionKey;

    /**
     * Getter for dataEncryptionKey.
     *
     * @return dataEncryptionKey value
     */
    public DataEncryptionKey getDataEncryptionKey() {
        return dataEncryptionKey;
    }

    /**
     * Sets the data encryption key.
     *
     * @param d
     *        encryptionKey
     */
    protected void setDataEncryptionKey(DataEncryptionKey d) {
        if (null == d) {
            encryptionType = EncryptionType.Plaintext;
        }
        dataEncryptionKey = d;
    }

    /**
     * Type of encryption, Deterministic or Randomized only.
     */
    protected EncryptionType encryptionType = EncryptionType.Randomized;

    /**
     * Getter for encryptionType.
     *
     * @return encryptionType value
     */
    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    /**
     * Set the encryption type
     *
     * @param e
     *        encryption type
     */
    protected void setEncryptionType(EncryptionType e) {
        encryptionType = e;
    }

    /**
     * Gets the ISerializer.
     *
     * @return ISerializer instance
     */
    public abstract ISerializer getSerializer();

    /**
     * Get the default encryption type.
     *
     * @param d
     *        encryption key
     * @return type of encryption
     */
    protected static EncryptionType getDefaultEncryptionType(DataEncryptionKey d) {
        return (null == d) ? EncryptionType.Plaintext : EncryptionType.Randomized;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other || !(other instanceof EncryptionSettings)) {
            return false;
        }

        if (null == dataEncryptionKey && null == ((EncryptionSettings) other).dataEncryptionKey) {
            return true;
        } else if (null != dataEncryptionKey) {
            return dataEncryptionKey.equals(((EncryptionSettings) other).dataEncryptionKey)
                    && encryptionType.equals(((EncryptionSettings) other).encryptionType)
                    && getSerializer().getClass() == ((EncryptionSettings) other).getSerializer().getClass();
        }
        return false;
    }
}
