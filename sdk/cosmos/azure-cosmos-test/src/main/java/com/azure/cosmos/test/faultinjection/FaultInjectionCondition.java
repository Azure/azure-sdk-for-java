// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

/***
 * Fault injection condition.
 * A fault injection rule will not be applicable if the condition mismatches.
 */
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
    public FaultInjectionEndpoints getEndpoints() {
        return this.endpoints;
    }

    /***
     * Get the operation type.
     *
     * @return the {@link FaultInjectionOperationType}.
     */
    public FaultInjectionOperationType getOperationType() {
        return this.operationType;
    }

    /***
     * Get the connection type.
     *
     * @return the {@link FaultInjectionConnectionType}.
     */
    public FaultInjectionConnectionType getConnectionType() {
        return this.connectionType;
    }

    /***
     * Get the configured region.
     *
     * @return the region.
     */
    public String getRegion() {
        return this.region;
    }

    @Override
    public String toString() {
        return String.format(
            "FaultInjectionCondition{ endpoints=%s, operationType=%s, connectionType=%s, region=%s }",
            this.endpoints,
            this.operationType,
            this.connectionType,
            this.region);
    }
}
