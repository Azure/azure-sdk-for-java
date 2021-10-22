// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.indexes.models.EntityRecognitionSkill;
import com.azure.search.documents.indexes.models.EntityRecognitionSkillVersion;
import com.azure.search.documents.indexes.models.SentimentSkill;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that multi-version skills throw an exception when an unsupported property is set.
 */
public class SkillsSupportedVersionsTests {
    @ParameterizedTest
    @MethodSource("throwsAsExpectedSupplier")
    public void throwsAsExpected(Executable executable) {
        assertThrows(IllegalArgumentException.class, executable);
    }

    private static Stream<Executable> throwsAsExpectedSupplier() {
        return Stream.of(
            // V1 doesn't support setting a model version.
            () -> new EntityRecognitionSkill(null, null).setModelVersion(""),

            // V3 doesn't support setting include typeless entities.
            () -> new EntityRecognitionSkill(null, null, EntityRecognitionSkillVersion.V3)
                .setTypelessEntitiesIncluded(false),

            // V1 doesn't support setting a model version.
            () -> new SentimentSkill(null, null).setModelVersion(""),

            // V1 doesn't support setting include opinion mining.
            () -> new SentimentSkill(null, null).setIncludeOpinionMining(false)
        );
    }

    @ParameterizedTest
    @MethodSource("doesNotThrowAsExpectedSupplier")
    public void doesNotThrowAsExpected(Executable executable) {
        assertDoesNotThrow(executable);
    }

    private static Stream<Executable> doesNotThrowAsExpectedSupplier() {
        // Setting null values are fine.
        return Stream.of(
            // V1 doesn't support setting a model version.
            () -> new EntityRecognitionSkill(null, null).setModelVersion(null),

            // V3 doesn't support setting include typeless entities.
            () -> new EntityRecognitionSkill(null, null, EntityRecognitionSkillVersion.V3)
                .setTypelessEntitiesIncluded(null),

            // V1 doesn't support setting a model version.
            () -> new SentimentSkill(null, null).setModelVersion(null),

            // V1 doesn't support setting include opinion mining.
            () -> new SentimentSkill(null, null).setIncludeOpinionMining(null)
        );
    }
}
