// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;


import com.azure.ai.textanalytics.implementation.ExtractKeyPhrasesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link ExtractKeyPhrasesActionResult} model.
 */
@Immutable
public final class ExtractKeyPhrasesActionResult extends TextAnalyticsActionResult {
    private ExtractKeyPhrasesResultCollection documentsResults;

    static {
        ExtractKeyPhrasesActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Gets the key phrases extraction action result.
     *
     * @return The key phrases extraction action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public ExtractKeyPhrasesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(ExtractKeyPhrasesResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
