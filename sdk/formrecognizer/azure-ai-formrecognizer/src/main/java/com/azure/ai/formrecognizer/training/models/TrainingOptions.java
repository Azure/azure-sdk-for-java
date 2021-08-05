// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

import static com.azure.ai.formrecognizer.implementation.Utility.DEFAULT_POLL_INTERVAL;

/**
 * Options that may be passed when using training APIs on Form Training client.
 */
@Fluent
public final class TrainingOptions {
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;
    private TrainingFileFilter trainingFileFilter;
    private String modelName;

    /**
     * Get the filter to apply to the documents in the source path for training.
     *
     * @return the filter to apply to the documents in the source path for training.
     */
    public TrainingFileFilter getTrainingFileFilter() {
        return trainingFileFilter;
    }

    /**
     * Set the filter to apply to the documents in the source path for training.
     *
     * @param trainingFileFilter the {@link TrainingFileFilter filter} to apply to the documents
     * in the source path for training.
     *
     * @return the updated {@code TrainingOptions} value.
     */
    public TrainingOptions setTrainingFileFilter(final TrainingFileFilter trainingFileFilter) {
        this.trainingFileFilter = trainingFileFilter;
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
     * @return the updated {@code TrainingOptions} value.
     */
    public TrainingOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        return this;
    }

    /**
     * Get the optional model name defined by the user.
     *
     * @return the modelName.
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Set the optional model name defined by the user.
     *
     * @param modelName the user defined model name to set.
     *
     * @return the updated {@code TrainingOptions} value.
     */
    public TrainingOptions setModelName(final String modelName) {
        this.modelName = modelName;
        return this;
    }
}
