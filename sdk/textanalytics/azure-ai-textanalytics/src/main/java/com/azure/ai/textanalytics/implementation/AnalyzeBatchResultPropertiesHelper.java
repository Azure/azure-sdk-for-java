// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeBatchResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.util.IterableStream;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeBatchResult} instance.
 */
public final class AnalyzeBatchResultPropertiesHelper {
    private static AnalyzeTasksResultAccessor accessor;

    private AnalyzeBatchResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeBatchResult} instance.
     */
    public interface AnalyzeTasksResultAccessor {
        void setStatistics(AnalyzeBatchResult analyzeBatchResult,
            TextDocumentBatchStatistics operationStatistics);
        void setEntitiesRecognitionResults(AnalyzeBatchResult analyzeBatchResult,
            IterableStream<RecognizeEntitiesResultCollection> entitiesRecognitionResults);
        void setPiiEntitiesRecognitionResults(AnalyzeBatchResult analyzeBatchResult,
            IterableStream<RecognizePiiEntitiesResultCollection> piiEntitiesRecognitionResults);
        void setKeyPhrasesExtractionResults(AnalyzeBatchResult analyzeBatchResult,
            IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesExtractionResults);
    }

    /**
     * The method called from {@link AnalyzeBatchResult} to set it's accessor.
     *
     * @param analyzeTasksResultAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeTasksResultAccessor analyzeTasksResultAccessor) {
        accessor = analyzeTasksResultAccessor;
    }

    public static void setStatistics(AnalyzeBatchResult analyzeBatchResult,
        TextDocumentBatchStatistics operationStatistics) {
        accessor.setStatistics(analyzeBatchResult, operationStatistics);
    }

    public static void setEntityRecognitionResults(AnalyzeBatchResult analyzeBatchResult,
        IterableStream<RecognizeEntitiesResultCollection> entityRecognitionResults) {
        accessor.setEntitiesRecognitionResults(analyzeBatchResult, entityRecognitionResults);
    }

    public static void setPiiEntityRecognitionResults(AnalyzeBatchResult analyzeBatchResult,
        IterableStream<RecognizePiiEntitiesResultCollection> piiEntityRecognitionResults) {
        accessor.setPiiEntitiesRecognitionResults(analyzeBatchResult, piiEntityRecognitionResults);
    }

    public static void setKeyPhraseExtractionResults(AnalyzeBatchResult analyzeBatchResult,
        IterableStream<ExtractKeyPhrasesResultCollection> keyPhraseExtractionResults) {
        accessor.setKeyPhrasesExtractionResults(analyzeBatchResult, keyPhraseExtractionResults);
    }
}
