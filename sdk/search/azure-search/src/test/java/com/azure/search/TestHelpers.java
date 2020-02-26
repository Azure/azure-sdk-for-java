// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.CoreUtils;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.DataSource;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.Skillset;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.LoudHotel;
import com.azure.search.test.environment.models.ModelWithPrimitiveCollections;
import com.azure.search.test.environment.models.NonNullableModel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.unitils.reflectionassert.ReflectionAssert.assertLenientEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

/**
 * This class contains helper methods for running Azure Search tests.
 */
public final class TestHelpers {
    static void assertDataSourcesEqual(DataSource expected, DataSource actual) {
        /*
         * Using 'assertReflectionEquals' will perform a deep check for the objects being equal, this will trigger the
         * ETag to be checked. This value is unknown at the time of the test running so the value should be ignored,
         * therefore we set the expected ETag value to the ETag returned from the service.
         */
        assertReflectionEquals(expected.setETag(actual.getETag()), actual);
    }

    static void assertIndexesEqual(Index expected, Index actual) {
        /*
         * Using 'assertReflectionEquals' will perform a deep check for the objects being equal, this will trigger the
         * ETag to be checked. This value is unknown at the time of the test running so the value should be ignored,
         * therefore we set the expected ETag value to the ETag returned from the service.
         */
        assertReflectionEquals(expected.setETag(actual.getETag()), actual, IGNORE_DEFAULTS);
    }

    static void assertIndexersEqual(Indexer expected, Indexer actual) {
        /*
         * Using 'assertReflectionEquals' will perform a deep check for the objects being equal, this will trigger the
         * ETag to be checked. This value is unknown at the time of the test running so the value should be ignored,
         * therefore we set the expected ETag value to the ETag returned from the service.
         */
        assertReflectionEquals(expected.setETag(actual.getETag()), actual, IGNORE_DEFAULTS);
    }

    static void assertSkillsetsEqual(Skillset expected, Skillset actual) {
        /*
         * Using 'assertReflectionEquals' will perform a deep check for the objects being equal, this will trigger the
         * ETag to be checked. This value is unknown at the time of the test running so the value should be ignored,
         * therefore we set the expected ETag value to the ETag returned from the service.
         */
        assertReflectionEquals(expected.setETag(actual.getETag()), actual, IGNORE_DEFAULTS);
    }

    static void assertHotelsEqual(Hotel expected, Hotel actual) {
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    static void assertLoudHotelsEqual(LoudHotel expected, LoudHotel actual) {
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    static void assetModelsWithPrimitivesEqual(ModelWithPrimitiveCollections expected,
        ModelWithPrimitiveCollections actual) {
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    static void assertDocumentsEqual(Map<String, Object> expected, Map<String, Object> actual) {
        assertLenientEquals(expected, actual);
    }

    static void assetNonNullableModelsEqual(NonNullableModel expected, NonNullableModel actual) {
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

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
    public static AccessCondition generateIfNotExistsAccessCondition() {
        // Setting this access condition modifies the request to include the HTTP If-None-Match conditional header set to "*"
        return new AccessCondition().setIfNoneMatch("*");
    }

    /**
     * Constructs an access condition such that an operation will be performed only if the resource exists.
     *
     * @return an AccessCondition object that represents a condition where a resource exists
     */
    public static AccessCondition generateIfExistsAccessCondition() {

        return new AccessCondition().setIfMatch("*");
    }

    /**
     * Constructs an access condition such that an operation will be performed only if the resource's current ETag value
     * matches the specified ETag value.
     *
     * @param eTag the ETag value to check against the resource's ETag
     * @return An AccessCondition object that represents the If-Match condition
     */
    public static AccessCondition generateIfNotChangedAccessCondition(String eTag) {
        return new AccessCondition().setIfMatch(eTag);
    }
}
