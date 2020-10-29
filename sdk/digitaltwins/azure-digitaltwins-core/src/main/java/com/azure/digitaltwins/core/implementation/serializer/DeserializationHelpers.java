// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.serializer;

import com.azure.core.util.serializer.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static com.azure.core.util.serializer.TypeReference.createInstance;

/**
 * Helper class for deserializing user-defined types using either a default object mapper or using a user-provided custom json deserializer
 */
public final class DeserializationHelpers {

    /**
     * Deserialize the payload object into a generic type.
     * There are two different paths we will have to take based on the type T
     * In case of a String, we need to write the value of the payload as a String
     * In case of any other type that the user decides to deserialize the payload, we will use mapper.convertValue to perform the conversion.
     *
     * If the customJsonSerializer is not null, then it will be used to deserialize the provided payload into the provided class. Otherwise
     * this function will use the provided mapper which will never be null.
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeObject(ObjectMapper mapper, Object payload, Class<T> clazz, JsonSerializer customJsonSerializer) throws JsonProcessingException {
        if (customJsonSerializer == null) {
            if (clazz.isAssignableFrom(String.class)) {
                return (T)mapper.writeValueAsString(payload);
            }
            else {
                return mapper.convertValue(payload, clazz);
            }
        }

        ByteArrayOutputStream sourceStream = new ByteArrayOutputStream();
        customJsonSerializer.serialize(sourceStream, payload);
        return customJsonSerializer.deserialize(new ByteArrayInputStream(sourceStream.toByteArray()), createInstance(clazz));
    }
}
