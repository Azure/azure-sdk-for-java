// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.faultinjection.model.IFaultInjectionRuleInternal;
import com.azure.cosmos.util.Beta;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * Fault injection rule.
 */
@Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public FaultInjectionCondition getCondition() {
        return condition;
    }

    /***
     * Get the fault injection result.
     *
     * @return the {@link IFaultInjectionResult}.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public IFaultInjectionResult getResult() {
        return result;
    }

    /***
     * Get the effective life span of the fault injection rule.
     *
     * @return the duration.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Duration getDuration() {
        return duration;
    }

    /***
     * Get the start delay of the fault injection rule.
     *
     * @return the start delay.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Duration getStartDelay() { return startDelay; }

    /***
     * Get the hit limit of the fault injection rule.
     *
     * @return the hit limit.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Integer getHitLimit() {
        return hitLimit;
    }

    /***
     * Get the fault injection rule id.
     *
     * @return the id.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getId() {
        return this.id;
    }

    /***
     * Get flag to indicate whether the rule is enabled.
     *
     * @return the flag to indicate whether the rule is enabled.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public boolean isEnabled() { return this.enabled; }

    /***
     * Disable the fault injection rule.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public long getHitCount() {
        return this.effectiveRule == null ? 0 : this.effectiveRule.getHitCount();
    }

    /***
     * Get the physical addresses of the fault injection rule.
     *
     * @return the list of physical addresses.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public List<URI> getAddresses() {
        return this.effectiveRule == null ? null : this.effectiveRule.getAddresses();
    }

    /***
     * Get the region endpoints of the fault injection rule.
     *
     * @return the list of region endpoints.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public List<URI> getRegionEndpoints() {
        return this.effectiveRule == null ? null : this.effectiveRule.getRegionEndpoints();
    }

    /***
     * Set the effective fault injection rule.
     *
     * @param effectiveRule the effective fault injection rule.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    void setEffectiveFaultInjectionRule(IFaultInjectionRuleInternal effectiveRule) {
        this.effectiveRule = effectiveRule;
    }

    @Override
    public String toString() {
        return "FaultInjectionRule{" +
            "condition=" + condition +
            ", result=" + result +
            ", duration=" + duration +
            ", startDelay=" + startDelay +
            ", hitLimit=" + hitLimit +
            ", id='" + id + '\'' +
            ", enabled=" + enabled +
            ", effectiveRule=" + effectiveRule +
            '}';
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
