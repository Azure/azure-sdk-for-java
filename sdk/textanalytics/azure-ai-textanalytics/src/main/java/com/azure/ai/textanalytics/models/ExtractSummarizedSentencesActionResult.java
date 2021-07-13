// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 *
 */
@Immutable
public final class ExtractSummarizedSentencesActionResult extends TextAnalyticsActionResult {
    private ExtractSummarizedSentencesResultCollection documentsResults;

    /**
     * Gets the summarized sentences extraction action result.
     *
     * @return the summarized sentences extraction action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public ExtractSummarizedSentencesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }
}
