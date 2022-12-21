// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.AnalyzeHealthcareEntitiesResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link AnalyzeHealthcareEntitiesActionResult} model.
 */
@Immutable
public final class AnalyzeHealthcareEntitiesActionResult extends TextAnalyticsActionResult {
    private AnalyzeHealthcareEntitiesResultCollection documentsResults;

    static {
        AnalyzeHealthcareEntitiesActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Gets the healthcare entities analysis action result.
     *
     * @return The healthcare entities analysis action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public AnalyzeHealthcareEntitiesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(AnalyzeHealthcareEntitiesResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
