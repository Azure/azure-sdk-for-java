// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ClassifyDocumentResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@code ClassifyDocumentResult} model. It classify the text document one single category.
 */
@Immutable
public final class ClassifyDocumentResult extends TextAnalyticsResult {
    private IterableStream<ClassificationCategory> classifications;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        ClassifyDocumentResultPropertiesHelper.setAccessor(
            new ClassifyDocumentResultPropertiesHelper.ClassifyDocumentResultAccessor() {
                @Override
                public void setClassifications(ClassifyDocumentResult classifyDocumentResult,
                    IterableStream<ClassificationCategory> classifications) {
                    classifyDocumentResult.setClassifications(classifications);
                }

                @Override
                public void setWarnings(ClassifyDocumentResult classifyDocumentResult,
                    IterableStream<TextAnalyticsWarning> warnings) {
                    classifyDocumentResult.setWarnings(warnings);
                }
            });

    }

    /**
     * Creates a {@code ClassifyDocumentResult} model.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public ClassifyDocumentResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * The document classification result which contains the classification category and the confidence score on it.
     *
     * @return {@link IterableStream} of {@link ClassificationCategory}.
     */
    public IterableStream<ClassificationCategory> getClassifications() {
        throwExceptionIfError();
        return classifications;
    }

    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    private void setClassifications(IterableStream<ClassificationCategory> classifications) {
        this.classifications = classifications;
    }

    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }
}
