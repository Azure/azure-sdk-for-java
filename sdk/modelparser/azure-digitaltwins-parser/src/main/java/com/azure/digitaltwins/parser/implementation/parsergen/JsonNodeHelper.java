// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to facilitate getting values out of a JsonNode object.
 */
public class JsonNodeHelper {

    /**
     * Gets a text value from a {@link JsonNode} object using the field name.
     * @param rootNode {@link JsonNode} object that contains the property.
     * @param propertyName Name of the desired property to be fetched.
     * @return The value of the property if exists, null otherwise.
     */
    public static String getTextValue(JsonNode rootNode, String propertyName) {
        JsonNode stringNode = rootNode.get(propertyName);
        return stringNode != null
            ? stringNode.textValue()
            : null;
    }

    /**
     * Gets an integer value from a {@link JsonNode} object using the field name.
     * @param rootNode {@link JsonNode} object that contains the property.
     * @param propertyName Name of the desired property to be fetched.
     * @return The value of the property if exists, null otherwise.
     */
    public static Integer getNullableIntegerValue(JsonNode rootNode, String propertyName) {
        JsonNode integerNode = rootNode.get(propertyName);
        return integerNode != null
            ? integerNode.intValue()
            : null;
    }

    /**
     * Gets a {@link Boolean} value from a {@link JsonNode} object using the field name.
     * @param rootNode {@link JsonNode} object that contains the property.
     * @param propertyName Name of the desired property to be fetched.
     * @return The value of the property if exists, null otherwise.
     */
    public static Boolean getNullableBooleanValue(JsonNode rootNode, String propertyName) {
        JsonNode booleanNode = rootNode.get(propertyName);
        return booleanNode != null
            ? booleanNode.booleanValue()
            : null;
    }

    /**
     * Gets an primitive not-nullable boolean value from a {@link JsonNode} object using the field name.
     * @param rootNode {@link JsonNode} object that contains the property.
     * @param propertyName Name of the desired property to be fetched.
     * @return The value of the property if exists, false otherwise.
     */
    public static boolean getNotNullableBooleanValue(JsonNode rootNode, String propertyName) {
        Boolean nullableValue = getNullableBooleanValue(rootNode, propertyName);
        if (nullableValue == null) {
            return false;
        } else {
            return nullableValue;
        }
    }

    /**
     * Gets a {@link List} of values from an array node object.
     * @param rootNode {@link JsonNode} object that contains the array.
     * @param propertyName The property name of the array.
     * @param clazz The type of the collection to be fetched.
     * @param <T> Generic type of the collection.
     * @return The {@link List} of the items in the array.
     */
    public static <T> List<T> getArrayValues(JsonNode rootNode, String propertyName, Class<T> clazz) {
        JsonNode arrayNode = rootNode.get(propertyName);

        if (arrayNode != null && arrayNode.isArray()) {
            List<T> values = new ArrayList<>();

            for (JsonNode jsonNode : arrayNode) {
                if (clazz.isAssignableFrom(String.class)) {
                    values.add(clazz.cast(jsonNode.textValue()));
                } else if (clazz.isAssignableFrom(Integer.class)) {
                    values.add(clazz.cast(Integer.valueOf(jsonNode.intValue())));
                }
            }

            return values;
        }

        return null;
    }
}
