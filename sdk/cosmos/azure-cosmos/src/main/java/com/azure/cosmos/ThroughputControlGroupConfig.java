// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

/**
 * Throughput control group configuration.
 */
public final class ThroughputControlGroupConfig {
    private final String groupName;
    private final Integer targetThroughput;
    private final Double targetThroughputThreshold;
    private final boolean isDefault;
    private final boolean continueOnInitError;

    ThroughputControlGroupConfig(
            String groupName,
            Integer targetThroughput,
            Double targetThroughputThreshold,
            boolean isDefault,
            boolean continueOnInitError) {
       this.groupName = groupName;
       this.targetThroughput = targetThroughput;
       this.targetThroughputThreshold = targetThroughputThreshold;
       this.isDefault = isDefault;
       this.continueOnInitError = continueOnInitError;
    }

    /**
     * Get the throughput control group name.
     *
     * @return the group name.
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * Get throughput control group target throughput.
     * Since we allow either TargetThroughput or TargetThroughputThreshold, this value can be null.
     *
     * By default, it is null.
     *
     * @return the target throughput.
     */
    public Integer getTargetThroughput() {
        return this.targetThroughput;
    }

    /**
     * Get the throughput control group target throughput threshold.
     * Since we allow either TargetThroughput or TargetThroughputThreshold, this value can be null.
     *
     * By default, this value is null.
     *
     * @return the target throughput threshold null or (0, 1].
     */
    public Double getTargetThroughputThreshold() {
        return this.targetThroughputThreshold;
    }

    /**
     * Get whether this throughput control group will be used by default.
     *
     * By default, it is false.
     * If it is true, requests without explicit override of the throughput control group will be routed to this group.
     *
     * @return {@code true} this throughput control group will be used by default unless being override. {@code false} otherwise.
     */
    public boolean isDefault() {
        return this.isDefault;
    }

    /**
     * Get whether request is allowed to continue on original request flow if throughput control controller failed on initialization.
     *
     * By default, it is false.
     * If it is true, requests will continue on original request flow if throughput control controller failed on initialization.
     *
     * @return {@code true} request will continue on original request flow if throughput control controller failed on initialization. {@code false} otherwise.
     */
    public boolean continueOnInitError() {
        return continueOnInitError;
    }
}
