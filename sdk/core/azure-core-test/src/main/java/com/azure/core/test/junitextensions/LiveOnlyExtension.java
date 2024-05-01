// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.junitextensions;

import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.test.implementation.TestingHelpers;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

/**
 * Extension to mark tests that should only be run in LIVE test mode.
 */
public class LiveOnlyExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        LiveOnly liveOnly = findAnnotation(context.getElement(), LiveOnly.class).orElse(null);
        if (liveOnly != null) {
            // Only disable evaluation if the annotation is set and the test mode is not live.
            TestMode testMode = TestingHelpers.getTestMode();
            return (testMode != TestMode.LIVE)
                ? ConditionEvaluationResult.disabled("LiveOnly annotation set and test ignored in " + testMode)
                : ConditionEvaluationResult.enabled("LiveOnly annotation set and test enabled in " + testMode);
        }

        return ConditionEvaluationResult.enabled("LiveOnly annotation not set.");
    }
}
