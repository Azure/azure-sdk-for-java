// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AbstractSummaryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.AbstractSummaryResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link AnalyzeHealthcareEntitiesActionResult} model.
 */
@Immutable
public final class AbstractSummaryActionResult extends TextAnalyticsActionResult {
    private AbstractSummaryResultCollection documentsResults;

    static {
        AbstractSummaryActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Gets the abstractive summarization action result.
     *
     * @return The abstractive summarization analysis action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public AbstractSummaryResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(AbstractSummaryResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
