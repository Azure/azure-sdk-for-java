// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeSentimentActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;

/**
 * The {@link AnalyzeSentimentActionResult} model.
 */
public final class AnalyzeSentimentActionResult extends TextAnalyticsActionResult {
    private AnalyzeSentimentResultCollection documentResults;

    static {
        AnalyzeSentimentActionResultPropertiesHelper.setAccessor(
            (actionsResult, documentResults) -> actionsResult.setDocumentResults(documentResults));
    }

    /**
     * Gets the key phrases extraction action result.
     *
     * @return the key phrases extraction action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public AnalyzeSentimentResultCollection getDocumentResults() {
        throwExceptionIfError();
        return documentResults;
    }

    private void setDocumentResults(AnalyzeSentimentResultCollection documentResults) {
        this.documentResults = documentResults;
    }
}
