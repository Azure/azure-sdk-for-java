// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.SingleCategoryClassifyResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link SingleCategoryClassifyResult} model. It classify the text document one single category.
 */
@Immutable
public final class SingleCategoryClassifyResult extends TextAnalyticsResult {
    private ClassificationCategory classification;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        SingleCategoryClassifyResultPropertiesHelper.setAccessor(
            new SingleCategoryClassifyResultPropertiesHelper.SingleCategoryClassifyResultAccessor() {
                @Override
                public void setClassification(
                    SingleCategoryClassifyResult singleCategoryClassifyResult,
                    ClassificationCategory classification) {
                    singleCategoryClassifyResult.setClassification(classification);
                }

                @Override
                public void setWarnings(SingleCategoryClassifyResult singleCategoryClassifyResult,
                    IterableStream<TextAnalyticsWarning> warnings) {
                    singleCategoryClassifyResult.setWarnings(warnings);
                }
            });

    }

    /**
     * Creates a {@link SingleCategoryClassifyResult} model.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public SingleCategoryClassifyResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * The document classification result which contains the classified category and the confidence score on it.
     *
     * @return The {@link ClassificationCategory}.
     */
    public ClassificationCategory getClassification() {
        throwExceptionIfError();
        return classification;
    }

    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    private void setClassification(ClassificationCategory classification) {
        this.classification = classification;
    }

    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }
}
