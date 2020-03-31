// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.MatchConditions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class contains helper methods for running Azure Search tests.
 */
public final class TestHelpers {
    /**
     * Checks if the passed {@link CharSequence} is {@code null}, empty, or only contains spaces.
     *
     * @param charSequence {@link CharSequence} to check for being blank.
     * @return {@code true} if the {@link CharSequence} is {@code null}, empty, or only contains spaces, otherwise
     * {@code false}.
     */
    public static boolean isBlank(CharSequence charSequence) {
        if (CoreUtils.isNullOrEmpty(charSequence)) {
            return true;
        }

        return charSequence.chars().allMatch(Character::isWhitespace);
    }

    /**
     * Gets the {@code "eTag"} value from the passed object.
     *
     * @param obj The object that will have its eTag value retrieved.
     * @return The eTag value if the object has an {@code "eTag"} field, otherwise {@code ""}.
     */
    public static String getETag(Object obj) {
        Class<?> clazz = obj.getClass();
        try {
            // Try using the getter method first.
            return (String) clazz.getMethod("getETag").invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            try {
                // Next attempt to get the value from the field directly.
                Field eTagField = clazz.getField("eTag");
                eTagField.setAccessible(true);
                return (String) eTagField.get(obj);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
                // Finally just return empty string since we couldn't access the method or field.
                return "";
            }
        }
    }

    /**
     * Constructs an access condition such that an operation will be performed only if the resource does not exist.
     *
     * @return an AccessCondition object that represents a condition where a resource does not exist
     */
    public static MatchConditions generateIfNotExistsAccessCondition() {
        // Setting this access condition modifies the request to include the HTTP If-None-Match conditional header set to "*"
        return new MatchConditions().setIfNoneMatch("*");
    }

    /**
     * Constructs an access condition such that an operation will be performed only if the resource exists.
     *
     * @return an AccessCondition object that represents a condition where a resource exists
     */
    public static MatchConditions generateIfExistsAccessCondition() {

        return new MatchConditions().setIfMatch("*");
    }

    /**
     * Constructs an access condition such that an operation will be performed only if the resource's current ETag value
     * matches the specified ETag value.
     *
     * @param eTag the ETag value to check against the resource's ETag
     * @return An AccessCondition object that represents the If-Match condition
     */
    public static MatchConditions generateIfNotChangedAccessCondition(String eTag) {
        return new MatchConditions().setIfMatch(eTag);
    }

    public static void assertObjectEquals(Object expected, Object actual) {
        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        try {
            assertEquals(jacksonAdapter.serialize(expected, SerializerEncoding.JSON),
                jacksonAdapter.serialize(actual, SerializerEncoding.JSON));
        } catch (IOException ex) {
            fail("There is something wrong happen in serializer.");
        }
    }

    public static void assertObjectEquals(Object expected, Object actual, boolean ignoreDefaults,
        String ... ignoreFields) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode expectedNode = mapper.valueToTree(expected);
        ObjectNode actualNode = mapper.valueToTree(actual);
        assertOnMapIterator(expectedNode.fields(), actualNode, ignoreDefaults, ignoreFields);
    }

    private static void assertOnMapIterator(Iterator<Map.Entry<String, JsonNode>> expectedNode,
        ObjectNode actualNode, boolean ignoreDefaults, String[] ignoreFields) {
        while (expectedNode.hasNext()) {
            assertTrue(actualNode.fields().hasNext());
            Map.Entry<String, JsonNode> expectedField = expectedNode.next();
            String fieldName = expectedField.getKey();
            if (!doesFieldNeedCheck(fieldName, expectedField.getValue(), ignoreDefaults, ignoreFields)) {
                continue;
            }
            if (expectedField.getValue().isValueNode()) {
                assertEquals(expectedField.getValue(), actualNode.get(expectedField.getKey()));
            } else if (expectedField.getValue().isArray()) {
                Iterator<JsonNode> expectedArray = expectedField.getValue().elements();
                Iterator<JsonNode> actualArray = actualNode.get(expectedField.getKey()).elements();
                while (expectedArray.hasNext() ) {
                    assertTrue(actualArray.hasNext());
                    Iterator<JsonNode> expectedElements = expectedArray.next().elements();
                    Iterator<JsonNode> actualElements = actualArray.next().elements();
                    while (expectedElements.hasNext()) {
                        assertTrue(actualElements.hasNext());
                        JsonNode a = expectedElements.next();
                        JsonNode b = actualElements.next();
                        if (Arrays.asList(ignoreFields).contains(fieldName)) {
                            continue;
                        }
                        if (!doesFieldNeedCheck(null, a, true)) {
                            continue;
                        }
                        assertEquals(a.asText(), b.asText());
                    }
                }
            } else {
                assertObjectEquals(expectedField.getValue(), actualNode.get(expectedField.getKey()), ignoreDefaults, ignoreFields);
            }
        }
    }

    private static boolean doesFieldNeedCheck(String fieldName, JsonNode fieldValue,
        boolean ignoreDefaults, String... ignoreFields) {
        if (Arrays.asList(ignoreFields).contains(fieldName)) {
            return false;
        }

        if (ignoreDefaults) {
            if (fieldValue.isNull()) {
                return false;
            }
            if (fieldValue.isBoolean() && !fieldValue.asBoolean()) {
                return false;
            }
            return !fieldValue.isNumber() || fieldValue.asDouble() != 0.0D;
        }
        return true;
    }
}
