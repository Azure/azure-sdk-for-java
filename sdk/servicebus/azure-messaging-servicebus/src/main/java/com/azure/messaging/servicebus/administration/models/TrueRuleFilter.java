// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

/**
 * Matches all the messages arriving to be selected for the subscription.
 *
 * @see CreateRuleOptions#setFilter(RuleFilter)
 * @see RuleProperties#setFilter(RuleFilter)
 */
public final class TrueRuleFilter extends SqlRuleFilter {
    private static final TrueRuleFilter INSTANCE = new TrueRuleFilter();

    /**
     * Gets an instance of the {@link TrueRuleFilter}.
     *
     * @return an instance of the {@link TrueRuleFilter}.
     */
    static TrueRuleFilter getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new instance.
     */
    public TrueRuleFilter() {
        super("1=1");
    }

    /**
     * Converts the current instance to its string representation.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "TrueRuleFilter";
    }
}
