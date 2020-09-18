package com.azure.digitaltwins.core.implementation.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeserializationHelpers {

    /**
     * Converts the payload object into a generic type.
     * There are two different paths we will have to take based on the type T
     * In case of a String, we need to write the value of the payload as a String
     * In case of any other type that the user decides to deserialize the payload, we will use mapper.convertValue to perform the casting.
     */
    @SuppressWarnings("unchecked")
    public static <T> T castObject(ObjectMapper mapper, Object payload, Class<T> clazz) throws JsonProcessingException {
        if (clazz.isAssignableFrom(String.class)){
            return (T)mapper.writeValueAsString(payload);
        }
        else {
            return mapper.convertValue(payload, clazz);
        }
    }
}
