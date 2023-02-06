// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.FaultInjectionCondition;
import com.azure.cosmos.models.FaultInjectionEndpoints;
import com.azure.cosmos.models.FaultInjectionOperationType;
import com.azure.cosmos.models.FaultInjectionRequestProtocol;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionConditionBuilder {

    private FaultInjectionEndpoints endpoints;
    private FaultInjectionOperationType operationType;
    private String region;
    private FaultInjectionRequestProtocol protocol = FaultInjectionRequestProtocol.TCP;

    /**
     * Optional.
     *
     * @param region the region in which the fault injection rule will be applied.
     *
     * @return the builder.
     */
    public FaultInjectionConditionBuilder region(String region) {
        checkArgument(StringUtils.isNotEmpty(region), "Argument 'region' can not be null nor empty");
        this.region = region;

        return this;
    }

    /**
     * Optional.
     *
     * @param operationType the operationType.
     * @return the builder.
     */
    public FaultInjectionConditionBuilder operationType(FaultInjectionOperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    /***
     * Optional.
     * @param protocol the protocol.
     * @return the builder.
     */
    public FaultInjectionConditionBuilder protocol(FaultInjectionRequestProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    /***
     * Optional. Can limit the endpoint down to a certain partition.
     * @param endpoints the fault injection endpoints.
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
            this.protocol,
            this.region,
            this.endpoints);
    }
}
