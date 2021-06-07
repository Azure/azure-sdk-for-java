// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeActionsResultPropertiesHelper;
import com.azure.core.util.IterableStream;

/**
 * The {@link AnalyzeActionsResult} model.
 */
public final class AnalyzeActionsResult {
    private TextDocumentBatchStatistics statistics;
    private IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults;
    private IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesActionResults;
    private IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults;
    private IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults;
    private IterableStream<AnalyzeSentimentActionResult> analyzeSentimentActionResults;

    static {
        AnalyzeActionsResultPropertiesHelper.setAccessor(
            new AnalyzeActionsResultPropertiesHelper.AnalyzeActionsResultAccessor() {
                @Override
                public void setStatistics(AnalyzeActionsResult analyzeActionsResult,
                    TextDocumentBatchStatistics operationStatistics) {
                    analyzeActionsResult.setStatistics(operationStatistics);
                }

                @Override
                public void setRecognizeEntitiesActionResults(AnalyzeActionsResult analyzeActionsResult,
                    IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults) {
                    analyzeActionsResult.setRecognizeEntitiesActionResults(recognizeEntitiesActionResults);
                }

                @Override
                public void setRecognizeLinkedEntitiesActionResults(AnalyzeActionsResult analyzeActionsResult,
                    IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesActionResults) {
                    analyzeActionsResult.setRecognizeLinkedEntitiesActionResults(
                        recognizeLinkedEntitiesActionResults);
                }

                @Override
                public void setRecognizePiiEntitiesActionResults(AnalyzeActionsResult analyzeActionsResult,
                    IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults) {
                    analyzeActionsResult.setRecognizePiiEntitiesActionResults(recognizePiiEntitiesActionResults);
                }

                @Override
                public void setExtractKeyPhrasesActionResults(AnalyzeActionsResult analyzeActionsResult,
                    IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults) {
                    analyzeActionsResult.setExtractKeyPhrasesActionResults(extractKeyPhrasesActionResults);
                }

                @Override
                public void setAnalyzeSentimentActionResults(AnalyzeActionsResult analyzeActionsResult,
                    IterableStream<AnalyzeSentimentActionResult> analyzeSentimentActionResults) {
                    analyzeActionsResult.setAnalyzeSentimentActionResults(analyzeSentimentActionResults);
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
     * Get the {@code recognizeLinkedEntitiesActionResults} property: The linked entities recognition action results
     * property.
     *
     * @return the recognizeLinkedEntitiesActionResults value.
     */
    public IterableStream<RecognizeLinkedEntitiesActionResult> getRecognizeLinkedEntitiesActionResults() {
        return this.recognizeLinkedEntitiesActionResults;
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

    /**
     * Get the {@code analyzeSentimentActionResults} property: The sentiment analysis actions results property.
     *
     * @return the analyzeSentimentActionResults value.
     */
    public IterableStream<AnalyzeSentimentActionResult> getAnalyzeSentimentActionResults() {
        return this.analyzeSentimentActionResults;
    }

    private void setStatistics(TextDocumentBatchStatistics statistics) {
        this.statistics = statistics;
    }

    private void setRecognizeEntitiesActionResults(
        IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults) {
        this.recognizeEntitiesActionResults = recognizeEntitiesActionResults;
    }

    private void setRecognizeLinkedEntitiesActionResults(
        IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesActionResults) {
        this.recognizeLinkedEntitiesActionResults = recognizeLinkedEntitiesActionResults;
    }

    private void setRecognizePiiEntitiesActionResults(
        IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults) {
        this.recognizePiiEntitiesActionResults = recognizePiiEntitiesActionResults;
    }

    private void setExtractKeyPhrasesActionResults(
        IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults) {
        this.extractKeyPhrasesActionResults = extractKeyPhrasesActionResults;
    }

    private void setAnalyzeSentimentActionResults(
        IterableStream<AnalyzeSentimentActionResult> analyzeSentimentActionResults) {
        this.analyzeSentimentActionResults = analyzeSentimentActionResults;
    }
}
