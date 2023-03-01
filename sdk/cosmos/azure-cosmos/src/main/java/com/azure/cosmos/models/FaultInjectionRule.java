// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.faultinjection.model.IFaultInjectionRuleInternal;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * Fault injection rule.
 */
public class FaultInjectionRule {
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
     * Flag to indicate whether the fault injection rule is enabled.
     * By default, it is enabled.
     *
     * @param enabled flag to indicate whether the rule is enabled.
     */
    public void enabled(boolean enabled) {
        this.enabled = enabled;
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
    public Duration getStartDelay() { return startDelay; }

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
    public boolean isEnabled() { return this.enabled; }

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

    /***
     * Get the physical addresses of the fault injection rule.
     *
     * @return the list of physical addresses.
     */
    public List<URI> getAddresses() {
        return this.effectiveRule == null ? null : this.effectiveRule.getAddresses();
    }

    /***
     * Get the region endpoints of the fault injection rule.
     *
     * @return the list of region endpoints.
     */
    public List<URI> getRegionEndpoints() {
        return this.effectiveRule == null ? null : this.effectiveRule.getRegionEndpoints();
    }

    /***
     * Set the effective fault injection rule.
     *
     * @param effectiveRule the effective fault injection rule.
     */
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
