// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoriesResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link ClassifyDocumentMultiCategoriesResult} model. It classify the text document the multiple categories.
 */
@Immutable
public final class ClassifyDocumentMultiCategoriesResult extends TextAnalyticsResult {
    private DocumentClassificationCollection documentClassifications;

    static {
        ClassifyCustomCategoriesResultPropertiesHelper.setAccessor(
            ((classifyMultiCategoriesResult, documentClassifications) ->
                 classifyMultiCategoriesResult.setDocumentClassifications(documentClassifications))
        );
    }

    /**
     * Creates a {@link ClassifyDocumentMultiCategoriesResult} model that describes recognized document classification
     * result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public ClassifyDocumentMultiCategoriesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * Gets an {@link IterableStream} of {@link DocumentClassification}.
     *
     * @return An {@link IterableStream} of {@link DocumentClassification}.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public DocumentClassificationCollection getDocumentClassifications() {
        throwExceptionIfError();
        return documentClassifications;
    }

    private void setDocumentClassifications(DocumentClassificationCollection documentClassifications) {
        this.documentClassifications = documentClassifications;
    }
}
