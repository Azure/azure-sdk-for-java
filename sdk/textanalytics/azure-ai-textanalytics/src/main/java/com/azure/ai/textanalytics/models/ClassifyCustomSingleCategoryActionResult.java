// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ClassifyCustomSingleCategoryResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link ClassifyCustomSingleCategoryActionResult} model.
 */
@Immutable
public final class ClassifyCustomSingleCategoryActionResult extends TextAnalyticsActionResult {

    private ClassifyCustomSingleCategoryResultCollection documentsResults;

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
    public ClassifyCustomSingleCategoryResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(ClassifyCustomSingleCategoryResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
