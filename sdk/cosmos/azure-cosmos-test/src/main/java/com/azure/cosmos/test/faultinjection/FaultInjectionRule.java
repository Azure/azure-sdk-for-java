// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

import com.azure.cosmos.test.implementation.faultinjection.IFaultInjectionRuleInternal;
import com.azure.cosmos.test.implementation.ImplementationBridgeHelpers;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * Fault injection rule.
 */
public final class FaultInjectionRule {
    private final FaultInjectionCondition condition;
    private final IFaultInjectionResult result;
    private final Duration duration;
    private final Duration startDelay;
    private final Integer hitLimit;
    private final String id;
    private boolean enabled;
    private IFaultInjectionRuleInternal effectiveRule;

    FaultInjectionRule(
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

    /***
     * Get the fault injection condition.
     *
     * @return the {@link FaultInjectionCondition}.
     */
    public FaultInjectionCondition getCondition() {
        return condition;
    }

    /***
     * Get the fault injection result.
     *
     * @return the {@link IFaultInjectionResult}.
     */
    public IFaultInjectionResult getResult() {
        return result;
    }

    /***
     * Get the effective life span of the fault injection rule.
     *
     * @return the duration.
     */
    public Duration getDuration() {
        return duration;
    }

    /***
     * Get the start delay of the fault injection rule.
     *
     * @return the start delay.
     */
    public Duration getStartDelay() {
        return startDelay;
    }

    /***
     * Get the hit limit of the fault injection rule.
     *
     * @return the hit limit.
     */
    public Integer getHitLimit() {
        return hitLimit;
    }

    /***
     * Get the fault injection rule id.
     *
     * @return the id.
     */
    public String getId() {
        return this.id;
    }

    /***
     * Get flag to indicate whether the rule is enabled.
     *
     * @return the flag to indicate whether the rule is enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /***
     * Disable the fault injection rule.
     */
    public void disable() {
        this.enabled = false;
        if (this.effectiveRule != null) {
            this.effectiveRule.disable();
        }
    }

    /**
     * Get the count of how many times the rule has applied.
     *
     * @return the hit count.
     */
    public long getHitCount() {
        return this.effectiveRule == null ? 0 : this.effectiveRule.getHitCount();
    }

    /**
     * Get the details of how many times the rule has applied.
     *
     * @return the hit count details map.
     */
    public Map<String, Long> getHitCountDetails() {
        return this.effectiveRule == null ? null : this.effectiveRule.getHitCountDetails();
    }

    /***
     * Get the physical addresses of the fault injection rule.
     *
     * @return the list of physical addresses.
     */
    public List<String> getAddresses() {
        return this.effectiveRule == null
            ? null
            : this.effectiveRule.getAddresses().stream().map(URI::toString).collect(Collectors.toList());
    }

    /***
     * Get the region endpoints of the fault injection rule.
     *
     * @return the list of region endpoints.
     */
    public List<String> getRegionEndpoints() {
        return this.effectiveRule == null
            ? null
            : this.effectiveRule.getRegionalRoutingContexts().stream().map(regionalRoutingContext -> regionalRoutingContext.getGatewayRegionalEndpoint().toString()).collect(Collectors.toList());
    }

    /***
     * Set the effective fault injection rule.
     *
     * @param effectiveRule the effective fault injection rule.
     */
    void setEffectiveFaultInjectionRule(IFaultInjectionRuleInternal effectiveRule) {
        this.effectiveRule = effectiveRule;
    }

    @Override
    public String toString() {
        return String.format(
            "FaultInjectionRule{ condition=%s, result=%s, duration=%s, startDelay=%s, hitLimit=%s, id=%s, enabled=%s",
            this.condition,
            this.result,
            this.duration,
            this.startDelay,
            this.hitLimit,
            this.id,
            this.enabled);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.FaultInjectionRuleHelper.setFaultInjectionRuleAccessor(
            (rule, ruleInternal) -> rule.setEffectiveFaultInjectionRule(ruleInternal));
    }

    static {
        initialize();
    }
}
