// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.SingleCategoryClassifyResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link SingleCategoryClassifyActionResult} model.
 */
@Immutable
public final class SingleCategoryClassifyActionResult extends TextAnalyticsActionResult {

    private SingleCategoryClassifyResultCollection documentsResults;

    static {
        ClassifyCustomCategoryActionResultPropertiesHelper.setAccessor(
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
    public SingleCategoryClassifyResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(SingleCategoryClassifyResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
