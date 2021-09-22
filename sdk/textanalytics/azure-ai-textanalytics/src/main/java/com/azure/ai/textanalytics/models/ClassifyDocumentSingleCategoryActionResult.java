// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ClassifyDocumentSingleCategoryResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link ClassifyDocumentSingleCategoryActionResult} model.
 */
@Immutable
public final class ClassifyDocumentSingleCategoryActionResult extends TextAnalyticsActionResult {

    private ClassifyDocumentSingleCategoryResultCollection documentsResults;

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
    public ClassifyDocumentSingleCategoryResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(ClassifyDocumentSingleCategoryResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
