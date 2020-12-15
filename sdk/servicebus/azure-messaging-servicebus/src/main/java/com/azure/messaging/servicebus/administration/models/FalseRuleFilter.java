// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

/**
 * Matches none the messages arriving to be selected for the subscription.
 */
public final class FalseRuleFilter extends SqlRuleFilter {
    private static final FalseRuleFilter INSTANCE = new FalseRuleFilter();

    /**
     * Initializes a new instance.
     */
    public FalseRuleFilter() {
        super("1=0");
    }

    /**
     * Gets an instance of the {@link FalseRuleFilter}.
     *
     * @return an instance of the {@link FalseRuleFilter}.
     */
    static FalseRuleFilter getInstance() {
        return INSTANCE;
    }

    /**
     * Converts the current instance to its string representation.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "FalseRuleFilter";
    }
}
