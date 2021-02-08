// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.HealthcareTaskResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.util.RecognizeHealthcareEntitiesResultCollection;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link HealthcareTaskResult} instance.
 */
public final class HealthcareTaskResultPropertiesHelper {
    private static HealthcareTaskResultAccessor accessor;

    private HealthcareTaskResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link HealthcareTaskResult} instance.
     */
    public interface HealthcareTaskResultAccessor {
        void setResult(HealthcareTaskResult healthcareTaskResult,
            RecognizeHealthcareEntitiesResultCollection recognizeHealthcareEntitiesResultCollection);
        void setErrors(HealthcareTaskResult healthcareTaskResult, List<TextAnalyticsError> errors);
    }

    /**
     * The method called from {@link HealthcareTaskResult} to set it's accessor.
     *
     * @param healthcareTaskResultAccessor The accessor.
     */
    public static void setAccessor(final HealthcareTaskResultAccessor healthcareTaskResultAccessor) {
        accessor = healthcareTaskResultAccessor;
    }

    public static void setResult(HealthcareTaskResult healthcareTaskResult,
        RecognizeHealthcareEntitiesResultCollection recognizeHealthcareEntitiesResultCollection) {
        accessor.setResult(healthcareTaskResult,
            recognizeHealthcareEntitiesResultCollection);
    }

    public static void setErrors(HealthcareTaskResult healthcareTaskResult, List<TextAnalyticsError> errors) {
        accessor.setErrors(healthcareTaskResult, errors);
    }
}
