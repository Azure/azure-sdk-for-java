// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifySingleCategoryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ClassifySingleCategoryResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link ClassifySingleCategoryActionResult} model.
 */
@Immutable
public final class ClassifySingleCategoryActionResult extends TextAnalyticsActionResult {

    private ClassifySingleCategoryResultCollection documentsResults;

    static {
        ClassifySingleCategoryActionResultPropertiesHelper.setAccessor(
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
    public ClassifySingleCategoryResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(ClassifySingleCategoryResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
