// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.FaultInjectionConnectionErrorResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class FaultInjectionConnectionErrorRule implements IFaultInjectionRuleInternal {
    private final FaultInjectionConnectionErrorResult result;
    private final FaultInjectionConditionInternal condition;
    private final String id;
    private boolean enabled;
    private Duration duration;
    private final Instant expireTime;

    public FaultInjectionConnectionErrorRule(
        String id,
        FaultInjectionConditionInternal condition,
        FaultInjectionConnectionErrorResult result,
        Duration duration,
        boolean enabled) {
        this.result = result;
        this.condition = condition;
        this.id = id;
        this.enabled = enabled;
        this.duration = duration;
        this.expireTime = this.duration != null ? Instant.now().plusMillis(this.duration.toMillis()) : Instant.MAX;
    }

    public FaultInjectionConnectionErrorResult getResult() {
        return result;
    }
    @Override
    public boolean isValid() {
        return this.enabled && Instant.now().isBefore(this.expireTime);
    }

    @Override
    public List<Uri> getPhysicalAddresses() {
        return this.condition.getPhysicalAddresses();
    }
    @Override
    public String getId() {
        return this.id;
    }

    public FaultInjectionConditionInternal getCondition() {
        return condition;
    }
}
