// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;


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
     * Gets an primitive not-nullable boolean value from a {@link JsonNode} object using the field name.
     * @param rootNode {@link JsonNode} object that contains the property.
     * @param propertyName Name of the desired property to be fetched.
     * @return The value of the property if exists, false otherwise.
     */
    public static boolean getNotNullableBooleanValue(JsonNode rootNode, String propertyName) {
        JsonNode booleanNode = rootNode.get(propertyName);
        return booleanNode != null
            ? booleanNode.booleanValue()
            : false;
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

    public static <K, V> Map<K, List<V>> getDictionaryOfListsValues(JsonNode rootNode, String propertyName, Class<K> keyClazz, Class<V> valClazz) {
        JsonNode rootObject = rootNode.get(propertyName);
        if (rootObject == null) {
            return null;
        }

        Map<K, List<V>> result = new HashMap<>();

        for (Iterator<String> it = rootObject.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            List<V> list  = new ArrayList<>();
            JsonNode thisNode = rootObject.get(fieldName);

            if (thisNode.isArray()) {
                for (JsonNode jsonNode : thisNode) {
                    if (valClazz.isAssignableFrom(String.class)) {
                        list.add(valClazz.cast(jsonNode.textValue()));
                    } else if (valClazz.isAssignableFrom(Integer.class)) {
                        list.add(valClazz.cast(Integer.valueOf(jsonNode.intValue())));
                    }
                }

                if (keyClazz.isAssignableFrom(String.class)) {
                    result.put(keyClazz.cast(fieldName), list);
                } else if (keyClazz.isAssignableFrom(Integer.class)) {
                    result.put(keyClazz.cast(Integer.valueOf(fieldName)), list);
                }
            }
        }

        return result;
    }

    public static <K, V> Map<K, V> getDictionaryOfSingularValues(JsonNode rootNode, String propertyName, Class<K> keyClazz, Class<V> valClazz) {
        JsonNode rootObject = rootNode.get(propertyName);
        Map<K, V> result = new HashMap<>();

        for (Iterator<String> it = rootObject.fieldNames(); it.hasNext();) {
            String fieldName = it.next();
            K key = null;

            if (keyClazz.isAssignableFrom(String.class)) {
                key = keyClazz.cast(fieldName);
            } else if (keyClazz.isAssignableFrom(Integer.class)) {
                key = keyClazz.cast(Integer.valueOf(fieldName));
            }

            JsonNode thisNode = rootObject.get(fieldName);

            V value = null;
            if (valClazz.isAssignableFrom(String.class)) {
                value = valClazz.cast(thisNode.textValue());
            } else if (valClazz.isAssignableFrom(Integer.class)) {
                value = valClazz.cast(thisNode.intValue());
            }

            result.put(key, value);
        }

        return result;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }

        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

}
