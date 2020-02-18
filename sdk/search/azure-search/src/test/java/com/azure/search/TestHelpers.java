// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.CoreUtils;
import com.azure.search.models.DataSource;
import com.azure.search.models.Index;
import com.azure.search.models.Indexer;
import com.azure.search.models.Skillset;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.LoudHotel;
import com.azure.search.test.environment.models.ModelWithPrimitiveCollections;
import com.azure.search.test.environment.models.NonNullableModel;

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

    public static boolean isBlank(CharSequence charSequence) {
        if (CoreUtils.isNullOrEmpty(charSequence)) {
            return true;
        }

        return charSequence.chars().allMatch(Character::isWhitespace);
    }
}
