// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.CustomClassifyDocumentSingleCategoryResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link CustomClassifyDocumentSingleCategoryActionResult} model.
 */
@Immutable
public final class CustomClassifyDocumentSingleCategoryActionResult extends TextAnalyticsActionResult {

    private CustomClassifyDocumentSingleCategoryResultCollection documentsResults;

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
    public CustomClassifyDocumentSingleCategoryResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(CustomClassifyDocumentSingleCategoryResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
