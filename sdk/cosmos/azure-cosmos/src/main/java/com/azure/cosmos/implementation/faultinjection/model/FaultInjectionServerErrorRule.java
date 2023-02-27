// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.models.FaultInjectionServerErrorResult;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FaultInjectionServerErrorRule implements IFaultInjectionRuleInternal {
    private final String id;
    private final Instant startTime;
    private final Instant expireTime;
    private final Integer hitLimit;
    private final AtomicLong hitCount;
    private final FaultInjectionConditionInternal condition;
    private final FaultInjectionServerErrorResult result;

    private boolean enabled;

    public FaultInjectionServerErrorRule(
        String id,
        boolean enabled,
        Duration delay,
        Duration duration,
        Integer hitLimit,
        FaultInjectionConditionInternal condition,
        FaultInjectionServerErrorResult result) {
        this.id = id;
        this.enabled = enabled;
        this.hitLimit = hitLimit;
        this.startTime = delay == null ? Instant.now() : Instant.now().plusMillis(delay.toMillis());
        this.expireTime = duration == null ? Instant.MAX : this.startTime.plusMillis(duration.toMillis());
        this.hitCount = new AtomicLong(0);
        this.condition = condition;
        this.result = result;
    }

    public boolean isApplicable(RxDocumentServiceRequest request) {
        if (this.isValid()
            && this.condition.isApplicable(request)
            && this.result.isApplicable(this.id, request)) {

            long hitCount = this.hitCount.incrementAndGet();
            return this.hitLimit == null || hitCount <= this.hitLimit;
        }

        return false;
    }

    public CosmosException getInjectedServerError() {
        return this.result.getInjectedServerError();
    }

    public String getId() {
        return id;
    }

    public FaultInjectionConditionInternal getCondition() {
        return condition;
    }

    public FaultInjectionServerErrorResult getResult() {
        return result;
    }

    @Override
    public boolean isValid() {
        Instant now = Instant.now();
        return this.enabled && now.isAfter(this.startTime) && now.isBefore(this.expireTime);
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public List<URI> getAddresses() {
        return this.condition.getAddresses();
    }
}
