// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

/**
 * Represents the filter actions which are allowed for the transformation of a message that have been matched by a
 * filter expression.
 *
 * <p>
 * Filter actions allow for the transformation of a message that have been matched by a filter expression. The typical
 * use case for filter actions is to append or update the properties that are attached to a message, for example
 * assigning a group ID based on the correlation ID of a message.
 * </p>
 *
 * @see EmptyRuleAction
 * @see SqlRuleAction
 * @see CreateRuleOptions#setAction(RuleAction)
 * @see RuleProperties#setAction(RuleAction)
 */
public class RuleAction {
    RuleAction() {
        // This is intentionally left blank. This constructor exists
        // only to prevent external assemblies inheriting from it.
    }
}
