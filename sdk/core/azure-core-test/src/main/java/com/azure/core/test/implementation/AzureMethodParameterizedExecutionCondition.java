// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.test.annotation.AzureMethodSource;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * An {@link ExecutionCondition} implementation which prevents a test method from failing when the {@link
 * AzureMethodSource} being used doesn't provide any test permutations.
 */
public final class AzureMethodParameterizedExecutionCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        AzureMethodSource azureMethodSource = context.getRequiredTestMethod().getAnnotation(AzureMethodSource.class);
        if (azureMethodSource == null) {
            return ConditionEvaluationResult.enabled("Method is not annotated with 'AzureMethodSource'.");
        }

        AzureMethodSourceArgumentsProvider provider = new AzureMethodSourceArgumentsProvider();
        provider.accept(azureMethodSource);

        try {
            if (provider.provideArguments(context).findAny().isPresent()) {
                return ConditionEvaluationResult.enabled("'AzureMethodSource' contains test permutations.");
            } else {
                return ConditionEvaluationResult.disabled("'AzureMethodSource' doesn't contain test permutations.");
            }

        } catch (Exception ignored) {
            // Ignoring the exception here for it to be thrown during execution time of the test.
            return ConditionEvaluationResult.enabled(
                "Unable to determine if 'AzureMethodSource' has test permutations, running anyways.");
        }
    }
}
