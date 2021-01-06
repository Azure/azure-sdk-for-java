// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeBatchResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;
import com.azure.core.util.IterableStream;

/**
 * The AnalyzeTasksResult model.
 */
public final class AnalyzeBatchResult {
    private TextDocumentBatchStatistics statistics;
    private IterableStream<TextAnalyticsError> taskErrors;
    private IterableStream<RecognizeEntitiesResultCollection> categorizedEntitiesRecognitionTasksResult;
    private IterableStream<RecognizePiiEntitiesResultCollection> piiEntitiesRecognitionTasksResult;
    private IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesExtractionTasksResult;

    static {
        AnalyzeBatchResultPropertiesHelper.setAccessor(
            new AnalyzeBatchResultPropertiesHelper.AnalyzeTasksResultAccessor() {
                @Override
                public void setStatistics(AnalyzeBatchResult analyzeBatchResult,
                    TextDocumentBatchStatistics operationStatistics) {
                    analyzeBatchResult.setStatistics(operationStatistics);
                }

                @Override
                public void setErrors(AnalyzeBatchResult analyzeBatchResult,
                    IterableStream<TextAnalyticsError> taskErrors) {
                    analyzeBatchResult.setErrors(taskErrors);
                }

                @Override
                public void setCategorizedEntitiesRecognitionTasksResult(AnalyzeBatchResult analyzeBatchResult,
                    IterableStream<RecognizeEntitiesResultCollection> categorizedEntitiesRecognitionTasksResult) {
                    analyzeBatchResult.setCategorizedEntitiesRecognitionTasksResult(
                        categorizedEntitiesRecognitionTasksResult);
                }

                @Override
                public void setPiiEntitiesRecognitionTasksResult(AnalyzeBatchResult analyzeBatchResult,
                    IterableStream<RecognizePiiEntitiesResultCollection> piiEntitiesRecognitionTasksResult) {
                    analyzeBatchResult.setPiiEntitiesRecognitionTasksResult(piiEntitiesRecognitionTasksResult);
                }

                @Override
                public void setKeyPhrasesExtractionTasksResult(AnalyzeBatchResult analyzeBatchResult,
                    IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesExtractionTasksResult) {
                    analyzeBatchResult.setKeyPhrasesExtractionTasksResult(keyPhrasesExtractionTasksResult);
                }
            });
    }

    /**
     * Get the analyze tasks operation statistics for analyzing multiple tasks.
     *
     * @return {@link TextDocumentBatchStatistics}.
     */
    public TextDocumentBatchStatistics getStatistics() {
        return this.statistics;
    }

    /**
     * Get an {@link IterableStream} of {@link TextAnalyticsError} for analyzing multiple tasks if operation failed.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsError}.
     */
    public IterableStream<TextAnalyticsError> getErrors() {
        return this.taskErrors;
    }

    /**
     * Get the categorizedEntitiesRecognitionTasksResult property: The categorized entities recognition tasks result
     * property.
     *
     * @return the categorizedEntitiesRecognitionTasksResult value.
     */
    public IterableStream<RecognizeEntitiesResultCollection> getCategorizedEntitiesRecognitionTasksResult() {
        return this.categorizedEntitiesRecognitionTasksResult;
    }

    /**
     * Get the piiEntitiesRecognitionTasksResult property: The PII entities recognition tasks result property.
     *
     * @return the piiEntitiesRecognitionTasksResult value.
     */
    public IterableStream<RecognizePiiEntitiesResultCollection> getPiiEntitiesRecognitionTasksResult() {
        return this.piiEntitiesRecognitionTasksResult;
    }

    /**
     * Get the keyPhrasesExtractionTasksResult property: The key phrases extraction tasks result property.
     *
     * @return the keyPhrasesExtractionTasksResult value.
     */
    public IterableStream<ExtractKeyPhrasesResultCollection> getKeyPhrasesExtractionTasksResult() {
        return this.keyPhrasesExtractionTasksResult;
    }

    private void setStatistics(TextDocumentBatchStatistics statistics) {
        this.statistics = statistics;
    }

    private void setErrors(IterableStream<TextAnalyticsError> taskErrors) {
        this.taskErrors = taskErrors;
    }

    private void setCategorizedEntitiesRecognitionTasksResult(
        IterableStream<RecognizeEntitiesResultCollection> categorizedEntitiesRecognitionTasksResult) {
        this.categorizedEntitiesRecognitionTasksResult = categorizedEntitiesRecognitionTasksResult;
    }

    private void setPiiEntitiesRecognitionTasksResult(
        IterableStream<RecognizePiiEntitiesResultCollection> piiEntitiesRecognitionTasksResult) {
        this.piiEntitiesRecognitionTasksResult = piiEntitiesRecognitionTasksResult;
    }

    private void setKeyPhrasesExtractionTasksResult(
        IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesExtractionTasksResult) {
        this.keyPhrasesExtractionTasksResult = keyPhrasesExtractionTasksResult;
    }
}
