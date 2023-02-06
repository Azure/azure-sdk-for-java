// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultInjection;

import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.FaultInjectionConnectionErrorResult;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class FaultInjectionConnectionErrorRule implements IFaultInjectionRuleInternal{
    private final FaultInjectionConnectionErrorResult result;
    private final String ruleId;
    private boolean enabled;
    private final List<Uri> addresses;
    private Duration duration;
    private final Instant expireTime;

    public FaultInjectionConnectionErrorRule(
        String ruleId,
        FaultInjectionConnectionErrorResult result,
        boolean enabled,
        Duration duration,
        List<Uri> addresses) {
        this.ruleId = ruleId;
        this.result = result;
        this.enabled = enabled;
        this.duration = duration;
        this.expireTime = this.duration != null ? Instant.now().plusMillis(this.duration.toMillis()) : Instant.MAX;
        this.addresses = addresses;
    }

    public FaultInjectionConnectionErrorResult getResult() {
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

    @Override
    public boolean isValid() {
        return this.enabled && Instant.now().isBefore(this.expireTime);
    }

    @Override
    public List<String> getEndpointAddresses() {
        return this.addresses.stream().map(uri -> uri.getURI().getAuthority()).collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return this.ruleId;
    }
}
