// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.common.jsonwrapper.api;

import java.util.List;

public interface JsonApi {

    /**
     * Configure the Json API
     * @param key configuration value key
     * @param value boolean
     */
    void configure(Config key, boolean value);

    /**
     * Set timezone configuration
     */
    void configureTimezone();

    /**
     * Register custom deserializer
     * @param deserializer custom deserialzer type
     * @param <T> Object type
     */
    <T> void registerCustomDeserializer(Deserializer<T> deserializer);

    /**
     * Deserialize string into object
     * @param json string to deserialize
     * @param cls class type to deserialize to
     * @param <T> type to deserialize to
     * @return Object of type T
     */
    <T> T readString(String json, Class<? extends T> cls);

    /**
     * Deserialize string into object
     * @param json string to deserialize
     * @param type type to deserialize to
     * @param <T> type to deserialize to
     * @return Object of type T
     */
    <T> T readString(String json, Type<T> type);

    /**
     * Deserialize string into list
     * @param json string to deserialize
     * @param type type to deserialize to
     * @param <T> type to deserialize to
     * @return list of objects of type T
     */
    <T> List<T> readStringToList(String json, Type<List<T>> type);

    /**
     * Reads a json from file in the project's resources folder, and converts it to a list of objects.
     * The json file must include a list of jsons (objects), that correspond to the type's class structure.
     * @param fileName The json file name in resources folder
     * @param type type to deserialize to
     * @param <T> type to deserialize to
     * @return list of objects of type T
     */
    <T> List<T> readJsonFileToList(String fileName, Type<List<T>> type);

    /**
     * Convert an Object to instance of class T
     * @param source source Object
     * @param cls class type to convert to
     * @param <T> class type to convert to
     * @return an object of type T
     */
    <T> T convertObjectToType(Object source, Class<T> cls);
}
