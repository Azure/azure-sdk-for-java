// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeActionsResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link AnalyzeActionsResult} model.
 */
@Immutable
public final class AnalyzeActionsResult {
    private IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesResults;
    private IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesResults;
    private IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesResults;
    private IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesResults;
    private IterableStream<AnalyzeSentimentActionResult> analyzeSentimentResults;

    static {
        AnalyzeActionsResultPropertiesHelper.setAccessor(
            new AnalyzeActionsResultPropertiesHelper.AnalyzeActionsResultAccessor() {
                @Override
                public void setRecognizeEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
                    IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesResults) {
                    analyzeActionsResult.setRecognizeEntitiesResults(recognizeEntitiesResults);
                }

                @Override
                public void setRecognizeLinkedEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
                    IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesResults) {
                    analyzeActionsResult.setRecognizeLinkedEntitiesResults(
                        recognizeLinkedEntitiesResults);
                }

                @Override
                public void setRecognizePiiEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
                    IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesResults) {
                    analyzeActionsResult.setRecognizePiiEntitiesResults(recognizePiiEntitiesResults);
                }

                @Override
                public void setExtractKeyPhrasesResults(AnalyzeActionsResult analyzeActionsResult,
                    IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesResults) {
                    analyzeActionsResult.setExtractKeyPhrasesResults(extractKeyPhrasesResults);
                }

                @Override
                public void setAnalyzeSentimentResults(AnalyzeActionsResult analyzeActionsResult,
                    IterableStream<AnalyzeSentimentActionResult> analyzeSentimentResults) {
                    analyzeActionsResult.setAnalyzeSentimentResults(analyzeSentimentResults);
                }
            });
    }

    /**
     * Get the {@code recognizeEntitiesActionResults} property: The categorized entities recognition action results
     * property.
     *
     * @return the recognizeEntitiesActionResults value.
     */
    public IterableStream<RecognizeEntitiesActionResult> getRecognizeEntitiesResults() {
        return this.recognizeEntitiesResults;
    }

    /**
     * Get the {@code recognizeLinkedEntitiesActionResults} property: The linked entities recognition action results
     * property.
     *
     * @return the recognizeLinkedEntitiesActionResults value.
     */
    public IterableStream<RecognizeLinkedEntitiesActionResult> getRecognizeLinkedEntitiesResults() {
        return this.recognizeLinkedEntitiesResults;
    }

    /**
     * Get the {@code recognizePiiEntitiesActionResults} property: The PII entities recognition actions results
     * property.
     *
     * @return the recognizePiiEntitiesActionResults value.
     */
    public IterableStream<RecognizePiiEntitiesActionResult> getRecognizePiiEntitiesResults() {
        return this.recognizePiiEntitiesResults;
    }

    /**
     * Get the {@code extractKeyPhrasesActionResults} property: The key phrases extraction actions results property.
     *
     * @return the extractKeyPhrasesActionResults value.
     */
    public IterableStream<ExtractKeyPhrasesActionResult> getExtractKeyPhrasesResults() {
        return this.extractKeyPhrasesResults;
    }

    /**
     * Get the {@code analyzeSentimentActionResults} property: The sentiment analysis actions results property.
     *
     * @return the analyzeSentimentActionResults value.
     */
    public IterableStream<AnalyzeSentimentActionResult> getAnalyzeSentimentResults() {
        return this.analyzeSentimentResults;
    }

    private void setRecognizeEntitiesResults(
        IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesResults) {
        this.recognizeEntitiesResults = recognizeEntitiesResults;
    }

    private void setRecognizeLinkedEntitiesResults(
        IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesResults) {
        this.recognizeLinkedEntitiesResults = recognizeLinkedEntitiesResults;
    }

    private void setRecognizePiiEntitiesResults(
        IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesResults) {
        this.recognizePiiEntitiesResults = recognizePiiEntitiesResults;
    }

    private void setExtractKeyPhrasesResults(
        IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesResults) {
        this.extractKeyPhrasesResults = extractKeyPhrasesResults;
    }

    private void setAnalyzeSentimentResults(
        IterableStream<AnalyzeSentimentActionResult> analyzeSentimentResults) {
        this.analyzeSentimentResults = analyzeSentimentResults;
    }
}
