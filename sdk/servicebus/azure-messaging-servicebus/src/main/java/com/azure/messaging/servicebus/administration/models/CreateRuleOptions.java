// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.annotation.Fluent;

import java.util.Objects;

/**
 * A set of options for creating a rule.
 */
@Fluent
public class CreateRuleOptions {
    public static final String DEFAULT_RULE_NAME = "$Default";

    private final String name;
    private RuleFilter filter;
    private RuleAction action;

    /**
     * Initializes a new instance with the default rule name, '$Default' and the {@link TrueRuleFilter}.
     */
    public CreateRuleOptions() {
        this(DEFAULT_RULE_NAME, TrueRuleFilter.getInstance());
    }

    /**
     * Initializes a new instance with the given rule name and {@link TrueRuleFilter}.
     *
     * @param name Name of the rule.
     *
     * @throws IllegalArgumentException if the length of name is over 50 characters.
     */
    public CreateRuleOptions(String name) {
        this(name, TrueRuleFilter.getInstance());
    }

    /**
     * Initializes a new instance with the given rule {@code name} and {@code filter}.
     *
     * @param name Name of the rule.
     * @param filter Filter expression used to match messages.
     */
    public CreateRuleOptions(String name, RuleFilter filter) {
        this.name = name;
        this.filter = filter;
    }

    public CreateRuleOptions(RuleProperties ruleProperties) {
        this.filter = ruleProperties.getFilter();
        this.name = ruleProperties.getName();
        this.action = ruleProperties.getAction();
    }

    public RuleAction getAction() {
        return action;
    }

    public void setAction(RuleAction action) {
        this.action = action;
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
     */
    public void setFilter(RuleFilter filter) {
        this.filter = Objects.requireNonNull(filter, "'filter' cannot be null.");
    }

    /**
     * Gets the name of the rule.
     *
     * @return The name of the rule.
     */
    public String getName() {
        return name;
    }
}
