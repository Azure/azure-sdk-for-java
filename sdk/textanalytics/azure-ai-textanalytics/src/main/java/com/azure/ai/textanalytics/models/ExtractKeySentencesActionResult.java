// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 *
 */
@Immutable
public final class ExtractKeySentencesActionResult extends TextAnalyticsActionResult {
    private ExtractKeySentencesResultCollection documentsResults;

    /**
     * Gets the key sentences extraction action result.
     *
     * @return the key sentences extraction action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public ExtractKeySentencesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }
}
