// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

import static com.azure.ai.textanalytics.implementation.Utility.DEFAULT_POLL_INTERVAL;

/**
 * The {@link RecognizeHealthcareEntityOptions} model.
 */
@Fluent
public final class RecognizeHealthcareEntityOptions extends TextAnalyticsRequestOptions {
    private Integer skip;
    private Integer top;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;

    /**
     * Set the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link RecognizeHealthcareEntityOptions} object itself.
     */
    @Override
    public RecognizeHealthcareEntityOptions setModelVersion(String modelVersion) {
        super.setModelVersion(modelVersion);
        return this;
    }

    /**
     * Set the value of {@code includeStatistics}.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return the {@link RecognizeHealthcareEntityOptions} object itself.
     */
    @Override
    public RecognizeHealthcareEntityOptions setIncludeStatistics(boolean includeStatistics) {
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
     * @return the {@link RecognizeHealthcareEntityOptions} object itself.
     */
    public RecognizeHealthcareEntityOptions setTop(Integer top) {
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
     * @return the {@link RecognizeHealthcareEntityOptions} object itself.
     */
    public RecognizeHealthcareEntityOptions setSkip(Integer skip) {
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
     * @return the updated {@code RecognizeHealthcareEntityOptions} value.
     */
    public RecognizeHealthcareEntityOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        return this;
    }
}
