// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.FaultInjectionConnectionErrorResult;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionConnectionErrorRule implements IFaultInjectionRuleInternal {
    private final String id;
    private final Instant startTime;
    private final Instant expireTime;
    private final AtomicLong hitCount;
    private final List<URI> addresses;
    private final FaultInjectionConnectionErrorResult result;

    private boolean enabled;

    public FaultInjectionConnectionErrorRule(
        String id,
        boolean enabled,
        Duration delay,
        Duration duration,
        List<URI> addresses,
        FaultInjectionConnectionErrorResult result) {

        checkArgument(StringUtils.isNotEmpty(id), "Argument 'id' cannot be null nor empty");
        checkNotNull(addresses, "Argument 'addresses' can not be null");
        checkNotNull(result, "Argument 'result' can not be null");

        this.id = id;
        this.enabled = enabled;
        this.startTime = delay == null ? Instant.now() : Instant.now().plusMillis(delay.toMillis());
        this.expireTime = duration == null ? Instant.MAX : this.startTime.plusMillis(duration.toMillis());
        this.addresses = addresses;
        this.result = result;
        this.hitCount = new AtomicLong(0);
    }

    @Override
    public String getId() {
        return id;
    }

    public FaultInjectionConnectionErrorResult getResult() {
        return result;
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public List<URI> getAddresses() {
        return this.addresses;
    }

    @Override
    public boolean isValid() {
        Instant now = Instant.now();
        return this.enabled && now.isAfter(this.startTime) && now.isBefore(this.expireTime);
    }
}
