// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.securityinsights.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The AutomationRuleBooleanCondition model. */
@Fluent
public final class AutomationRuleBooleanCondition {
    /*
     * The operator property.
     */
    @JsonProperty(value = "operator")
    private AutomationRuleBooleanConditionSupportedOperator operator;

    /*
     * The innerConditions property.
     */
    @JsonProperty(value = "innerConditions")
    private List<AutomationRuleCondition> innerConditions;

    /**
     * Get the operator property: The operator property.
     *
     * @return the operator value.
     */
    public AutomationRuleBooleanConditionSupportedOperator operator() {
        return this.operator;
    }

    /**
     * Set the operator property: The operator property.
     *
     * @param operator the operator value to set.
     * @return the AutomationRuleBooleanCondition object itself.
     */
    public AutomationRuleBooleanCondition withOperator(AutomationRuleBooleanConditionSupportedOperator operator) {
        this.operator = operator;
        return this;
    }

    /**
     * Get the innerConditions property: The innerConditions property.
     *
     * @return the innerConditions value.
     */
    public List<AutomationRuleCondition> innerConditions() {
        return this.innerConditions;
    }

    /**
     * Set the innerConditions property: The innerConditions property.
     *
     * @param innerConditions the innerConditions value to set.
     * @return the AutomationRuleBooleanCondition object itself.
     */
    public AutomationRuleBooleanCondition withInnerConditions(List<AutomationRuleCondition> innerConditions) {
        this.innerConditions = innerConditions;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (innerConditions() != null) {
            innerConditions().forEach(e -> e.validate());
        }
    }
}
