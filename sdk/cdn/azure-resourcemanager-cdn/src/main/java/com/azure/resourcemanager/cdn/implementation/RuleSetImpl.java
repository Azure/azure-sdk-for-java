// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.RuleSetInner;
import com.azure.resourcemanager.cdn.models.AfdProvisioningState;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.DeploymentStatus;
import com.azure.resourcemanager.cdn.models.Rule;
import com.azure.resourcemanager.cdn.models.RuleSet;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Implementation for {@link RuleSet}.
 */
class RuleSetImpl extends ExternalChildResourceImpl<RuleSet, RuleSetInner, CdnProfileImpl, CdnProfile>
    implements RuleSet, RuleSet.DefinitionStages.Blank<CdnProfile.DefinitionStages.WithStandardCreate>,
    RuleSet.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>,
    RuleSet.UpdateDefinitionStages.Blank<CdnProfile.Update>,
    RuleSet.UpdateDefinitionStages.WithAttach<CdnProfile.Update>, RuleSet.Update {

    private final RulesImpl rules;

    RuleSetImpl(String name, CdnProfileImpl parent, RuleSetInner inner) {
        super(name, parent, inner);
        this.rules = new RulesImpl(this);
    }

    @Override
    public String id() {
        return this.innerModel() == null ? null : this.innerModel().id();
    }

    @Override
    public String profileName() {
        return this.innerModel() == null ? null : this.innerModel().profileName();
    }

    @Override
    public AfdProvisioningState provisioningState() {
        return this.innerModel() == null ? null : this.innerModel().provisioningState();
    }

    @Override
    public DeploymentStatus deploymentStatus() {
        return this.innerModel() == null ? null : this.innerModel().deploymentStatus();
    }

    @Override
    public Map<String, Rule> rules() {
        return this.rules.rulesAsMap();
    }

    @Override
    public Mono<RuleSet> createResourceAsync() {
        final RuleSetImpl self = this;
        return this.parent()
            .manager()
            .serviceClient()
            .getRuleSets()
            .createAsync(this.parent().resourceGroupName(), this.parent().name(), this.name())
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<RuleSet> updateResourceAsync() {
        final RuleSetImpl self = this;
        return getInnerAsync().map(inner -> {
            self.setInner(inner);
            return self;
        });
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this.parent()
            .manager()
            .serviceClient()
            .getRuleSets()
            .deleteAsync(this.parent().resourceGroupName(), this.parent().name(), this.name());
    }

    @Override
    protected Mono<RuleSetInner> getInnerAsync() {
        return this.parent()
            .manager()
            .serviceClient()
            .getRuleSets()
            .getAsync(this.parent().resourceGroupName(), this.parent().name(), this.name());
    }

    @Override
    protected Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        this.rules.clear();
        return Mono.empty();
    }

    @Override
    public CdnProfileImpl attach() {
        return this.parent().withRuleSet(this);
    }

    // ---- Nested rule CRUD ----

    @Override
    public RuleImpl defineRule(String name) {
        return this.rules.defineNewRule(name);
    }

    @Override
    public RuleImpl updateRule(String name) {
        return this.rules.updateRule(name);
    }

    @Override
    public RuleSetImpl withoutRule(String name) {
        this.rules.remove(name);
        return this;
    }

    RuleSetImpl withRule(RuleImpl rule) {
        this.rules.addRule(rule);
        return this;
    }
}
