// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

public class FaultInjectionCondition {
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

    public FaultInjectionEndpoints getEndpoints() {
        return this.endpoints;
    }

    public FaultInjectionOperationType getOperationType() {
        return this.operationType;
    }

    public FaultInjectionConnectionType getConnectionType() {
        return this.connectionType;
    }

    public String getRegion() {
        return this.region;
    }
}
