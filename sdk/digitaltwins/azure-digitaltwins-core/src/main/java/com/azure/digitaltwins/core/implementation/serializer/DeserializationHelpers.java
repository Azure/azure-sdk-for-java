// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.serializer;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;

import java.io.IOException;

import static com.azure.core.util.serializer.TypeReference.createInstance;

/**
 * Helper class for deserializing user-defined types using either a default object mapper or using a user-provided
 * custom json deserializer
 */
public final class DeserializationHelpers {

    /**
     * Deserialize the payload object into a generic type.
     * There are two different paths we will have to take based on the type T
     * In case of a String, we need to write the value of the payload as a String
     * In case of any other type that the user decides to deserialize the payload, we will use mapper.convertValue to
     * perform the conversion.
     * <p>
     * If the customJsonSerializer is not null, then it will be used to deserialize the provided payload into the
     * provided class. Otherwise, this function will use the provided mapper which will never be null.
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeObject(SerializerAdapter adapter, Object payload, Class<T> clazz,
        JsonSerializer customJsonSerializer) throws IOException {
        if (customJsonSerializer == null) {
            if (clazz.isAssignableFrom(String.class)) {
                return clazz.cast(adapter.serialize(payload, SerializerEncoding.JSON));
            } else {
                return adapter.deserialize(adapter.serializeToBytes(payload, SerializerEncoding.JSON), clazz,
                    SerializerEncoding.JSON);
            }
        }

        return customJsonSerializer.deserializeFromBytes(customJsonSerializer.serializeToBytes(payload),
            createInstance(clazz));
    }
}
