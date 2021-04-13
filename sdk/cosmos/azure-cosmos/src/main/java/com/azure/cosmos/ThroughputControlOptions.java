// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.util.Beta;

/**
 * Encapsulates throughput control options.
 */
@Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class ThroughputControlOptions {
    private String groupName;
    private boolean fallbackOnInitError;

    /**
     * Creates a new instance of the ThroughputControlOptions class.
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlOptions() {
        this.fallbackOnInitError = false;
    }

    /**
     * Get throughput control group for the request.
     *
     * @return The throughput control group name.
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getGroupName() {
        return groupName;
    }

    /**
     * Set the throughput control group for the request.
     *
     * @param groupName The throughput control group name.
     *
     * @return the {@link ThroughputControlOptions}.
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlOptions setGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    /**
     * Get whether the request will fail if throughput control store failed on initialization.
     *
     * By default, it is false. Which means request will fail if throughput control store failed on initialization.
     * @return {@code true} request will fallback on to original request flow. {@code false} request will fail.
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isFallbackOnInitError() {
        return fallbackOnInitError;
    }

    /**
     * Set whether the request will fail if throughput control controller failed on initialization.
     *
     * @return the {@link ThroughputControlOptions}.
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlOptions setFallbackOnInitError(boolean fallbackOnInitError) {
        this.fallbackOnInitError = fallbackOnInitError;
        return this;
    }
}
