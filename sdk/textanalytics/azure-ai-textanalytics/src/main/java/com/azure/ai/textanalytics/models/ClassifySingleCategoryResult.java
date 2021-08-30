// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifySingleCategoryResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link ClassifySingleCategoryResult} model.
 */
@Immutable
public final class ClassifySingleCategoryResult extends TextAnalyticsResult {
    private DocumentClassification documentClassification;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        ClassifySingleCategoryResultPropertiesHelper.setAccessor(
            new ClassifySingleCategoryResultPropertiesHelper.ClassifySingleCategoryResultAccessor() {
            @Override
            public void setDocumentClassification(ClassifySingleCategoryResult classifySingleCategoryResult,
                DocumentClassification documentClassification) {
                classifySingleCategoryResult.setDocumentClassification(documentClassification);
            }

            @Override
            public void setWarnings(ClassifySingleCategoryResult classifySingleCategoryResult,
                IterableStream<TextAnalyticsWarning> warnings) {
                classifySingleCategoryResult.setWarnings(warnings);
            }
        });

    }

    /**
     * Creates a {@link ClassifySingleCategoryResult} model.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public ClassifySingleCategoryResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * The document classification result which contains the classified category and the confidence score on it.
     *
     * @return The {@link DocumentClassification}.
     */
    public DocumentClassification getDocumentClassification() {
        throwExceptionIfError();
        return documentClassification;
    }

    // TODO: this is no a recognized pattern.
    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    private void setDocumentClassification(DocumentClassification documentClassification) {
        this.documentClassification = documentClassification;
    }

    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }
}
