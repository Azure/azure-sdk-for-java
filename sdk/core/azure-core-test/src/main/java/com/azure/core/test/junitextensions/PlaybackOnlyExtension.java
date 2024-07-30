// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.junitextensions;

import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.PlaybackOnly;
import com.azure.core.test.implementation.TestingHelpers;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

/**
 * Extension to mark tests that should only be run in PLAYBACK test mode.
 */
public class PlaybackOnlyExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        PlaybackOnly playbackOnly = findAnnotation(context.getElement(), PlaybackOnly.class).orElse(null);
        if (playbackOnly != null) {
            // Check if the Playback only time has expired.
            validateExpiryTime(playbackOnly);

            // Only disable evaluation if the annotation is set and the test mode is not playback.
            TestMode testMode = TestingHelpers.getTestMode();
            return (testMode != TestMode.PLAYBACK)
                ? ConditionEvaluationResult.disabled("PlaybackOnly annotation set and test ignored in " + testMode)
                : ConditionEvaluationResult.enabled("PlaybackOnly annotation set and test enabled in " + testMode);
        }

        return ConditionEvaluationResult.enabled("PlaybackOnly annotation not set.");
    }

    private static void validateExpiryTime(PlaybackOnly annotation) {
        String expiryStr = annotation.expiryTime();
        if ("".equals(expiryStr)) {
            return;
        }

        LocalDate expiry = LocalDate.parse(expiryStr, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate now = LocalDate.now();
        if (now.isAfter(expiry)) {
            throw new RuntimeException("PlaybackOnly has expired. Test must be re-enabled");
        }
    }

}
