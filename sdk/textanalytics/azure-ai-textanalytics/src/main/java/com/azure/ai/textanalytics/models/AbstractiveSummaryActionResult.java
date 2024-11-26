// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AbstractiveSummaryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.AbstractiveSummaryResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@code AbstractiveSummaryActionResult} model.
 */
@Immutable
public final class AbstractiveSummaryActionResult extends TextAnalyticsActionResult {
    private AbstractiveSummaryResultCollection documentsResults;

    static {
        AbstractiveSummaryActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Constructs a {@code AbstractiveSummaryActionResult} model.
     */
    public AbstractiveSummaryActionResult() {
    }

    /**
     * Gets the abstractive summarization action result.
     *
     * @return The abstractive summarization analysis action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public AbstractiveSummaryResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(AbstractiveSummaryResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
