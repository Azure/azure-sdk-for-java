/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * Contains the methods for serializing and deserializing data objects.
 */
public abstract class Serializer<T> implements ISerializer {
    // The Identifier uniquely identifies a particular Serializer implementation.
    protected String identifier;

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Serializes the provided value.
     *
     * @param value
     *        The value to be serialized
     * @return The serialized data as a byte array
     */
    public abstract byte[] serialize(Object value) throws MicrosoftDataEncryptionException;

    /**
     * Deserializes the provided value.
     *
     * @param value
     *        The value to be deserialized
     * @return The deserialized data
     */
    public abstract T deserialize(byte[] bytes) throws MicrosoftDataEncryptionException;
}
