// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoriesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ClassifyCustomMultiCategoriesResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link ClassifyCustomMultiCategoriesActionResult} model.
 */
@Immutable
public final class ClassifyCustomMultiCategoriesActionResult extends TextAnalyticsActionResult {
    private ClassifyCustomMultiCategoriesResultCollection documentsResults;

    static {
        ClassifyCustomCategoriesActionResultPropertiesHelper.setAccessor(
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
    public ClassifyCustomMultiCategoriesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(ClassifyCustomMultiCategoriesResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
