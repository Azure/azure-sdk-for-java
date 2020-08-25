// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

/**
 * Matches none the messages arriving to be selected for the subscription.
 */
public class FalseRuleFilter extends SqlRuleFilter {
    /**
     * Initializes a new instance.
     */
    public FalseRuleFilter() {
        super("1=0");
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
