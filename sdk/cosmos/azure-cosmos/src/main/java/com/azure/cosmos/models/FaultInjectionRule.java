// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.faultinjection.model.IFaultInjectionRuleInternal;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionRule {
    private final FaultInjectionCondition condition;
    private final IFaultInjectionResult result;
    private final Duration duration;
    private final Duration startDelay;
    private final Integer hitLimit;
    private final String id;
    private boolean enabled;
    private IFaultInjectionRuleInternal effectiveRule;

    public FaultInjectionRule(
        String id,
        Duration startDelay,
        Duration duration,
        Integer requestHitLimit,
        boolean enabled,
        FaultInjectionCondition condition,
        IFaultInjectionResult result) {

        checkNotNull(condition, "Argument 'condition' can not be null");
        checkNotNull(result, "Argument 'result' can not be null");

        this.id = id;
        this.startDelay = startDelay;
        this.duration = duration;
        this.hitLimit = requestHitLimit;
        this.enabled = enabled;
        this.condition = condition;
        this.result = result;
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

    public Duration getStartDelay() { return startDelay; }

    public Integer getHitLimit() {
        return hitLimit;
    }

    public String getId() {
        return this.id;
    }

    public boolean isEnabled() { return this.enabled; }

    public void disable() {
        this.enabled = false;
        if (this.effectiveRule != null) {
            this.effectiveRule.disable();
        }
    }

    public long getHitCount() {
        return this.effectiveRule.getHitCount();
    }

    public List<URI> getAddresses() {
        return this.effectiveRule.getAddresses();
    }

    public List<URI> getRegionEndpoints() {
        return this.effectiveRule.getRegionEndpoints();
    }

    void setEffectiveFaultInjectionRule(IFaultInjectionRuleInternal effectiveRule) {
        this.effectiveRule = effectiveRule;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.FaultInjectionRuleHelper.setFaultInjectionRuleAccessor(
            (rule, ruleInternal) -> rule.setEffectiveFaultInjectionRule(ruleInternal));
    }

    static { initialize(); }
}
