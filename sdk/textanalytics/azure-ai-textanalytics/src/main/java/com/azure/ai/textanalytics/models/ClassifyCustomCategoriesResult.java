// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoriesResultPropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@link ClassifyCustomCategoriesResult} model.
 */
@Immutable
public final class ClassifyCustomCategoriesResult extends TextAnalyticsResult {
    private DocumentClassificationCollection documentClassifications;

    static {
        ClassifyCustomCategoriesResultPropertiesHelper.setAccessor(
            ((classifyMultiCategoriesResult, documentClassifications) ->
                 classifyMultiCategoriesResult.setDocumentClassifications(documentClassifications))
        );
    }

    /**
     * Creates a {@link ClassifyCustomCategoriesResult} model that describes recognized document classification
     * result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public ClassifyCustomCategoriesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
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
