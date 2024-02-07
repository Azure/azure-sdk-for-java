// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.test.TestMode;
import com.azure.storage.common.test.shared.TestEnvironment;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

/**
 * Extension to mark tests that should only be run in PLAYBACK test mode.
 */
public class PlaybackOnlyExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        // Check for the PlaybackOnly annotation on the test method.
        // If it exists, check if the expiry time has passed.
        findAnnotation(context.getElement(), PlaybackOnly.class).ifPresent(PlaybackOnlyExtension::validateExpiryTime);

        TestMode testMode = TestEnvironment.getInstance().getTestMode();
        return (testMode != TestMode.PLAYBACK)
            ? ConditionEvaluationResult.disabled("Test ignored in " + testMode + " mode")
            : ConditionEvaluationResult.enabled("Test enabled in " + testMode + " mode");
    }

    private static void validateExpiryTime(PlaybackOnly annotation) {
        String expiryStr = annotation.expiryTime();
        if ("".equals(expiryStr)) {
            return;
        }
        OffsetDateTime expiry = LocalDate.parse(expiryStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(0, 0)
            .atZone(ZoneId.of(ZoneId.SHORT_IDS.get("PST"))).toOffsetDateTime();
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));
        if (now.isAfter(expiry)) {
            throw new RuntimeException("PlaybackOnly has expired. Test must be re-enabled");
        }
    }


}
