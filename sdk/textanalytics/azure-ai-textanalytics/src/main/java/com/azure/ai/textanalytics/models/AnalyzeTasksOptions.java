// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.util.List;

import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;

/**
 * The {@link AnalyzeTasksOptions} model.
 */
@Fluent
public final class AnalyzeTasksOptions extends TextAnalyticsRequestOptions {
    private String displayName;
    private Integer skip;
    private Integer top;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;
    private List<EntitiesTask> entitiesRecognitionTasks;
    private List<PiiTask> piiEntitiesRecognitionTasks;
    private List<KeyPhrasesTask> keyPhrasesExtractionTasks;

    /**
     * Set the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link AnalyzeTasksOptions} object itself.
     */
    @Override
    public AnalyzeTasksOptions setModelVersion(String modelVersion) {
        super.setModelVersion(modelVersion);
        return this;
    }

    /**
     * Set the value of {@code includeStatistics}.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return the {@link AnalyzeTasksOptions} object itself.
     */
    @Override
    public AnalyzeTasksOptions setIncludeStatistics(boolean includeStatistics) {
        super.setIncludeStatistics(includeStatistics);
        return this;
    }

    /**
     * Get the custom name for the analyze tasks.
     *
     * @return the name of analyze tasks.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the custom name for the analyze tasks.
     *
     * @param displayName the display name of analyze tasks.
     *
     * @return the {@link AnalyzeTasksOptions} object itself.
     */
    public AnalyzeTasksOptions setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the top value, which is the number of healthcare document result per page.
     *
     * @return the top value, which is the number of healthcare document result per page.
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Set the top value, which is the healthcare document results per page.
     *
     * @param top the top value, which is the healthcare document results per page.
     *
     * @return the {@link AnalyzeTasksOptions} object itself.
     */
    public AnalyzeTasksOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     * Get the skip value, which is the number of healthcare document results to skip.
     *
     * @return the skip value, which is the number of healthcare document results to skip.
     */
    public Integer getSkip() {
        return skip;
    }

    /**
     * Set the skip value, which is the number of healthcare document results to skip.
     *
     * @param skip the skip value, which is the number of healthcare document results to skip.
     *
     * @return the {@link AnalyzeTasksOptions} object itself.
     */
    public AnalyzeTasksOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Get the duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @return the {@code pollInterval} value.
     */
    public Duration getPollInterval() {
        return pollInterval;
    }

    /**
     * Set the duration between each poll for the operation status. If none is specified, a default of
     * 5 seconds is used.
     *
     * @param pollInterval the duration to specify between each poll for the operation status.
     *
     * @return the updated {@code AnalyzeTasksOptions} value.
     */
    public AnalyzeTasksOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        return this;
    }

    /**
     * Get the list of {@link EntitiesTask} to be analyzed.
     *
     * @return the entitiesRecognitionTasks value.
     */
    public List<EntitiesTask> getEntitiesRecognitionTasks() {
        return this.entitiesRecognitionTasks;
    }

    /**
     * Set the list of {@link EntitiesTask} to be analyzed.
     *
     * @param entitiesRecognitionTasks the list of {@link EntitiesTask} to be analyzed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public AnalyzeTasksOptions setEntitiesRecognitionTasks(List<EntitiesTask> entitiesRecognitionTasks) {
        this.entitiesRecognitionTasks = entitiesRecognitionTasks;
        return this;
    }

    /**
     * Get the list of {@link PiiTask} to be analyzed.
     *
     * @return the list of {@link PiiTask} to be analyzed.
     */
    public List<PiiTask> getPiiEntitiesRecognitionTasks() {
        return this.piiEntitiesRecognitionTasks;
    }

    /**
     * Set the list of {@link PiiTask} to be analyzed.
     *
     * @param piiEntitiesRecognitionTasks the list of {@link PiiTask} to be analyzed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public AnalyzeTasksOptions setPiiEntitiesRecognitionTasks(List<PiiTask> piiEntitiesRecognitionTasks) {
        this.piiEntitiesRecognitionTasks = piiEntitiesRecognitionTasks;
        return this;
    }

    /**
     * Get the list of {@link KeyPhrasesTask} to be analyzed.
     *
     * @return the list of {@link KeyPhrasesTask} to be analyzed.
     */
    public List<KeyPhrasesTask> getKeyPhrasesExtractionTasks() {
        return this.keyPhrasesExtractionTasks;
    }

    /**
     * Set the list of {@link KeyPhrasesTask} to be analyzed.
     *
     * @param keyPhrasesExtractionTasks the list of {@link KeyPhrasesTask} to be analyzed.
     *
     * @return the AnalyzeTasksOptions object itself.
     */
    public AnalyzeTasksOptions setKeyPhrasesExtractionTasks(List<KeyPhrasesTask> keyPhrasesExtractionTasks) {
        this.keyPhrasesExtractionTasks = keyPhrasesExtractionTasks;
        return this;
    }
}
