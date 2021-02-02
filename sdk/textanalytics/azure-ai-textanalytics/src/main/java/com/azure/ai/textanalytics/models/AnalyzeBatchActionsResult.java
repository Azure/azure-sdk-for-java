// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeBatchActionsResultPropertiesHelper;
import com.azure.core.util.IterableStream;

/**
 * The {@link AnalyzeBatchActionsResult} model.
 */
public final class AnalyzeBatchActionsResult {
    private TextDocumentBatchStatistics statistics;
    private IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults;
    private IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults;
    private IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults;

    static {
        AnalyzeBatchActionsResultPropertiesHelper.setAccessor(
            new AnalyzeBatchActionsResultPropertiesHelper.AnalyzeBatchActionsResultAccessor() {
                @Override
                public void setStatistics(AnalyzeBatchActionsResult analyzeBatchActionsResult,
                    TextDocumentBatchStatistics operationStatistics) {
                    analyzeBatchActionsResult.setStatistics(operationStatistics);
                }

                @Override
                public void setRecognizeEntitiesActionResults(AnalyzeBatchActionsResult analyzeBatchActionsResult,
                    IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults) {
                    analyzeBatchActionsResult.setRecognizeEntitiesActionResults(recognizeEntitiesActionResults);
                }

                @Override
                public void setRecognizePiiEntitiesActionResults(AnalyzeBatchActionsResult analyzeBatchActionsResult,
                    IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults) {
                    analyzeBatchActionsResult.setRecognizePiiEntitiesActionResults(recognizePiiEntitiesActionResults);
                }

                @Override
                public void setExtractKeyPhrasesActionResults(AnalyzeBatchActionsResult analyzeBatchActionsResult,
                    IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults) {
                    analyzeBatchActionsResult.setExtractKeyPhrasesActionResults(extractKeyPhrasesActionResults);
                }
            });
    }

    /**
     * Get the execute actions operation statistics for analyzing multiple actions.
     *
     * @return {@link TextDocumentBatchStatistics}.
     */
    public TextDocumentBatchStatistics getStatistics() {
        return this.statistics;
    }

    /**
     * Get the {@code recognizeEntitiesActionResults} property: The categorized entities recognition action results
     * property.
     *
     * @return the recognizeEntitiesActionResults value.
     */
    public IterableStream<RecognizeEntitiesActionResult> getRecognizeEntitiesActionResults() {
        return this.recognizeEntitiesActionResults;
    }

    /**
     * Get the {@code recognizePiiEntitiesActionResults} property: The PII entities recognition actions results
     * property.
     *
     * @return the recognizePiiEntitiesActionResults value.
     */
    public IterableStream<RecognizePiiEntitiesActionResult> getRecognizePiiEntitiesActionResults() {
        return this.recognizePiiEntitiesActionResults;
    }

    /**
     * Get the {@code extractKeyPhrasesActionResults} property: The key phrases extraction actions results property.
     *
     * @return the extractKeyPhrasesActionResults value.
     */
    public IterableStream<ExtractKeyPhrasesActionResult> getExtractKeyPhrasesActionResults() {
        return this.extractKeyPhrasesActionResults;
    }

    private void setStatistics(TextDocumentBatchStatistics statistics) {
        this.statistics = statistics;
    }

    private void setRecognizeEntitiesActionResults(
        IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults) {
        this.recognizeEntitiesActionResults = recognizeEntitiesActionResults;
    }

    private void setRecognizePiiEntitiesActionResults(
        IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults) {
        this.recognizePiiEntitiesActionResults = recognizePiiEntitiesActionResults;
    }

    private void setExtractKeyPhrasesActionResults(
        IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults) {
        this.extractKeyPhrasesActionResults = extractKeyPhrasesActionResults;
    }
}
