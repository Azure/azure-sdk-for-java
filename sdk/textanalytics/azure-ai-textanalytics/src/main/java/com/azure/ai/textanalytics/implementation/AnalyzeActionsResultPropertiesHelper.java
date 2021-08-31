// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeActionsResult;
import com.azure.ai.textanalytics.models.AnalyzeSentimentActionResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.ExtractSummaryActionResult;
import com.azure.ai.textanalytics.models.ClassifyCustomCategoryActionResult;
import com.azure.ai.textanalytics.models.ClassifyCustomCategoriesActionResult;
import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
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
        void setRecognizeEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesResults);
        void setRecognizeLinkedEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesResults);
        void setRecognizePiiEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesResults);
        void setExtractKeyPhrasesResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesResults);
        void setAnalyzeSentimentResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<AnalyzeSentimentActionResult> analyzeSentimentResults);
        void setExtractSummaryResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<ExtractSummaryActionResult> extractSummaryResults);
        void setRecognizeCustomEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<RecognizeCustomEntitiesActionResult> recognizeCustomEntitiesResults);
        void setClassifyCustomCategoryResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<ClassifyCustomCategoryActionResult> classifyCustomCategoryResults);
        void setClassifyCustomCategoriesResults(AnalyzeActionsResult analyzeActionsResult,
            IterableStream<ClassifyCustomCategoriesActionResult> classifyCustomCategoriesResults);
    }

    /**
     * The method called from {@link AnalyzeActionsResult} to set it's accessor.
     *
     * @param analyzeActionsResultAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeActionsResultAccessor analyzeActionsResultAccessor) {
        accessor = analyzeActionsResultAccessor;
    }

    public static void setRecognizeEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesResults) {
        accessor.setRecognizeEntitiesResults(analyzeActionsResult, recognizeEntitiesResults);
    }

    public static void setRecognizeLinkedEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<RecognizeLinkedEntitiesActionResult> recognizeLinkedEntitiesResults) {
        accessor.setRecognizeLinkedEntitiesResults(analyzeActionsResult,
            recognizeLinkedEntitiesResults);
    }

    public static void setRecognizePiiEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesResults) {
        accessor.setRecognizePiiEntitiesResults(analyzeActionsResult, recognizePiiEntitiesResults);
    }

    public static void setExtractKeyPhrasesResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesResults) {
        accessor.setExtractKeyPhrasesResults(analyzeActionsResult, extractKeyPhrasesResults);
    }

    public static void setAnalyzeSentimentResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<AnalyzeSentimentActionResult> analyzeSentimentResults) {
        accessor.setAnalyzeSentimentResults(analyzeActionsResult, analyzeSentimentResults);
    }

    public static void setExtractSummaryResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<ExtractSummaryActionResult> extractSummaryResults) {
        accessor.setExtractSummaryResults(analyzeActionsResult, extractSummaryResults);
    }

    public static void setRecognizeCustomEntitiesResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<RecognizeCustomEntitiesActionResult> recognizeCustomEntitiesResults) {
        accessor.setRecognizeCustomEntitiesResults(analyzeActionsResult, recognizeCustomEntitiesResults);
    }

    public static void setClassifySingleCategoryResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<ClassifyCustomCategoryActionResult> classifyCustomCategoryResults) {
        accessor.setClassifyCustomCategoryResults(analyzeActionsResult, classifyCustomCategoryResults);
    }

    public static void setClassifyMultiCategoriesResults(AnalyzeActionsResult analyzeActionsResult,
        IterableStream<ClassifyCustomCategoriesActionResult> classifyCustomCategoriesResults) {
        accessor.setClassifyCustomCategoriesResults(analyzeActionsResult, classifyCustomCategoriesResults);
    }
}
