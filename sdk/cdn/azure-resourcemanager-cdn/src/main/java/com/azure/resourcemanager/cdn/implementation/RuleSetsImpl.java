// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.RuleSetInner;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.RuleSet;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of rule sets associated with a CDN profile.
 */
class RuleSetsImpl
    extends ExternalChildResourcesNonCachedImpl<RuleSetImpl, RuleSet, RuleSetInner, CdnProfileImpl, CdnProfile> {

    RuleSetsImpl(CdnProfileImpl parent) {
        super(parent, parent.taskGroup(), "RuleSet");
    }

    Map<String, RuleSet> ruleSetsAsMap() {
        Map<String, RuleSet> result = new HashMap<>();
        for (RuleSetInner inner : this.getParent()
            .manager()
            .serviceClient()
            .getRuleSets()
            .listByProfile(this.getParent().resourceGroupName(), this.getParent().name())) {
            RuleSetImpl ruleSet = new RuleSetImpl(inner.name(), this.getParent(), inner);
            result.put(ruleSet.name(), ruleSet);
        }
        return Collections.unmodifiableMap(result);
    }

    void remove(String name) {
        this.prepareInlineRemove(new RuleSetImpl(name, getParent(), null));
    }

    void addRuleSet(RuleSetImpl ruleSet) {
        this.childCollection.put(ruleSet.name(), ruleSet);
    }

    RuleSetImpl defineNewRuleSet(String name) {
        return this.prepareInlineDefine(new RuleSetImpl(name, this.getParent(), null));
    }

    RuleSetImpl updateRuleSet(String name) {
        RuleSetInner inner = this.getParent()
            .manager()
            .serviceClient()
            .getRuleSets()
            .get(this.getParent().resourceGroupName(), this.getParent().name(), name);
        return this.prepareInlineUpdate(new RuleSetImpl(name, this.getParent(), inner));
    }
}
