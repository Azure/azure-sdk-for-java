/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * Contains the methods for serializing and deserializing data objects.
 *
 */
public interface ISerializer {
    /**
     * Serializes the provided value.
     *
     * @param value
     *        The value to be serialized
     * @return The serialized data as a byte array
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    byte[] serialize(Object value) throws MicrosoftDataEncryptionException;

    /**
     * Deserializes the provided bytes
     *
     * @param bytes
     *        The data to be deserialized
     * @return The serialized data
     * @throws MicrosoftDataEncryptionException
     *         on error
     */
    Object deserialize(byte[] bytes) throws MicrosoftDataEncryptionException;
}
