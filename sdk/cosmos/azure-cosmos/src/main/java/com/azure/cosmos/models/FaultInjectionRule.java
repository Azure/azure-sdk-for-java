// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectionRuleInternal;

import java.time.Duration;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionRule {
    private final FaultInjectionCondition condition;
    private final IFaultInjectionResult result;
    private final Duration duration;
    private final int requestHitLimit;
    private final String id;
    private boolean enabled;
    private IFaultInjectionRuleInternal effectiveFaultInjectionRule;

    public FaultInjectionRule(
        String id,
        FaultInjectionCondition condition,
        IFaultInjectionResult result,
        Duration duration,
        int requestHitLimit,
        boolean enabled) {

        checkNotNull(condition, "Argument 'condition' can not be null");
        checkNotNull(result, "Argument 'result' can not be null");
        checkArgument(requestHitLimit > 0, "Argument 'requestHitLimit' should be larger than 0");

        this.id = id;
        this.condition = condition;
        this.result = result;
        this.duration = duration;
        this.requestHitLimit = requestHitLimit;
        this.enabled = enabled;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FaultInjectionCondition getCondition() {
        return condition;
    }

    public IFaultInjectionResult getResult() {
        return result;
    }

    public Duration getDuration() {
        return duration;
    }

    public int getRequestHitLimit() {
        return requestHitLimit;
    }

    public String getId() {
        return this.id;
    }

    public boolean isEnabled() { return this.enabled; }

    public void disable() {
        this.enabled = false;
    }

    void setEffectiveFaultInjectionRule(IFaultInjectionRuleInternal effectiveFaultInjectionRule) {
        this.effectiveFaultInjectionRule = effectiveFaultInjectionRule;
    }

    public List<String> getEndpointAddresses() {
        return this.effectiveFaultInjectionRule.getEndpointAddresses();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosFaultInjectionRuleHelper.setCosmosFaultInjectionRuleAccessor(
            new ImplementationBridgeHelpers.CosmosFaultInjectionRuleHelper.CosmosFaultInjectionRuleAccessor() {
                @Override
                public void setEffectiveFaultInjectionRule(FaultInjectionRule rule, IFaultInjectionRuleInternal ruleInternal) {
                    rule.setEffectiveFaultInjectionRule(ruleInternal);
                }
            });
    }

    static { initialize(); }
}
