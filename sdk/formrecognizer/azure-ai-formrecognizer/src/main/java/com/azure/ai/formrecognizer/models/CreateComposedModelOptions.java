// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/**
 * The configurable options to pass when creating a composed model.
 */
@Fluent
public final class CreateComposedModelOptions {
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);
    private String modelName;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;

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
     * @return the updated {@code CreateComposedModelOptions} value.
     */
    public CreateComposedModelOptions setModelName(final String modelName) {
        this.modelName = modelName;
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
     * @return the updated {@code CreateComposedModelOptions} value.
     */
    public CreateComposedModelOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        return this;
    }
}
