// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionServerErrorRule implements IFaultInjectionRuleInternal {
    private final String id;
    private final Instant startTime;
    private final Instant expireTime;
    private final Integer hitLimit;
    private final AtomicLong hitCount;
    private final FaultInjectionConnectionType connectionType;
    private final FaultInjectionConditionInternal condition;
    private final FaultInjectionServerErrorResultInternal result;

    private boolean enabled;

    public FaultInjectionServerErrorRule(
        String id,
        boolean enabled,
        Duration delay,
        Duration duration,
        Integer hitLimit,
        FaultInjectionConnectionType connectionType,
        FaultInjectionConditionInternal condition,
        FaultInjectionServerErrorResultInternal result) {

        checkArgument(StringUtils.isNotEmpty(id), "Argument 'id' cannot be null nor empty");
        checkNotNull(condition, "Argument 'condition' can not be null");
        checkNotNull(result, "Argument 'result' can not be null");
        checkNotNull(connectionType, "Argument 'connectionType' can not be null");

        this.id = id;
        this.enabled = enabled;
        this.hitLimit = hitLimit;
        this.startTime = delay == null ? Instant.now() : Instant.now().plusMillis(delay.toMillis());
        this.expireTime = duration == null ? Instant.MAX : this.startTime.plusMillis(duration.toMillis());
        this.hitCount = new AtomicLong(0);
        this.condition = condition;
        this.result = result;
        this.connectionType = connectionType;
    }

    public boolean isApplicable(RntbdRequestArgs requestArgs) {
        if (this.isValid()
            && this.condition.isApplicable(requestArgs)
            && this.result.isApplicable(this.id, requestArgs.serviceRequest())) {

            long hitCount = this.hitCount.incrementAndGet();
            return this.hitLimit == null || hitCount <= this.hitLimit;
        }

        return false;
    }

    public CosmosException getInjectedServerError(RxDocumentServiceRequest request) {
        return this.result.getInjectedServerError(request);
    }

    public String getId() {
        return id;
    }

    @Override
    public long getHitCount() {
        if (this.hitLimit == null) {
            return this.hitCount.get();
        }

        return Math.min(this.hitLimit, this.hitCount.get());
    }

    @Override
    public FaultInjectionConnectionType getConnectionType() {
        return this.connectionType;
    }

    public FaultInjectionConditionInternal getCondition() {
        return condition;
    }

    public FaultInjectionServerErrorResultInternal getResult() {
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

    @Override
    public List<URI> getRegionEndpoints() {
        return this.condition.getRegionEndpoints();
    }
}
