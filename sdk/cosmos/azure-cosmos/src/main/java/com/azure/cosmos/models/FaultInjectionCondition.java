// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

public class FaultInjectionCondition {
    private final FaultInjectionEndpoints endpoints;
    private final FaultInjectionOperationType operationType;
    private final FaultInjectionConnectionType connectionType;
    private final String region;

    public FaultInjectionCondition(
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

    int getEffectiveReplicaCount() {
        return this.operationType == FaultInjectionOperationType.CREATE ? 1 : this.endpoints.getReplicaCount();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.FaultInjectionConditionHelper.setFaultInjectionConditionAccessor(
            new ImplementationBridgeHelpers.FaultInjectionConditionHelper.FaultInjectionConditionAccessor() {
                @Override
                public int getEffectiveReplicaCount(FaultInjectionCondition condition) {
                    return condition.getEffectiveReplicaCount();
                }
            });
    }

    static { initialize(); }
}
