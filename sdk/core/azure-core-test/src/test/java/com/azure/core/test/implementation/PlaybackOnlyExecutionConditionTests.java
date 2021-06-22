// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.PlaybackOnly;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link PlaybackOnlyExecutionCondition}.
 */
public class PlaybackOnlyExecutionConditionTests {
    @Test
    public void undeterminedTestMethodRuns() {
        ExtensionContext extensionContext = when(mock(ExtensionContext.class).getTestMethod())
            .thenReturn(Optional.empty())
            .getMock();

        ConditionEvaluationResult evaluationResult = new PlaybackOnlyExecutionCondition()
            .evaluateExecutionCondition(extensionContext);

        assertFalse(evaluationResult.isDisabled());
        assertTrue(evaluationResult.getReason().isPresent());
        assertEquals(PlaybackOnlyExecutionCondition.UNABLE_TO_DETERMINE_METHOD, evaluationResult.getReason().get());
    }

    @ParameterizedTest
    @MethodSource("getEvaluationResultSupplier")
    public void getEvaluationResultSupplier(Method testMethod, TestMode testMode, boolean expectedIsDisabled,
        String expectedReason) {
        ConditionEvaluationResult evaluationResult = PlaybackOnlyExecutionCondition
            .getEvaluationResult(testMethod, testMode);

        assertEquals(expectedIsDisabled, evaluationResult.isDisabled());
        assertTrue(evaluationResult.getReason().isPresent());
        assertEquals(expectedReason, evaluationResult.getReason().get());
    }

    private static Stream<Arguments> getEvaluationResultSupplier() throws NoSuchMethodException {
        Method methodWithoutPlaybackOnly = PlaybackOnlyExecutionConditionTests.class
            .getDeclaredMethod("methodWithoutPlaybackOnly");

        Method methodWithDefaultPlaybackOnly = PlaybackOnlyExecutionConditionTests.class
            .getDeclaredMethod("methodWithDefaultPlaybackOnly");
        String defaultReason = methodWithDefaultPlaybackOnly.getAnnotation(PlaybackOnly.class).reason();

        Method methodWithCustomPlaybackOnly = PlaybackOnlyExecutionConditionTests.class
            .getDeclaredMethod("methodWithCustomPlaybackOnly");
        String customReason = methodWithCustomPlaybackOnly.getAnnotation(PlaybackOnly.class).reason();

        return Stream.of(
            // Test method isn't annotated with @PlaybackOnly.
            Arguments.of(methodWithoutPlaybackOnly, TestMode.PLAYBACK, false,
                PlaybackOnlyExecutionCondition.NOT_ANNOTATED_WITH_PLAYBACK_ONLY),
            Arguments.of(methodWithoutPlaybackOnly, TestMode.LIVE, false,
                PlaybackOnlyExecutionCondition.NOT_ANNOTATED_WITH_PLAYBACK_ONLY),
            Arguments.of(methodWithoutPlaybackOnly, TestMode.RECORD, false,
                PlaybackOnlyExecutionCondition.NOT_ANNOTATED_WITH_PLAYBACK_ONLY),

            // TestMode is PLAYBACK.
            Arguments.of(methodWithDefaultPlaybackOnly, TestMode.PLAYBACK, false,
                PlaybackOnlyExecutionCondition.ANNOTATED_WITH_PLAYBACK_ONLY_IN_PLAYBACK),
            Arguments.of(methodWithCustomPlaybackOnly, TestMode.PLAYBACK, false,
                PlaybackOnlyExecutionCondition.ANNOTATED_WITH_PLAYBACK_ONLY_IN_PLAYBACK),

            // TestMode is LIVE.
            Arguments.of(methodWithDefaultPlaybackOnly, TestMode.LIVE, true, defaultReason),
            Arguments.of(methodWithCustomPlaybackOnly, TestMode.LIVE, true, customReason),

            // TestMode is RECORD.
            Arguments.of(methodWithDefaultPlaybackOnly, TestMode.RECORD, true, defaultReason),
            Arguments.of(methodWithCustomPlaybackOnly, TestMode.RECORD, true, customReason)
        );
    }

    @PlaybackOnly
    private void methodWithDefaultPlaybackOnly() {
    }

    @PlaybackOnly(reason = "Custom reason")
    private void methodWithCustomPlaybackOnly() {
    }

    private void methodWithoutPlaybackOnly() {
    }
}
