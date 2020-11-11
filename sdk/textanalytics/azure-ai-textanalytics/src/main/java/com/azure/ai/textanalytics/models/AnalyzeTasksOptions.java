// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;
/**
 * The {@link AnalyzeTasksOptions} model.
 */
@Fluent
public final class AnalyzeTasksOptions extends TextAnalyticsRequestOptions {
    private Integer skip;
    private Integer top;

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
}
