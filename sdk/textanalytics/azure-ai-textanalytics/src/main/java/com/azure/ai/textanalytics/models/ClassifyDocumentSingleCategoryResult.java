// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyCustomCategoryResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link ClassifyDocumentSingleCategoryResult} model. It classify the text document one single category.
 */
@Immutable
public final class ClassifyDocumentSingleCategoryResult extends TextAnalyticsResult {
    private DocumentClassification documentClassification;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        ClassifyCustomCategoryResultPropertiesHelper.setAccessor(
            new ClassifyCustomCategoryResultPropertiesHelper.ClassifyCustomCategoryResultAccessor() {
                @Override
                public void setDocumentClassification(
                    ClassifyDocumentSingleCategoryResult classifyDocumentSingleCategoryResult,
                    DocumentClassification documentClassification) {
                    classifyDocumentSingleCategoryResult.setDocumentClassification(documentClassification);
                }

                @Override
                public void setWarnings(ClassifyDocumentSingleCategoryResult classifyDocumentSingleCategoryResult,
                    IterableStream<TextAnalyticsWarning> warnings) {
                    classifyDocumentSingleCategoryResult.setWarnings(warnings);
                }
            });

    }

    /**
     * Creates a {@link ClassifyDocumentSingleCategoryResult} model.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public ClassifyDocumentSingleCategoryResult(String id, TextDocumentStatistics textDocumentStatistics,
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
