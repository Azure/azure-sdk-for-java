// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

/**
 * Represents a rule action that does nothing.
 *
 * @see CreateRuleOptions#setAction(RuleAction)
 * @see RuleProperties#setAction(RuleAction)
 */
public class EmptyRuleAction extends RuleAction {
    private static final EmptyRuleAction INSTANCE = new EmptyRuleAction();

    /**
     * Creates a new instance of a rule action that does nothing.
     */
    public EmptyRuleAction() {
    }

    /**
     * Gets an instance of the empty rule action.
     *
     * @return An instance of the empty rule action.
     */
    static EmptyRuleAction getInstance() {
        return INSTANCE;
    }
}
