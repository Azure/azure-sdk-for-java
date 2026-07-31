// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.RuleInner;
import com.azure.resourcemanager.cdn.models.Rule;
import com.azure.resourcemanager.cdn.models.RuleSet;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of rules associated with a rule set.
 */
class RulesImpl extends ExternalChildResourcesNonCachedImpl<RuleImpl, Rule, RuleInner, RuleSetImpl, RuleSet> {

    RulesImpl(RuleSetImpl parent) {
        super(parent, parent.taskGroup(), "Rule");
    }

    Map<String, Rule> rulesAsMap() {
        Map<String, Rule> result = new HashMap<>();
        for (RuleInner ruleInner : this.getParent()
            .parent()
            .manager()
            .serviceClient()
            .getRules()
            .listByRuleSet(this.getParent().parent().resourceGroupName(), this.getParent().parent().name(),
                this.getParent().name())) {
            RuleImpl rule = new RuleImpl(ruleInner.name(), this.getParent(), ruleInner);
            result.put(rule.name(), rule);
        }
        return Collections.unmodifiableMap(result);
    }

    void remove(String name) {
        this.prepareInlineRemove(new RuleImpl(name, getParent(), new RuleInner()));
    }

    void addRule(RuleImpl rule) {
        this.childCollection.put(rule.name(), rule);
    }

    RuleImpl defineNewRule(String name) {
        return this.prepareInlineDefine(new RuleImpl(name, this.getParent(), new RuleInner()));
    }

    RuleImpl updateRule(String name) {
        RuleInner ruleInner = this.getParent()
            .parent()
            .manager()
            .serviceClient()
            .getRules()
            .get(this.getParent().parent().resourceGroupName(), this.getParent().parent().name(),
                this.getParent().name(), name);
        return this.prepareInlineUpdate(new RuleImpl(name, this.getParent(), ruleInner));
    }
}
