// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/**
 * The configurable options to pass when creating a composed model.
 */
@Fluent
public final class CreateComposeModelOptions {
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);
    private String modelDisplayName;
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;

    /**
     * Get the optional model name defined by the user. (max length: 1024).
     *
     * @return the modelDisplayName.
     */
    public String getModelDisplayName() {
        return modelDisplayName;
    }

    /**
     * Set the optional model name defined by the user. (max length: 1024).
     *
     * @param modelDisplayName the user defined model display name to set.
     *
     * @return the updated {@code CreateComposeModelOptions} value.
     */
    public CreateComposeModelOptions setModelDisplayName(final String modelDisplayName) {
        this.modelDisplayName = modelDisplayName;
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
     * @return the updated {@code CreateComposeModelOptions} value.
     */
    public CreateComposeModelOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval == null ? DEFAULT_POLL_INTERVAL : pollInterval;
        return this;
    }
}
