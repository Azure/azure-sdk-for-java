// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ExtractiveSummaryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ExtractiveSummaryResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@code ExtractiveSummaryActionResult} model.
 */
@Immutable
public final class ExtractiveSummaryActionResult extends TextAnalyticsActionResult {
    private ExtractiveSummaryResultCollection documentsResults;

    static {
        ExtractiveSummaryActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Constructs a {@code ExtractiveSummaryActionResult} model.
     */
    public ExtractiveSummaryActionResult() {
    }

    /**
     * Gets the extractive summarization action result.
     *
     * @return The extractive summarization action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public ExtractiveSummaryResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(ExtractiveSummaryResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
