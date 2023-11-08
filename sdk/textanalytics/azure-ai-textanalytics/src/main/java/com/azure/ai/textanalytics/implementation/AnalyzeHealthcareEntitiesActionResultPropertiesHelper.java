// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesActionResult;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeHealthcareEntitiesActionResult} instance.
 */
public final class AnalyzeHealthcareEntitiesActionResultPropertiesHelper {
    private static AnalyzeHealthcareEntitiesActionResultAccessor accessor;

    private AnalyzeHealthcareEntitiesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeHealthcareEntitiesActionResult}
     * instance.
     */
    public interface AnalyzeHealthcareEntitiesActionResultAccessor {
        void setDocumentsResults(AnalyzeHealthcareEntitiesActionResult actionResult,
            AnalyzeHealthcareEntitiesResultCollection documentsResults);
    }

    /**
     * The method called from {@link AnalyzeHealthcareEntitiesActionResult} to set it's accessor.
     *
     * @param analyzeHealthcareEntitiesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeHealthcareEntitiesActionResultAccessor analyzeHealthcareEntitiesActionResultAccessor) {
        accessor = analyzeHealthcareEntitiesActionResultAccessor;
    }

    public static void setDocumentsResults(AnalyzeHealthcareEntitiesActionResult actionResult,
        AnalyzeHealthcareEntitiesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
