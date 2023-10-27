// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizeLinkedEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@code RecognizeLinkedEntitiesActionResult} model.
 */
@Immutable
public final class RecognizeLinkedEntitiesActionResult extends TextAnalyticsActionResult {
    private RecognizeLinkedEntitiesResultCollection documentsResults;

    static {
        RecognizeLinkedEntitiesActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Constructs a {@code RecognizeLinkedEntitiesActionResult} model.
     */
    public RecognizeLinkedEntitiesActionResult() {
    }

    /**
     * Gets the linked entities recognition action result.
     *
     * @return The linked entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public RecognizeLinkedEntitiesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(RecognizeLinkedEntitiesResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
