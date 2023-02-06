// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultInjection;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.FaultInjectionServerErrorResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class FaultInjectionServerErrorRule implements IFaultInjectionRuleInternal{
    private final OperationType operationType;
    private final FaultInjectionServerErrorResult result;
    private final String ruleId;
    private boolean enabled;
    private final List<Uri> addresses;
    private final Duration duration;
    private final Instant expireTime;
    private final int requestHitLimit;

    public FaultInjectionServerErrorRule(
        String ruleId,
        OperationType operationType,
        FaultInjectionServerErrorResult result,
        boolean enabled,
        Duration duration,
        int requestHitLimit,
        List<Uri> addresses) {
        this.operationType = operationType;
        this.result = result;
        this.ruleId = ruleId;
        this.enabled = enabled;
        this.addresses = addresses;
        this.duration = duration;
        this.expireTime = this.duration != null ? Instant.now().plusMillis(this.duration.toMillis()) : Instant.MAX;
        this.requestHitLimit = requestHitLimit;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public FaultInjectionServerErrorResult getResult() {
        return result;
    }

    public String getRuleId() {
        return ruleId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<Uri> getAddresses() {
        return addresses;
    }

    public Duration getDuration() {
        return duration;
    }

    public int getRequestHitLimit() {
        return requestHitLimit;
    }

    @Override
    public boolean isValid() {
        return this.enabled && Instant.now().isBefore(this.expireTime);
    }

    @Override
    public List<String> getEndpointAddresses() {
        return this.addresses.stream().map(uri -> uri.getURI().getAuthority().toString()).collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return this.getRuleId();
    }
}
