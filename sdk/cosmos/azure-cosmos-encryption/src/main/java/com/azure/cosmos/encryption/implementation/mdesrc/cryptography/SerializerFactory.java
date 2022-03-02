/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * Abstract factory for creating serializers.
 *
 */
public abstract class SerializerFactory {

    /**
     * Gets a registered serializer by its Identifier Property.
     *
     * @param identifier
     *        The identifier uniquely identifies a particular Serializer implementation.
     * @return The ISerializer implementation.
     * @throws MicrosoftDataEncryptionException
     *         if identifier not found.
     */
    public abstract ISerializer getSerializer(String identifier) throws MicrosoftDataEncryptionException;

    /**
     * Gets a default registered serializer for the type.
     *
     * @param <T>
     *        The data type to be serialized.
     * @param object
     *        an object of the template type.
     * @return A default registered serializer for the type.
     * @throws MicrosoftDataEncryptionException
     *         if object not supported.
     */
    public abstract <T> ISerializer getDefaultSerializer(Class<?> c) throws MicrosoftDataEncryptionException;

    /**
     * Registers a custom serializer.
     *
     * @param type
     *        The data type on which the Serializer operates.
     * @param sqlSerializer
     *        The Serializer to register.
     * @param overrideDefault
     *        Determines whether to override an existing default serializer for the same type.
     */
    public abstract void registerSerializer(Type type, ISerializer sqlSerializer, boolean overrideDefault);

}
