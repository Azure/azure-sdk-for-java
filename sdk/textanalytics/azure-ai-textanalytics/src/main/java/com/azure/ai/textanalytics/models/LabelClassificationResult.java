// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.LabelClassificationResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link LabelClassificationResult} model. It classify the text document one single category.
 */
@Immutable
public final class LabelClassificationResult extends TextAnalyticsResult {
    private IterableStream<ClassificationCategory> classifications;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        LabelClassificationResultPropertiesHelper.setAccessor(
            new LabelClassificationResultPropertiesHelper.LabelClassificationResultAccessor() {
                @Override
                public void setClassifications(
                    LabelClassificationResult labelClassificationResult,
                    IterableStream<ClassificationCategory> classifications) {
                    labelClassificationResult.setClassifications(classifications);
                }

                @Override
                public void setWarnings(LabelClassificationResult labelClassificationResult,
                    IterableStream<TextAnalyticsWarning> warnings) {
                    labelClassificationResult.setWarnings(warnings);
                }
            });

    }

    /**
     * Creates a {@link LabelClassificationResult} model.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public LabelClassificationResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * The document classification result which contains the classified category and the confidence score on it.
     *
     * @return The {@link ClassificationCategory}.
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
