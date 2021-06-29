// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.PlaybackOnly;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * An {@link ExecutionCondition} which requires the test to be running in {@link TestMode} {@link TestMode#PLAYBACK}.
 */
public final class PlaybackOnlyExecutionCondition implements ExecutionCondition {
    static final String NOT_ANNOTATED_WITH_PLAYBACK_ONLY = "Test is not annotated with @PlaybackOnly, "
        + "test will be evaluated.";
    static final String UNABLE_TO_DETERMINE_METHOD = "Unable to determine test method, test will be evaluated.";
    static final String ANNOTATED_WITH_PLAYBACK_ONLY_IN_PLAYBACK = "Test is annotated with @PlaybackOnly and TestMode "
        + "is PLAYBACK, test will be evaluated.";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<Method> potentialTestMethod = context.getTestMethod();

        return (potentialTestMethod.isPresent())
            ? getEvaluationResult(potentialTestMethod.get(), TestingHelpers.getTestMode())
            : ConditionEvaluationResult.enabled(UNABLE_TO_DETERMINE_METHOD);
    }

    static ConditionEvaluationResult getEvaluationResult(Method testMethod, TestMode testMode) {
        PlaybackOnly playbackOnly = testMethod.getAnnotation(PlaybackOnly.class);
        if (playbackOnly == null) {
            return ConditionEvaluationResult.enabled(NOT_ANNOTATED_WITH_PLAYBACK_ONLY);
        }

        return (testMode != TestMode.PLAYBACK)
            ? ConditionEvaluationResult.disabled(playbackOnly.reason())
            : ConditionEvaluationResult.enabled(ANNOTATED_WITH_PLAYBACK_ONLY_IN_PLAYBACK);
    }
}
