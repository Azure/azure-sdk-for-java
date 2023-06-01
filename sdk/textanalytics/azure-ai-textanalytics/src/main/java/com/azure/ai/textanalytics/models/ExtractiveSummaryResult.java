// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ExtractiveSummaryResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link ExtractiveSummaryResult} model.
 */
@Immutable
public final class ExtractiveSummaryResult extends TextAnalyticsResult {
    private DetectedLanguage detectedLanguage;
    private IterableStream<ExtractiveSummarySentence> sentences;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        ExtractiveSummaryResultPropertiesHelper.setAccessor(
            new ExtractiveSummaryResultPropertiesHelper.ExtractiveSummaryResultAccessor() {
                @Override
                public void setDetectedLanguage(ExtractiveSummaryResult documentResult,
                                                DetectedLanguage detectedLanguage) {
                    documentResult.setDetectedLanguage(detectedLanguage);
                }

                @Override
                public void setSentences(ExtractiveSummaryResult documentResult,
                                         IterableStream<ExtractiveSummarySentence> sentences) {
                    documentResult.setSentences(sentences);
                }

                @Override
                public void setWarnings(ExtractiveSummaryResult documentResult,
                                        IterableStream<TextAnalyticsWarning> warnings) {
                    documentResult.setWarnings(warnings);
                }
            });
    }

    /**
     * Creates a {@link ExtractiveSummaryResult} model that describes extractive summarization result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public ExtractiveSummaryResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * Get the detectedLanguage property: If 'language' is set to 'auto' for the document in the request this field will
     * contain an object of the language detected for this document.
     *
     * @return the detectedLanguage value.
     */
    public DetectedLanguage getDetectedLanguage() {
        return this.detectedLanguage;
    }

    /**
     * Get the extractive summarization sentence collection.
     *
     * @return The extractive summarization sentence collection.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public IterableStream<ExtractiveSummarySentence> getSentences() {
        throwExceptionIfError();
        return sentences;
    }

    /**
     * Get the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    private void setDetectedLanguage(DetectedLanguage detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    private void setSentences(IterableStream<ExtractiveSummarySentence> sentences) {
        this.sentences = sentences;
    }

    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }
}
