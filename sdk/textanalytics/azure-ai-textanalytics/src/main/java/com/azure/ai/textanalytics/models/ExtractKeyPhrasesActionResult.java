// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;


import com.azure.ai.textanalytics.implementation.ExtractKeyPhrasesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;

/**
 * The {@link ExtractKeyPhrasesActionResult} model.
 */
public final class ExtractKeyPhrasesActionResult extends TextAnalyticsActionResult {
    private ExtractKeyPhrasesResultCollection result;

    static {
        ExtractKeyPhrasesActionResultPropertiesHelper.setAccessor(
            (actionsResult, result) -> actionsResult.setResult(result));
    }

    /**
     * Gets the key phrases extraction action result.
     *
     * @return the key phrases extraction action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public ExtractKeyPhrasesResultCollection getResult() {
        throwExceptionIfError();
        return result;
    }

    private void setResult(ExtractKeyPhrasesResultCollection result) {
        this.result = result;
    }
}
