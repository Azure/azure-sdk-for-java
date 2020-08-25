// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

/**
 * A set of options for creating a rule.
 */
public class CreateRuleOptions {
    public static final String DEFAULT_RULE_NAME = "$Default";

    private final String name;
    private final RuleFilter filter;

    public CreateRuleOptions() {
        this(DEFAULT_RULE_NAME, TrueRuleFilter.getInstance());
    }

    public CreateRuleOptions(String name) {
        this(name, TrueRuleFilter.getInstance());
    }

    public CreateRuleOptions(String name, RuleFilter filter) {
        this.name = name;

        this.filter = filter;
    }
}
