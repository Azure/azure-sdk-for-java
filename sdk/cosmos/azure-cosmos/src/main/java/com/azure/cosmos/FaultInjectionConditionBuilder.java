// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.FaultInjectionCondition;
import com.azure.cosmos.models.FaultInjectionEndpoints;
import com.azure.cosmos.models.FaultInjectionOperationType;
import com.azure.cosmos.models.FaultInjectionConnectionType;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionConditionBuilder {

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

    public FaultInjectionCondition build() {
        return new FaultInjectionCondition(
            this.operationType,
            this.connectionType,
            this.region,
            this.endpoints);
    }
}
