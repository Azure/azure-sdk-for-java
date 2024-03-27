// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Fault injection condition builder.
 */
public final class FaultInjectionConditionBuilder {

    private FaultInjectionEndpoints endpoints;
    private FaultInjectionOperationType operationType;
    private String region;
    private FaultInjectionConnectionType connectionType = FaultInjectionConnectionType.DIRECT;

    /***
     * Set the region of the rule. The rule will not be applicable if the request is targeting a different region.
     *
     * By default, it is null which means the rule can be applied in any region.
     *
     * @param region the region of the rule.
     * @return the builder.
     */
    public FaultInjectionConditionBuilder region(String region) {
        checkArgument(StringUtils.isNotEmpty(region), "Argument 'region' can not be null nor empty");
        this.region = region;

        return this;
    }

    /***
     * Set the operation type of the rule. The rule will not be applicable if the request is targeting a different operation type.
     *
     * By default, it is null which means the rule can be applied to any operation type.
     *
     * @param operationType the operation type.
     * @return the builder.
     */
    public FaultInjectionConditionBuilder operationType(FaultInjectionOperationType operationType) {
        checkNotNull(operationType, "Argument 'operationType' can not be null");
        this.operationType = operationType;
        return this;
    }

    /***
     * Set the connection type of the rule. The rule will not be applicable if the request is targeting a different connection type.
     *
     * By default, it is direct.
     *
     * @param connectionType the connection type.
     * @return the builder.
     */
    public FaultInjectionConditionBuilder connectionType(FaultInjectionConnectionType connectionType) {
        checkNotNull(connectionType, "Argument 'connectionType' can not be null");
        this.connectionType = connectionType;
        return this;
    }

    /***
     * Set the physical endpoints of the rule. Only applicable in direct connection type.
     *
     * @param endpoints the physical endpoints.
     * @return the builder.
     */
    public FaultInjectionConditionBuilder endpoints(FaultInjectionEndpoints endpoints) {
        checkNotNull(endpoints, "Argument 'endpoints' can not be null");
        this.endpoints = endpoints;
        return this;
    }

    /***
     * Create new fault injection condition.
     *
     * @return the {@link FaultInjectionCondition}.
     */
    public FaultInjectionCondition build() {
        return new FaultInjectionCondition(
            this.operationType,
            this.connectionType,
            this.region,
            this.endpoints);
    }
}
