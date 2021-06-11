// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizeLinkedEntitiesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;

/**
 * The {@link RecognizeLinkedEntitiesActionResult} model.
 */
public final class RecognizeLinkedEntitiesActionResult extends TextAnalyticsActionResult {
    private RecognizeLinkedEntitiesResultCollection documentResults;

    static {
        RecognizeLinkedEntitiesActionResultPropertiesHelper.setAccessor(
            (actionsResult, documentResults) -> actionsResult.setDocumentResults(documentResults));
    }

    /**
     * Gets the linked entities recognition action result.
     *
     * @return the linked entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public RecognizeLinkedEntitiesResultCollection getDocumentResults() {
        throwExceptionIfError();
        return documentResults;
    }

    private void setDocumentResults(RecognizeLinkedEntitiesResultCollection documentResults) {
        this.documentResults = documentResults;
    }
}
