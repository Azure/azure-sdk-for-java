// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.test.TestMode;
import com.azure.storage.common.test.shared.TestEnvironment;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Extension to mark tests that should only be run in LIVE test mode.
 */
public class LiveOnlyExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        TestMode testMode = TestEnvironment.getInstance().getTestMode();
        return (testMode != TestMode.LIVE)
            ? ConditionEvaluationResult.disabled("Test ignored in " + testMode + " mode")
            : ConditionEvaluationResult.enabled("Test enabled in " + testMode + " mode");
    }
}
