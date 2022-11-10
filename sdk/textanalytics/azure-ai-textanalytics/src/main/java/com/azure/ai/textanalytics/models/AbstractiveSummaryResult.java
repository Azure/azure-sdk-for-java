// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AbstractiveSummaryResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link AbstractiveSummaryResult} model.
 */
@Immutable
public final class AbstractiveSummaryResult extends TextAnalyticsResult {
    private DetectedLanguage detectedLanguage;
    private IterableStream<AbstractiveSummary> summaries;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        AbstractiveSummaryResultPropertiesHelper.setAccessor(
                new AbstractiveSummaryResultPropertiesHelper.AbstractiveSummaryResultAccessor() {
                    @Override
                    public void setDetectedLanguage(AbstractiveSummaryResult summaryResult,
                        DetectedLanguage detectedLanguage) {
                        summaryResult.setDetectedLanguage(detectedLanguage);
                    }

                    @Override
                    public void setWarnings(AbstractiveSummaryResult summaryResult,
                        IterableStream<TextAnalyticsWarning> warnings) {
                        summaryResult.setWarnings(warnings);
                    }

                    @Override
                    public void setSummaries(AbstractiveSummaryResult summaryResult,
                        IterableStream<AbstractiveSummary> summaries) {
                        summaryResult.setSummaries(summaries);
                    }
                });
    }

    /**
     * Creates a {@link AbstractiveSummaryResult} model that describes the abstractive summarization result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public AbstractiveSummaryResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error) {
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
