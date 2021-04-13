// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.util.Beta;

/**
 * Encapsulates throughput control options. It contains properties can be customized for each request.
 */
@Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class ThroughputControlOptions {
    private final static boolean DEFAULT_FALLBACK_ON_INIT_ERROR = false;

    private String groupName;
    private boolean fallbackOnInitError;

    /**
     * Creates a new instance of the ThroughputControlOptions class.
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ThroughputControlOptions() {
        this.fallbackOnInitError = DEFAULT_FALLBACK_ON_INIT_ERROR;
    }

    /**
     * Get throughput control group name for the request.
     *
     * @return The throughput control group name.
     */
    @Beta(value = Beta.SinceVersion.V4_15_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getGroupName() {
        return groupName;
    }

    /**
     * Set the throughput control group group for the request.
     * By default, the request will use the default group if default group is specified.
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
     * Get whether the request will fail if throughput control controller failed on initialization.
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
