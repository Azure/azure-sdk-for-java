// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus.rules;

/**
 * Representation of a rule defined on service bus topics.
 *
 * @since 1.0
 */
public class RuleDescription {
    private Filter filter;
    private RuleAction action;
    private String name;

    /**
     * Creates a rule description with no name, {@link TrueFilter} as filter and no action.
     */
    public RuleDescription() {
        this.filter = TrueFilter.DEFAULT;
    }

    /**
     * Creates a rule description with the given name, {@link TrueFilter} as filter and no action.
     *
     * @param name name of the rule
     */
    public RuleDescription(String name) {
        this.filter = TrueFilter.DEFAULT;
        this.name = name;
    }

    /**
     * Creates a rule description with no name, given filter as filter and no action.
     *
     * @param filter filter the rule uses to filter messages. Can be {@link CorrelationFilter} or {@link SqlFilter}.
     */
    public RuleDescription(Filter filter) {
        this.filter = filter;
    }

    /**
     * Creates a rule description with the given name, given filter as filter and no action.
     *
     * @param name   name of the rule
     * @param filter filter this rule uses to filter messages. Can be Can be {@link CorrelationFilter} or {@link SqlFilter}.
     */
    public RuleDescription(String name, Filter filter) {
        this.name = name;
        this.filter = filter;
    }

    /**
     * Gets the filter of this rule.
     *
     * @return the filter this rule uses to filter messages
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Sets the filter of this rule.
     *
     * @param filter filter this rule uses to filter messages. Can be Can be {@link CorrelationFilter} or {@link SqlFilter}.
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    /**
     * Gets the action this rule performs on messages matched by this rule's filter.
     *
     * @return action action this rule performs on matched messages
     */
    public RuleAction getAction() {
        return action;
    }

    /**
     * Sets the action this rule performs on messages matched by this rule's filter.
     *
     * @param action action this rule performs on matched messages
     */
    public void setAction(RuleAction action) {
        this.action = action;
    }

    /**
     * Gets the name of this rule.
     *
     * @return name of this rule
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this rule.
     *
     * @param name name of this rule
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof RuleDescription)) {
            return false;
        }

        RuleDescription otherRule = (RuleDescription)other;
        if (this.name == null ? otherRule.name == null : this.name.equalsIgnoreCase(otherRule.name)
            && this.filter == null ? otherRule.filter == null : this.filter.equals(otherRule.filter)
            && this.action == null ? otherRule.action == null : this.action.equals(otherRule.action)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 13;

        if (this.filter != null) {
            hash = hash * 7 + this.filter.hashCode();
        }

        if (this.action != null) {
            hash = hash * 7 + this.action.hashCode();
        }

        return hash;
    }
}
