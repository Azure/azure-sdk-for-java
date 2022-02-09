// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizePiiEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link RecognizePiiEntitiesActionResult} model.
 */
@Immutable
public final class RecognizePiiEntitiesActionResult extends TextAnalyticsActionResult {
    private RecognizePiiEntitiesResultCollection documentsResults;

    static {
        RecognizePiiEntitiesActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Gets the PII entities recognition action result.
     *
     * @return The PII entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public RecognizePiiEntitiesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(RecognizePiiEntitiesResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
