// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

/**
 *
 */
public class Rule {
    private final String name;
    private final String ruleAction;

    /**
     * Constructor to create rule.
     * @param name of the rule.
     * @param ruleAction to be taken for this rule.
     */
    public Rule(String name, String ruleAction) {
        this.name = name;
        this.ruleAction = ruleAction;
    }
    /**
     *
      * @return the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     *
     * @return the rule action.
     */
    public String getRuleAction() {
        return this.ruleAction;
    }


}
