// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.FaultInjectionCondition;
import com.azure.cosmos.models.FaultInjectionRule;
import com.azure.cosmos.models.FaultInjectionServerErrorResult;
import com.azure.cosmos.models.IFaultInjectionResult;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionRuleBuilder {
    private FaultInjectionCondition condition;
    private IFaultInjectionResult result;
    private Duration duration;
    private int requestHitLimit = Integer.MAX_VALUE;
    private boolean enabled = true;

    public FaultInjectionRuleBuilder condition(FaultInjectionCondition condition) {
        checkNotNull(condition, "Argument 'condition' can not be null");
        this.condition = condition;
        return this;
    }

    public FaultInjectionRuleBuilder result(IFaultInjectionResult faultInjectionResult) {
        checkNotNull(faultInjectionResult, "Argument 'faultInjectionResult' can not be null");
        this.result = faultInjectionResult;
        return this;
    }

    public FaultInjectionRuleBuilder duration(Duration duration) {
        checkNotNull(duration, "Argument 'duration' can not be null");
        this.duration = duration;
        return this;
    }

    public FaultInjectionRuleBuilder requestHitLimit(int requestHitLimit) {
        checkArgument(requestHitLimit > 0, "Argument 'requestHitLimit' should be larger than 0");
        this.requestHitLimit = requestHitLimit;
        return this;
    }

    public FaultInjectionRuleBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public FaultInjectionRule build() {
        checkNotNull(this.condition, "Argument 'condition' can not be null");
        checkNotNull(this.result, "Argument 'result' can not be null");

        return new FaultInjectionRule(this.condition, this.result, this.duration, this.requestHitLimit, this.enabled);
    }
}
