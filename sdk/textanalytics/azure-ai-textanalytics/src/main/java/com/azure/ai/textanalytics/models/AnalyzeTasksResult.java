// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AnalyzeTasksResultPropertiesHelper;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The AnalyzeTasksResult model.
 */
public final class AnalyzeTasksResult extends JobMetadata {

    /*
     * The errors property.
     */
    private List<TextAnalyticsError> errors;

    /*
     * if showStats=true was specified in the request this field will contain
     * information about the request payload.
     */
    private TextDocumentBatchStatistics statistics;

    /*
     * The completed property.
     */
    private int completed;

    /*
     * The failed property.
     */
    private int failed;

    /*
     * The inProgress property.
     */
    private int inProgress;

    /*
     * The total property.
     */
    private int total;

    /*
     * The entityRecognitionTasks property.
     */
    private List<RecognizeEntitiesResultCollection> entityRecognitionTasks;

    /*
     * The entityRecognitionPiiTasks property.
     */
    private List<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks;

    /*
     * The keyPhraseExtractionTasks property.
     */
    private List<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks;

    static {
        AnalyzeTasksResultPropertiesHelper.setAccessor(
            new AnalyzeTasksResultPropertiesHelper.AnalyzeTasksResultAccessor() {
                @Override
                public void setErrors(AnalyzeTasksResult analyzeTasksResult, List<TextAnalyticsError> errors) {
                    analyzeTasksResult.setErrors(errors);
                }

                @Override
                public void setStatistics(AnalyzeTasksResult analyzeTasksResult,
                    TextDocumentBatchStatistics statistics) {
                    analyzeTasksResult.setStatistics(statistics);
                }

                @Override
                public void setCompleted(AnalyzeTasksResult analyzeTasksResult, int completed) {
                    analyzeTasksResult.setCompleted(completed);
                }

                @Override
                public void setFailed(AnalyzeTasksResult analyzeTasksResult, int failed) {
                    analyzeTasksResult.setFailed(failed);
                }

                @Override
                public void setInProgress(AnalyzeTasksResult analyzeTasksResult, int inProgress) {
                    analyzeTasksResult.setInProgress(inProgress);
                }

                @Override
                public void setTotal(AnalyzeTasksResult analyzeTasksResult, int total) {
                    analyzeTasksResult.setTotal(total);
                }

                @Override
                public void setEntityRecognitionTasks(AnalyzeTasksResult analyzeTasksResult,
                    List<RecognizeEntitiesResultCollection> entityRecognitionTasks) {
                    analyzeTasksResult.setEntityRecognitionTasks(entityRecognitionTasks);
                }

                @Override
                public void setEntityRecognitionPiiTasks(AnalyzeTasksResult analyzeTasksResult,
                    List<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks) {
                    analyzeTasksResult.setEntityRecognitionPiiTasks(entityRecognitionPiiTasks);
                }

                @Override
                public void setKeyPhraseExtractionTasks(AnalyzeTasksResult analyzeTasksResult,
                    List<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks) {
                    analyzeTasksResult.setKeyPhraseExtractionTasks(keyPhraseExtractionTasks);
                }
            });
    }

    /**
     * Creates a {@link AnalyzeTasksResult} model that describes analyzed tasks result.
     *
     * @param jobId the job identification.
     * @param createdDateTime the created time of the job.
     * @param lastUpdateDateTime the last updated time of the job.
     * @param status the job status.
     * @param displayName the display name.
     * @param expirationDateTime the expiration time of the job.
     */
    public AnalyzeTasksResult(String jobId, OffsetDateTime createdDateTime, OffsetDateTime lastUpdateDateTime,
        JobState status, String displayName, OffsetDateTime expirationDateTime) {
        super(jobId, createdDateTime, lastUpdateDateTime, status, displayName, expirationDateTime);
    }

    /**
     * Get the errors property: The errors property.
     *
     * @return the errors value.
     */
    public List<TextAnalyticsError> getErrors() {
        return this.errors;
    }

    /**
     * Get the statistics property: if showStats=true was specified in the request this field will contain information
     * about the request payload.
     *
     * @return the statistics value.
     */
    public TextDocumentBatchStatistics getStatistics() {
        return this.statistics;
    }

    /**
     * Get the completed property: The completed property.
     *
     * @return the completed value.
     */
    public int getCompleted() {
        return this.completed;
    }

    /**
     * Get the failed property: The failed property.
     *
     * @return the failed value.
     */
    public int getFailed() {
        return this.failed;
    }

    /**
     * Get the inProgress property: The inProgress property.
     *
     * @return the inProgress value.
     */
    public int getInProgress() {
        return this.inProgress;
    }

    /**
     * Get the total property: The total property.
     *
     * @return the total value.
     */
    public int getTotal() {
        return this.total;
    }

    /**
     * Get the entityRecognitionTasks property: The entityRecognitionTasks property.
     *
     * @return the entityRecognitionTasks value.
     */
    public List<RecognizeEntitiesResultCollection> getEntityRecognitionTasks() {
        return this.entityRecognitionTasks;
    }

    /**
     * Get the entityRecognitionPiiTasks property: The entityRecognitionPiiTasks property.
     *
     * @return the entityRecognitionPiiTasks value.
     */
    public List<RecognizePiiEntitiesResultCollection> getEntityRecognitionPiiTasks() {
        return this.entityRecognitionPiiTasks;
    }

    /**
     * Get the keyPhraseExtractionTasks property: The keyPhraseExtractionTasks property.
     *
     * @return the keyPhraseExtractionTasks value.
     */
    public List<ExtractKeyPhrasesResultCollection> getKeyPhraseExtractionTasks() {
        return this.keyPhraseExtractionTasks;
    }

    private void setErrors(List<TextAnalyticsError> errors) {
        this.errors = errors;
    }

    private void setStatistics(TextDocumentBatchStatistics statistics) {
        this.statistics = statistics;
    }

    private void setCompleted(int completed) {
        this.completed = completed;
    }

    private void setFailed(int failed) {
        this.failed = failed;
    }

    private void setInProgress(int inProgress) {
        this.inProgress = inProgress;
    }

    private void setTotal(int total) {
        this.total = total;
    }

    private void setEntityRecognitionTasks(List<RecognizeEntitiesResultCollection> entityRecognitionTasks) {
        this.entityRecognitionTasks = entityRecognitionTasks;
    }

    private void setEntityRecognitionPiiTasks(List<RecognizePiiEntitiesResultCollection> entityRecognitionPiiTasks) {
        this.entityRecognitionPiiTasks = entityRecognitionPiiTasks;
    }

    private void setKeyPhraseExtractionTasks(List<ExtractKeyPhrasesResultCollection> keyPhraseExtractionTasks) {
        this.keyPhraseExtractionTasks = keyPhraseExtractionTasks;
    }
}
