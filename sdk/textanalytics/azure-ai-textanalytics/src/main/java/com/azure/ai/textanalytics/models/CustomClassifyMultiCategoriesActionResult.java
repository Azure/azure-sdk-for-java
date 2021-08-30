// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.CustomClassifyMultiCategoriesActionResultPropertiesHelper;
import com.azure.ai.textanalytics.util.CustomClassifyMultiCategoriesResultCollection;
import com.azure.core.annotation.Immutable;

/**
 * The {@link CustomClassifyMultiCategoriesActionResult} model.
 */
@Immutable
public final class CustomClassifyMultiCategoriesActionResult extends TextAnalyticsActionResult {
    private CustomClassifyMultiCategoriesResultCollection documentsResults;

    static {
        CustomClassifyMultiCategoriesActionResultPropertiesHelper.setAccessor(
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
    public CustomClassifyMultiCategoriesResultCollection getDocumentsResults() {
        throwExceptionIfError();
        return documentsResults;
    }

    private void setDocumentsResults(CustomClassifyMultiCategoriesResultCollection documentsResults) {
        this.documentsResults = documentsResults;
    }
}
