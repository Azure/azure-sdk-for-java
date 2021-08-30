// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.CustomClassifySingleCategoryActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.CustomClassifySingleCategoryResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link CustomClassifySingleCategoryActionResult} model.
 */
@Immutable
public final class CustomClassifySingleCategoryActionResult extends TextAnalyticsActionResult {

    private CustomClassifySingleCategoryResultCollection documentsResults;

    static {
        CustomClassifySingleCategoryActionResultPropertiesHelper.setAccessor(
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
    public CustomClassifySingleCategoryResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(CustomClassifySingleCategoryResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
