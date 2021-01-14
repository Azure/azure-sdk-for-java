// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.annotation.Fluent;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;

import java.util.Objects;

/**
 * A set of options for creating a rule.
 *
 * @see ServiceBusAdministrationClient#createRule(String, String, String, CreateRuleOptions)
 * @see ServiceBusAdministrationAsyncClient#createRule(String, String, String, CreateRuleOptions)
 */
@Fluent
public final class CreateRuleOptions {
    private RuleFilter filter;
    private RuleAction action;

    /**
     * Initializes a new instance with the {@link TrueRuleFilter}.
     */
    public CreateRuleOptions() {
        this(TrueRuleFilter.getInstance());
    }

    /**
     * Initializes a new instance with the given rule {@code name} and {@code filter}.
     *
     * @param filter Filter expression used to match messages.
     * @throws NullPointerException if {@code filter} is null.
     */
    public CreateRuleOptions(RuleFilter filter) {
        this.filter = Objects.requireNonNull(filter, "'filter' cannot be null.");
    }

    /**
     * Initializes a new instance with the given rule properties.
     *
     * @param ruleProperties Rule properties to create new rule from.
     * @throws NullPointerException if {@code ruleProperties} is null.
     */
    public CreateRuleOptions(RuleProperties ruleProperties) {
        Objects.requireNonNull(ruleProperties, "'ruleProperties' cannot be null.");

        this.filter = ruleProperties.getFilter();
        this.action = ruleProperties.getAction();
    }

    /**
     * Gets the action to perform if the message satisfies the filtering expression.
     *
     * @return The action to perform if the message satisfies the filtering expression.
     */
    public RuleAction getAction() {
        return action;
    }

    /**
     * Sets the action to perform if the message satisfies the filtering expression.
     *
     * @param action The action to perform if the message satisfies the filtering expression.
     * @return The updated {@link CreateRuleOptions} object.
     */
    public CreateRuleOptions setAction(RuleAction action) {
        this.action = action;
        return this;
    }

    /**
     * Gets the filter expression used to match messages.
     *
     * @return The filter expression used to match messages.
     */
    public RuleFilter getFilter() {
        return filter;
    }

    /**
     * Sets the filter expression used to match messages.
     *
     * @param filter The filter expression used to match messages.
     * @return The updated {@link CreateRuleOptions} object.
     */
    public CreateRuleOptions setFilter(RuleFilter filter) {
        this.filter = Objects.requireNonNull(filter, "'filter' cannot be null.");
        return this;
    }
}
