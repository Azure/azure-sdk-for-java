// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeBatchActionsResult;
import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeBatchActionsResult} instance.
 */
public final class AnalyzeBatchActionsResultPropertiesHelper {
    private static AnalyzeBatchActionsResultAccessor accessor;

    private AnalyzeBatchActionsResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeBatchActionsResult} instance.
     */
    public interface AnalyzeBatchActionsResultAccessor {
        void setStatistics(AnalyzeBatchActionsResult analyzeBatchActionsResult,
            TextDocumentBatchStatistics operationStatistics);
        void setRecognizeEntitiesActionResults(AnalyzeBatchActionsResult analyzeBatchActionsResult,
            IterableStream<RecognizeEntitiesActionResult> recognizeEntitiesActionResults);
        void setRecognizePiiEntitiesActionResults(AnalyzeBatchActionsResult analyzeBatchActionsResult,
            IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults);
        void setExtractKeyPhrasesActionResults(AnalyzeBatchActionsResult analyzeBatchActionsResult,
            IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults);
    }

    /**
     * The method called from {@link AnalyzeBatchActionsResult}
     * to set it's accessor.
     *
     * @param analyzeBatchActionsResultAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeBatchActionsResultAccessor analyzeBatchActionsResultAccessor) {
        accessor = analyzeBatchActionsResultAccessor;
    }

    public static void setStatistics(AnalyzeBatchActionsResult analyzeBatchActionsResult,
        TextDocumentBatchStatistics operationStatistics) {
        accessor.setStatistics(analyzeBatchActionsResult, operationStatistics);
    }

    public static void setRecognizeEntitiesActionResults(AnalyzeBatchActionsResult analyzeBatchActionsResult,
        IterableStream<RecognizeEntitiesActionResult> entityRecognitionActionResults) {
        accessor.setRecognizeEntitiesActionResults(analyzeBatchActionsResult, entityRecognitionActionResults);
    }

    public static void setRecognizePiiEntitiesActionResults(AnalyzeBatchActionsResult analyzeBatchActionsResult,
        IterableStream<RecognizePiiEntitiesActionResult> recognizePiiEntitiesActionResults) {
        accessor.setRecognizePiiEntitiesActionResults(analyzeBatchActionsResult, recognizePiiEntitiesActionResults);
    }

    public static void setExtractKeyPhrasesActionResults(AnalyzeBatchActionsResult analyzeBatchActionsResult,
        IterableStream<ExtractKeyPhrasesActionResult> extractKeyPhrasesActionResults) {
        accessor.setExtractKeyPhrasesActionResults(analyzeBatchActionsResult, extractKeyPhrasesActionResults);
    }
}
