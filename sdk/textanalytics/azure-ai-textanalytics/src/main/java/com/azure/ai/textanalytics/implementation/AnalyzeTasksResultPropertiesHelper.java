// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeTasksResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeTasksResult} instance.
 */
public final class AnalyzeTasksResultPropertiesHelper {
    private static AnalyzeTasksResultAccessor accessor;

    private AnalyzeTasksResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeTasksResult} instance.
     */
    public interface AnalyzeTasksResultAccessor {
        void setErrors(AnalyzeTasksResult analyzeTasksResult, List<TextAnalyticsError> errors);
        void setStatistics(AnalyzeTasksResult analyzeTasksResult, TextDocumentBatchStatistics statistics);
        void setCompleted(AnalyzeTasksResult analyzeTasksResult, int completed);
        void setFailed(AnalyzeTasksResult analyzeTasksResult, int failed);
        void setInProgress(AnalyzeTasksResult analyzeTasksResult, int inProgress);
        void setTotal(AnalyzeTasksResult analyzeTasksResult, int total);
        void setEntityRecognitionTasks(AnalyzeTasksResult analyzeTasksResult,
            List<RecognizeEntitiesResultCollection> entityRecognitionTasks);
        void setEntityRecognitionPiiTasks(AnalyzeTasksResult analyzeTasksResult,
            List<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks);
        void setKeyPhraseExtractionTasks(AnalyzeTasksResult analyzeTasksResult,
            List<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks);
    }

    /**
     * The method called from {@link AnalyzeTasksResult} to set it's accessor.
     *
     * @param analyzeTasksResultAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeTasksResultAccessor analyzeTasksResultAccessor) {
        accessor = analyzeTasksResultAccessor;
    }

    public static void setErrors(AnalyzeTasksResult analyzeTasksResult, List<TextAnalyticsError> errors) {
        accessor.setErrors(analyzeTasksResult, errors);
    }

    public static void setStatistics(AnalyzeTasksResult analyzeTasksResult, TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(analyzeTasksResult, statistics);
    }

    public static void setCompleted(AnalyzeTasksResult analyzeTasksResult, int completed) {
        accessor.setCompleted(analyzeTasksResult, completed);
    }

    public static void setFailed(AnalyzeTasksResult analyzeTasksResult, int failed) {
        accessor.setFailed(analyzeTasksResult, failed);
    }

    public static void setInProgress(AnalyzeTasksResult analyzeTasksResult, int inProgress) {
        accessor.setInProgress(analyzeTasksResult, inProgress);
    }

    public static void setTotal(AnalyzeTasksResult analyzeTasksResult, int total) {
        accessor.setTotal(analyzeTasksResult, total);
    }

    public static void setEntityRecognitionTasks(AnalyzeTasksResult analyzeTasksResult,
        List<RecognizeEntitiesResultCollection> entityRecognitionTasks) {
        accessor.setEntityRecognitionTasks(analyzeTasksResult, entityRecognitionTasks);
    }

    public static void setEntityRecognitionPiiTasks(AnalyzeTasksResult analyzeTasksResult,
        List<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks) {
        accessor.setEntityRecognitionPiiTasks(analyzeTasksResult, entityRecognitionPiiTasks);
    }

    public static void setKeyPhraseExtractionTasks(AnalyzeTasksResult analyzeTasksResult,
        List<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks) {
        accessor.setKeyPhraseExtractionTasks(analyzeTasksResult, keyPhraseExtractionTasks);
    }
}
