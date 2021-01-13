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
public final class AnalyzeBatchTasksResult {
    private TextDocumentBatchStatistics statistics;
    private IterableStream<RecognizeEntitiesResultCollection> entitiesRecognitionResults;
    private IterableStream<RecognizePiiEntitiesResultCollection> piiEntitiesRecognitionResults;
    private IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesExtractionResults;

    static {
        AnalyzeBatchResultPropertiesHelper.setAccessor(
            new AnalyzeBatchResultPropertiesHelper.AnalyzeTasksResultAccessor() {
                @Override
                public void setStatistics(AnalyzeBatchTasksResult analyzeBatchTasksResult,
                    TextDocumentBatchStatistics operationStatistics) {
                    analyzeBatchTasksResult.setStatistics(operationStatistics);
                }

                @Override
                public void setEntitiesRecognitionResults(AnalyzeBatchTasksResult analyzeBatchTasksResult,
                    IterableStream<RecognizeEntitiesResultCollection> entitiesRecognitionResults) {
                    analyzeBatchTasksResult.setEntitiesRecognitionResults(
                        entitiesRecognitionResults);
                }

                @Override
                public void setPiiEntitiesRecognitionResults(AnalyzeBatchTasksResult analyzeBatchTasksResult,
                    IterableStream<RecognizePiiEntitiesResultCollection> piiEntitiesRecognitionResults) {
                    analyzeBatchTasksResult.setPiiEntitiesRecognitionResults(piiEntitiesRecognitionResults);
                }

                @Override
                public void setKeyPhrasesExtractionResults(AnalyzeBatchTasksResult analyzeBatchTasksResult,
                    IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesExtractionResults) {
                    analyzeBatchTasksResult.setKeyPhrasesExtractionResults(keyPhrasesExtractionResults);
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
     * Get the entitiesRecognitionResults property: The categorized entities recognition tasks result
     * property.
     *
     * @return the entitiesRecognitionResults value.
     */
    public IterableStream<RecognizeEntitiesResultCollection> getEntitiesRecognitionResults() {
        return this.entitiesRecognitionResults;
    }

    /**
     * Get the piiEntitiesRecognitionResults property: The PII entities recognition tasks result property.
     *
     * @return the piiEntitiesRecognitionResults value.
     */
    public IterableStream<RecognizePiiEntitiesResultCollection> getPiiEntitiesRecognitionResults() {
        return this.piiEntitiesRecognitionResults;
    }

    /**
     * Get the keyPhrasesExtractionResults property: The key phrases extraction tasks result property.
     *
     * @return the keyPhrasesExtractionResults value.
     */
    public IterableStream<ExtractKeyPhrasesResultCollection> getKeyPhrasesExtractionResults() {
        return this.keyPhrasesExtractionResults;
    }

    private void setStatistics(TextDocumentBatchStatistics statistics) {
        this.statistics = statistics;
    }

    private void setEntitiesRecognitionResults(
        IterableStream<RecognizeEntitiesResultCollection> entitiesRecognitionResults) {
        this.entitiesRecognitionResults = entitiesRecognitionResults;
    }

    private void setPiiEntitiesRecognitionResults(
        IterableStream<RecognizePiiEntitiesResultCollection> piiEntitiesRecognitionResults) {
        this.piiEntitiesRecognitionResults = piiEntitiesRecognitionResults;
    }

    private void setKeyPhrasesExtractionResults(
        IterableStream<ExtractKeyPhrasesResultCollection> keyPhrasesExtractionResults) {
        this.keyPhrasesExtractionResults = keyPhrasesExtractionResults;
    }
}
