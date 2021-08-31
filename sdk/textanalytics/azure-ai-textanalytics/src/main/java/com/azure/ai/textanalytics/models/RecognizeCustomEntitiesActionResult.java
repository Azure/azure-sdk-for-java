// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizeCustomEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link RecognizeCustomEntitiesActionResult} model.
 */
@Immutable
public final class RecognizeCustomEntitiesActionResult extends TextAnalyticsActionResult {
    private RecognizeCustomEntitiesResultCollection documentsResults;

    static {
        RecognizeCustomEntitiesActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Gets the custom entities recognition action result.
     *
     * @return the custom entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public RecognizeCustomEntitiesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(RecognizeCustomEntitiesResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
