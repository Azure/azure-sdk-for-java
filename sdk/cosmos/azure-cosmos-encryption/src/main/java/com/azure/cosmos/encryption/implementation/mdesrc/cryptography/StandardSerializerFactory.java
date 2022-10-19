/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Provides methods for getting serializer implementations, such as by type and ID.
 *
 */
@SuppressWarnings("unchecked")
public class StandardSerializerFactory extends SerializerFactory {

    private Map<String, ISerializer> serializers = new ConcurrentHashMap<>();

    @Override
    public ISerializer getSerializer(String id) throws MicrosoftDataEncryptionException {
        ISerializer s = serializers.get(id);
        if (null == s) {
            s = createSerializer(id);
        }
        return s;
    }

    /**
     * Gets a default registered serializer for the type.
     *
     * @param name
     *        The datatype class name.
     * @return A default registered serializer for the type.
     * @throws MicrosoftDataEncryptionException
     */
    public <T> Serializer<T> getDefaultSerializer(String name) throws MicrosoftDataEncryptionException {
        return (Serializer<T>) getSerializer(name);
    }

    /**
     * Gets a default registered serializer for the type.
     *
     * @param clazz
     *        The data type to be serialized.
     * @return A default registered serializer for the type.
     * @throws MicrosoftDataEncryptionException
     */
    @Override
    public <T> Serializer<T> getDefaultSerializer(Class<?> clazz) throws MicrosoftDataEncryptionException {
        return (Serializer<T>) getSerializer(clazz.getName());
    }

    @Override
    public void registerSerializer(Type type, ISerializer s, boolean overrideDefault) {
        if (null == type || (!overrideDefault && serializers.containsKey(type.getId()))) {
            return;
        }
        serializers.put(type.getId(), s);
    }

    private Serializer<?> createSerializer(String id) throws MicrosoftDataEncryptionException {
        Serializer<?> s = null;
        switch (id) {
            case "java.lang.Boolean":
                s = new BooleanSerializer();
                break;
            case "java.lang.Integer":
                s = new IntegerSerializer();
                break;
            case "java.lang.Long":
                s = new LongSerializer();
                break;
            case "java.lang.Byte":
                s = new ByteSerializer();
                break;
            case "java.lang.Double":
                s = new DoubleSerializer();
                break;
            case "java.lang.Float":
                s = new FloatSerializer();
                break;
            case "java.lang.String":
                s = new StringSerializer();
                break;
            case "java.lang.Character":
                s = new CharSerializer();
                break;
            case "java.util.UUID":
                s = new UuidSerializer();
                break;
            case "java.util.Date":
                s = new DateSerializer();
                break;
            default:
                MessageFormat form = new MessageFormat(
                        MicrosoftDataEncryptionExceptionResource.getResource("R_InvalidSerializerName"));
                Object[] msgArgs = {id.toLowerCase()};
                throw new MicrosoftDataEncryptionException(form.format(msgArgs));
        }
        serializers.put(id, s);
        return s;
    }
}
