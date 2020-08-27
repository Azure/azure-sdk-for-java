// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.annotation.Fluent;
import com.azure.messaging.servicebus.implementation.EntityHelper;

import java.util.Objects;

/**
 * Represents the properties of a rule.
 */
@Fluent
public class RuleProperties {
    private final String name;
    private RuleFilter filter;
    private RuleAction action;

    static {
        EntityHelper.setRuleAccessor(new EntityHelper.RuleAccessor() {
            @Override
            public RuleProperties toModel(String name, RuleFilter filter, RuleAction action) {
                return new RuleProperties(name, filter, action);
            }
        });
    }

    /**
     * Initializes a new instance with the given rule {@code name}, {@code filter}, and {@code action}.
     *
     * @param name Name of the rule.
     * @param filter Filter for the rule.
     * @param action Action for the rule.
     */
    RuleProperties(String name, RuleFilter filter, RuleAction action) {
        this.name = name;
        this.filter = filter;
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
     * @param filter the filter expression used to match messages.
     *
     * @return The updated {@link RuleProperties} object itself.
     * @throws NullPointerException if {@code filter} is null.
     */
    public RuleProperties setFilter(RuleFilter filter) {
        this.filter = Objects.requireNonNull(filter, "'filter' cannot be null.");
        return this;
    }

    /**
     * Gets the name of the rule.
     *
     * @return The name of the rule.
     */
    public String getName() {
        return name;
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
     *
     * @return The updated {@link RuleProperties} object itself.
     */
    public RuleProperties setAction(RuleAction action) {
        this.action = action;
        return this;
    }
}
