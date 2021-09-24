// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoriesResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link MultiCategoryClassifyResult} model. It classify the text document the multiple categories.
 */
@Immutable
public final class MultiCategoryClassifyResult extends TextAnalyticsResult {
    private ClassificationCategoryCollection classifications;

    static {
        ClassifyCustomCategoriesResultPropertiesHelper.setAccessor(
            ((classifyMultiCategoriesResult, documentClassifications) ->
                 classifyMultiCategoriesResult.setClassifications(documentClassifications))
        );
    }

    /**
     * Creates a {@link MultiCategoryClassifyResult} model that describes recognized document classification
     * result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public MultiCategoryClassifyResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * Gets an {@link IterableStream} of {@link ClassificationCategory}.
     *
     * @return An {@link IterableStream} of {@link ClassificationCategory}.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public ClassificationCategoryCollection getClassifications() {
        throwExceptionIfError();
        return classifications;
    }

    private void setClassifications(ClassificationCategoryCollection classifications) {
        this.classifications = classifications;
    }
}
