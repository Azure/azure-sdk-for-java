// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeBatchResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
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
        void setErrors(AnalyzeBatchResult analyzeBatchResult, IterableStream<TextAnalyticsError> taskErrors);
        void setCategorizedEntitiesRecognitionTasksResult(AnalyzeBatchResult analyzeBatchResult,
            IterableStream<RecognizeEntitiesResultCollection> categorizedEntitiesRecognitionTasksResult);
        void setPiiEntitiesRecognitionTasksResult(AnalyzeBatchResult analyzeBatchResult,
            IterableStream<RecognizePiiEntitiesResultCollection> piiEntitiesRecognitionTasksResult);
        void setKeyPhrasesExtractionTasksResult(AnalyzeBatchResult analyzeBatchResult,
            IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesExtractionTasksResult);
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

    public static void setErrors(AnalyzeBatchResult analyzeBatchResult,
        IterableStream<TextAnalyticsError> taskErrors) {
        accessor.setErrors(analyzeBatchResult, taskErrors);
    }

    public static void setCategorizedEntityRecognitionTasksResult(AnalyzeBatchResult analyzeBatchResult,
        IterableStream<RecognizeEntitiesResultCollection> entityRecognitionTasks) {
        accessor.setCategorizedEntitiesRecognitionTasksResult(analyzeBatchResult, entityRecognitionTasks);
    }

    public static void setPiiEntityRecognitionTasksResult(AnalyzeBatchResult analyzeBatchResult,
        IterableStream<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks) {
        accessor.setPiiEntitiesRecognitionTasksResult(analyzeBatchResult, entityRecognitionPiiTasks);
    }

    public static void setKeyPhraseExtractionTasksResult(AnalyzeBatchResult analyzeBatchResult,
        IterableStream<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks) {
        accessor.setKeyPhrasesExtractionTasksResult(analyzeBatchResult, keyPhraseExtractionTasks);
    }
}
