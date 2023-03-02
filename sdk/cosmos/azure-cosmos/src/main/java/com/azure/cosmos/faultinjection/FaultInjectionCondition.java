// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.util.Beta;

/***
 * Fault injection condition.
 * A fault injection rule will not be applicable if the condition mismatches.
 */
@Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class FaultInjectionCondition {
    private final FaultInjectionEndpoints endpoints;
    private final FaultInjectionOperationType operationType;
    private final FaultInjectionConnectionType connectionType;
    private final String region;

    FaultInjectionCondition(
        FaultInjectionOperationType operationType,
        FaultInjectionConnectionType connectionType,
        String region,
        FaultInjectionEndpoints endpoints) {
        this.operationType = operationType;
        this.connectionType = connectionType;
        this.region = region;
        this.endpoints = endpoints;
    }

    /***
     * Get the fault injection endpoints.
     *
     * @return the {@link FaultInjectionEndpoints}.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public FaultInjectionEndpoints getEndpoints() {
        return this.endpoints;
    }

    /***
     * Get the operation type.
     *
     * @return the {@link FaultInjectionOperationType}.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public FaultInjectionOperationType getOperationType() {
        return this.operationType;
    }

    /***
     * Get the connection type.
     *
     * @return the {@link FaultInjectionConnectionType}.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public FaultInjectionConnectionType getConnectionType() {
        return this.connectionType;
    }

    /***
     * Get the configured region.
     *
     * @return the region.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getRegion() {
        return this.region;
    }

    @Override
    public String toString() {
        return "FaultInjectionCondition{" +
            "endpoints=" + endpoints +
            ", operationType=" + operationType +
            ", connectionType=" + connectionType +
            ", region='" + region + '\'' +
            '}';
    }
}
