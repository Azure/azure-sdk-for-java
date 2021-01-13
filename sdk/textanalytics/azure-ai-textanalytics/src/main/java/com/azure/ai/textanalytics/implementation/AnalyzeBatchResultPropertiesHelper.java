// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeBatchTasksResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeBatchTasksResult} instance.
 */
public final class AnalyzeBatchResultPropertiesHelper {
    private static AnalyzeTasksResultAccessor accessor;

    private AnalyzeBatchResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeBatchTasksResult} instance.
     */
    public interface AnalyzeTasksResultAccessor {
        void setStatistics(AnalyzeBatchTasksResult analyzeBatchTasksResult,
            TextDocumentBatchStatistics operationStatistics);
        void setEntitiesRecognitionResults(AnalyzeBatchTasksResult analyzeBatchTasksResult,
            IterableStream<RecognizeEntitiesResultCollection> entitiesRecognitionResults);
        void setPiiEntitiesRecognitionResults(AnalyzeBatchTasksResult analyzeBatchTasksResult,
            IterableStream<RecognizePiiEntitiesResultCollection> piiEntitiesRecognitionResults);
        void setKeyPhrasesExtractionResults(AnalyzeBatchTasksResult analyzeBatchTasksResult,
            IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesExtractionResults);
    }

    /**
     * The method called from {@link AnalyzeBatchTasksResult} to set it's accessor.
     *
     * @param analyzeTasksResultAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeTasksResultAccessor analyzeTasksResultAccessor) {
        accessor = analyzeTasksResultAccessor;
    }

    public static void setStatistics(AnalyzeBatchTasksResult analyzeBatchTasksResult,
        TextDocumentBatchStatistics operationStatistics) {
        accessor.setStatistics(analyzeBatchTasksResult, operationStatistics);
    }

    public static void setEntityRecognitionResults(AnalyzeBatchTasksResult analyzeBatchTasksResult,
        IterableStream<RecognizeEntitiesResultCollection> entityRecognitionResults) {
        accessor.setEntitiesRecognitionResults(analyzeBatchTasksResult, entityRecognitionResults);
    }

    public static void setPiiEntityRecognitionResults(AnalyzeBatchTasksResult analyzeBatchTasksResult,
        IterableStream<RecognizePiiEntitiesResultCollection> piiEntityRecognitionResults) {
        accessor.setPiiEntitiesRecognitionResults(analyzeBatchTasksResult, piiEntityRecognitionResults);
    }

    public static void setKeyPhraseExtractionResults(AnalyzeBatchTasksResult analyzeBatchTasksResult,
        IterableStream<ExtractKeyPhrasesResultCollection> keyPhraseExtractionResults) {
        accessor.setKeyPhrasesExtractionResults(analyzeBatchTasksResult, keyPhraseExtractionResults);
    }
}
