// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.MultiCategoryClassifyActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.MultiCategoryClassifyResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link MultiCategoryClassifyActionResult} is the result for
 */
@Immutable
public final class MultiCategoryClassifyActionResult extends TextAnalyticsActionResult {
    private MultiCategoryClassifyResultCollection documentsResults;

    static {
        MultiCategoryClassifyActionResultPropertiesHelper.setAccessor(
            (actionResult, documentsResults) -> actionResult.setDocumentsResults(documentsResults));
    }

    /**
     * Gets the custom entities recognition action result.
     *
     * @return the custom entities recognition action result.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public MultiCategoryClassifyResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(MultiCategoryClassifyResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
