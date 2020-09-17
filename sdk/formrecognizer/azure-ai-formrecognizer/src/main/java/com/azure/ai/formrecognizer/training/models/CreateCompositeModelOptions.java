// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

@Fluent
public final class CreateCompositeModelOptions {
    private String displayName;

    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);
    private Duration pollInterval = DEFAULT_POLL_INTERVAL;

    public String getDisplayName() {
        return displayName;
    }

    public CreateCompositeModelOptions setDisplayName(final String displayName) {
        this.displayName = displayName;
        return this;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public CreateCompositeModelOptions setPollInterval(final Duration pollInterval) {
        this.pollInterval = pollInterval;
        return this;
    }
}
