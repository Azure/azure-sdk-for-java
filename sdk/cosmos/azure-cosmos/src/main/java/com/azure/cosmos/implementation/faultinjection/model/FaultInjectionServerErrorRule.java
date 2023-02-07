// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.FaultInjectionServerErrorResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class FaultInjectionServerErrorRule implements IFaultInjectionRuleInternal {
    private final FaultInjectionServerErrorResult result;
    private final FaultInjectionConditionInternal condition;
    private final String id;
    private final Duration duration;
    private final Instant expireTime;
    private final int operationHitLimit;
    private boolean enabled;

    public FaultInjectionServerErrorRule(
        String id,
        FaultInjectionConditionInternal condition,
        FaultInjectionServerErrorResult result,
        Duration duration,
        int operationHitLimit,
        boolean enabled) {
        this.result = result;
        this.condition = condition;
        this.id = id;
        this.duration = duration;
        this.expireTime = this.duration != null ? Instant.now().plusMillis(this.duration.toMillis()) : Instant.MAX;
        this.operationHitLimit = operationHitLimit;
        this.enabled = enabled;
    }

    public FaultInjectionServerErrorResult getResult() {
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
        return this.condition;
    }

    public boolean isApplicable(RxDocumentServiceRequest request) {
        return this.isValid()
            && this.getCondition().matches(request)
            && request.faultInjectionRequestContext.getFaultInjectionRuleApplyCount(this.id) < this.result.getTimes();
    }
}
