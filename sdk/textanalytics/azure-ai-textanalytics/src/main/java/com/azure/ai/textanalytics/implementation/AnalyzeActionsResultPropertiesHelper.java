// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentActionResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeActionsResult} instance.
 */
public final class AnalyzeActionsResultPropertiesHelper {
    private static AnalyzeActionsResultAccessor accessor;

    private AnalyzeActionsResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeActionsResult} instance.
     */
    public interface AnalyzeActionsResultAccessor {
        void setStatistics(AnalyzeActionsResult analyzeActionsResult,
            TextDocumentBatchStatistics operationStatistics);
        void setRecognizeEntitiesActionResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults);
        void setRecognizeLinkedEntitiesActionResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesActionResults);
        void setRecognizePiiEntitiesActionResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults);
        void setExtractKeyPhrasesActionResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults);
        void setAnalyzeSentimentActionResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<AnalyzeSentimentActionResult> analyzeSentimentActionResults);
    }

    /**
     * The method called from {@link AnalyzeActionsResult} to set it's accessor.
     *
     * @param analyzeActionsResultAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeActionsResultAccessor analyzeActionsResultAccessor) {
        accessor = analyzeActionsResultAccessor;
    }

    public static void setStatistics(AnalyzeActionsResult analyzeActionsResult,
        TextDocumentBatchStatistics operationStatistics) {
        accessor.setStatistics(analyzeActionsResult, operationStatistics);
    }

    public static void setRecognizeEntitiesActionResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults) {
        accessor.setRecognizeEntitiesActionResults(analyzeActionsResult, recognizeEntitiesActionResults);
    }

    public static void setRecognizeLinkedEntitiesActionResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesActionResults) {
        accessor.setRecognizeLinkedEntitiesActionResults(analyzeActionsResult,
            recognizeLinkedEntitiesActionResults);
    }

    public static void setRecognizePiiEntitiesActionResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults) {
        accessor.setRecognizePiiEntitiesActionResults(analyzeActionsResult, recognizePiiEntitiesActionResults);
    }

    public static void setExtractKeyPhrasesActionResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults) {
        accessor.setExtractKeyPhrasesActionResults(analyzeActionsResult, extractKeyPhrasesActionResults);
    }

    public static void setAnalyzeSentimentActionResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<AnalyzeSentimentActionResult> analyzeSentimentActionResults) {
        accessor.setAnalyzeSentimentActionResults(analyzeActionsResult, analyzeSentimentActionResults);
    }
}
