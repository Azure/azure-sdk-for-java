// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionRuleBuilder {

    private final String id;
    private FaultInjectionCondition condition;
    private IFaultInjectionResult result;
    private Duration duration;
    private Duration startDelay;
    private Integer hitLimit;
    private boolean enabled = true;

    /***
     * The constructor.
     *
     * @param id the fault injection rule id.
     */
    public FaultInjectionRuleBuilder(String id) {
        checkArgument(StringUtils.isNotEmpty(id), "Argument 'id' can not be null or empty");
        this.id = id;
    }

    /***
     * Set the condition of the rule. The rule will not be applicable if not all conditions are met.
     *
     * @param condition the {@link FaultInjectionCondition}.
     * @return the builder.
     */
    public FaultInjectionRuleBuilder condition(FaultInjectionCondition condition) {
        checkNotNull(condition, "Argument 'condition' can not be null");
        this.condition = condition;
        return this;
    }

    /***
     * Set the result of the rule.
     *
     * @param faultInjectionResult the {@link IFaultInjectionResult}.
     * @return the builder.
     */
    public FaultInjectionRuleBuilder result(IFaultInjectionResult faultInjectionResult) {
        checkNotNull(faultInjectionResult, "Argument 'faultInjectionResult' can not be null");
        this.result = faultInjectionResult;
        return this;
    }

    /***
     * Set the effective duration of the rule. The rule will not be applicable if after the effective duration.
     *
     * By default, it is null which means it will be effective until the end of the application.
     *
     * @param duration the effective duration.
     * @return the builder.
     */
    public FaultInjectionRuleBuilder duration(Duration duration) {
        checkNotNull(duration, "Argument 'duration' can not be null");
        this.duration = duration;
        return this;
    }

    /***
     * Set the start time of the rule. The rule will not be applicable before the start time.
     *
     * By default, it is null which means the rule will be effective right away.
     *
     * @param startDelay the delay of the rule.
     * @return the builder.
     */
    public FaultInjectionRuleBuilder startDelay(Duration startDelay) {
        checkNotNull(startDelay, "Argument 'startDelay' can not be null");
        this.startDelay = startDelay;
        return this;
    }

    /***
     * Set the total hit limit of the rule. The rule will be not applicable anymore once it has applied hitLimit times.
     *
     * By default, it is null which means there is no limit.
     *
     * @param hitLimit the hit limit.
     * @return the builder.
     */
    public FaultInjectionRuleBuilder hitLimit(int hitLimit) {
        checkArgument(hitLimit > 0, "Argument 'hitLimit' should be larger than 0");
        this.hitLimit = hitLimit;
        return this;
    }

    /***
     * Flag to indicate whether the rule is enabled. The rule will not be applicable if it is disabled.
     * A rule can be disabled/enable multiple times.
     *
     * By default, it is enabled.
     *
     * @param enabled flag to indicate whether the rule is enabled.
     * @return the builder.
     */
    public FaultInjectionRuleBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /***
     * Create a new fault injection rule.
     *
     * @return the {@link FaultInjectionRule}.
     */
    public FaultInjectionRule build() {
        checkNotNull(this.condition, "Argument 'condition' can not be null");
        checkNotNull(this.result, "Argument 'result' can not be null");

        return new FaultInjectionRule(
            this.id,
            this.startDelay,
            this.duration,
            this.hitLimit,
            this.enabled,
            this.condition,
            this.result);
    }
}
