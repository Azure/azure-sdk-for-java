// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.faultinjection;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.faultinjection.FaultInjectionRequestArgs;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionServerErrorRule implements IFaultInjectionRuleInternal {
    private final String id;
    private final Instant startTime;
    private final Instant expireTime;
    private final Integer hitLimit;
    private final AtomicLong hitCount;
    private final Map<String, Long> hitCountDetails;
    private final AtomicLong evaluationCount;
    private final FaultInjectionConnectionType connectionType;
    private final FaultInjectionConditionInternal condition;
    private final FaultInjectionServerErrorResultInternal result;

    private static final Random random = new Random();

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
        this.hitCountDetails = new ConcurrentHashMap<>();
        this.evaluationCount = new AtomicLong(0);
        this.condition = condition;
        this.result = result;
        this.connectionType = connectionType;
    }

    public boolean isApplicable(FaultInjectionRequestArgs requestArgs) {
        if (!this.isValid()) {
            requestArgs.getServiceRequest().faultInjectionRequestContext.recordFaultInjectionRuleEvaluation(
                requestArgs.getTransportRequestId(),
                String.format(
                    "%s[Disable or Duration reached. StartTime: %s, ExpireTime: %s]",
                    this.id,
                    this.startTime,
                    this.expireTime)
            );

            return false;
        }

        // the failure reason will be populated during condition evaluation
        if (!this.condition.isApplicable(this.id, requestArgs)) {
            return false;
        }

        if (!this.result.isApplicable(this.id, requestArgs.getServiceRequest())) {
            requestArgs.getServiceRequest().faultInjectionRequestContext.recordFaultInjectionRuleEvaluation(
                requestArgs.getTransportRequestId(),
                this.id + "[Per operation apply limit reached]"
            );
            return false;
        }

        if (this.result.getServerErrorType() == FaultInjectionServerErrorType.STALED_ADDRESSES_SERVER_GONE
            && requestArgs.getServiceRequest().faultInjectionRequestContext.getAddressForceRefreshed()) {
            requestArgs.getServiceRequest().faultInjectionRequestContext.recordFaultInjectionRuleEvaluation(
                requestArgs.getTransportRequestId(),
                "Address force refresh happened, STALED_ADDRESSES error is cleared."
            );

            return false;
        }

        long evaluationCount = this.evaluationCount.incrementAndGet();
        boolean withinHitLimit = this.hitLimit == null || evaluationCount <= this.hitLimit;
        if (!withinHitLimit) {
            requestArgs.getServiceRequest().faultInjectionRequestContext.recordFaultInjectionRuleEvaluation(
                requestArgs.getTransportRequestId(),
                String.format("%s [Hit Limit reached. Configured hitLimit %d, evaluationCount %d]", this.id, this.hitLimit, evaluationCount)
            );
            return false;
        } else if (random.nextDouble() > this.result.getInjectionRate()) {
            requestArgs.getServiceRequest().faultInjectionRequestContext.recordFaultInjectionRuleEvaluation(
                requestArgs.getTransportRequestId(),
                String.format("%s Injection Rate: Rule will not be applied. Configured injectionRate %.2f%%", this.id, this.result.getInjectionRate() * 100)
            );
            return false;
        } else {
            this.hitCount.incrementAndGet();

            // track hit count details, key will be operationType-resourceType
            String name =
                requestArgs.getServiceRequest().getOperationType().toString() + "-" + requestArgs.getServiceRequest().getResourceType().toString();
            this.hitCountDetails.compute(name, (key, count) -> {
                if (count == null) {
                    count = 0L;
                }

                count++;
                return count;
            });

            return true;
        }
    }

    public CosmosException getInjectedServerError(RxDocumentServiceRequest request) {
        return this.result.getInjectedServerError(request);
    }

    public String getId() {
        return id;
    }

    @Override
    public long getHitCount() {
        return this.hitCount.get();
    }

    @Override
    public Map<String, Long> getHitCountDetails() {
        return this.hitCountDetails;
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
        return this.enabled
            && (now.equals(this.startTime) ||  now.isAfter(this.startTime))
            && (now.equals(this.expireTime) || now.isBefore(this.expireTime));
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
