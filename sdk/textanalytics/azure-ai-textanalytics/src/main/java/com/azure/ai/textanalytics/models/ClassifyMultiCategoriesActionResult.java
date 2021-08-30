// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyMultiCategoriesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ClassifyMultiCategoriesResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link ClassifyMultiCategoriesActionResult} model.
 */
@Immutable
public final class ClassifyMultiCategoriesActionResult extends TextAnalyticsActionResult {
    private ClassifyMultiCategoriesResultCollection documentsResults;

    static {
        ClassifyMultiCategoriesActionResultPropertiesHelper.setAccessor(
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
    public ClassifyMultiCategoriesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(ClassifyMultiCategoriesResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
