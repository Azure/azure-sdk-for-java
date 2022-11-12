// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AbstractSummaryResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link AbstractSummaryResult} model.
 */
@Immutable
public final class AbstractSummaryResult extends TextAnalyticsResult {
    private DetectedLanguage detectedLanguage;
    private IterableStream<AbstractiveSummary> summaries;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        AbstractSummaryResultPropertiesHelper.setAccessor(
                new AbstractSummaryResultPropertiesHelper.AbstractSummaryResultAccessor() {
                    @Override
                    public void setDetectedLanguage(AbstractSummaryResult documentResult,
                        DetectedLanguage detectedLanguage) {
                        documentResult.setDetectedLanguage(detectedLanguage);
                    }

                    @Override
                    public void setWarnings(AbstractSummaryResult documentResult,
                        IterableStream<TextAnalyticsWarning> warnings) {
                        documentResult.setWarnings(warnings);
                    }

                    @Override
                    public void setSummaries(AbstractSummaryResult documentResult,
                        IterableStream<AbstractiveSummary> summaries) {
                        documentResult.setSummaries(summaries);
                    }
                });
    }

    /**
     * Creates a {@link AbstractSummaryResult} model that describes the abstractive summarization result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public AbstractSummaryResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error) {
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
     * Gets the abstractive summaries of document.
     *
     * @return The abstractive summaries of document.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public IterableStream<AbstractiveSummary> getSummaries() {
        throwExceptionIfError();
        return summaries;
    }

    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    private void setDetectedLanguage(DetectedLanguage detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }

    private void setSummaries(IterableStream<AbstractiveSummary> summaries) {
        this.summaries = summaries;
    }
}
