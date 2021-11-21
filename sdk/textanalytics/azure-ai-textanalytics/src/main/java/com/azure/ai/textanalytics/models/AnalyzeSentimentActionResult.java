// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeSentimentActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link AnalyzeSentimentActionResult} model.
 */
@Immutable
public final class AnalyzeSentimentActionResult extends TextAnalyticsActionResult {
    private AnalyzeSentimentResultCollection documentsResults;

    static {
        AnalyzeSentimentActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Gets the sentiment analysis action result.
     *
     * @return The sentiment analysis action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public AnalyzeSentimentResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(AnalyzeSentimentResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
