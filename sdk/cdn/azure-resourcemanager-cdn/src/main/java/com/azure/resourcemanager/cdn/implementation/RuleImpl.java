// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.RuleInner;
import com.azure.resourcemanager.cdn.models.AfdProvisioningState;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.DeliveryRuleAction;
import com.azure.resourcemanager.cdn.models.DeliveryRuleCondition;
import com.azure.resourcemanager.cdn.models.DeploymentStatus;
import com.azure.resourcemanager.cdn.models.MatchProcessingBehavior;
import com.azure.resourcemanager.cdn.models.Rule;
import com.azure.resourcemanager.cdn.models.RuleSet;
import com.azure.resourcemanager.cdn.models.RuleUpdateParameters;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Implementation for {@link Rule}.
 */
class RuleImpl extends ExternalChildResourceImpl<Rule, RuleInner, RuleSetImpl, RuleSet> implements Rule,
    Rule.DefinitionStages.Blank<RuleSet.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    Rule.DefinitionStages.WithOrder<RuleSet.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    Rule.DefinitionStages.WithActions<RuleSet.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    Rule.DefinitionStages.WithAttach<RuleSet.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    Rule.UpdateDefinitionStages.Blank<RuleSet.Update>, Rule.UpdateDefinitionStages.WithOrder<RuleSet.Update>,
    Rule.UpdateDefinitionStages.WithActions<RuleSet.Update>, Rule.UpdateDefinitionStages.WithAttach<RuleSet.Update>,
    Rule.Update {

    RuleImpl(String name, RuleSetImpl parent, RuleInner inner) {
        super(name, parent, inner);
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String ruleSetName() {
        return this.innerModel().ruleSetName();
    }

    @Override
    public Integer order() {
        return this.innerModel().order();
    }

    @Override
    public List<DeliveryRuleCondition> conditions() {
        return this.innerModel().conditions();
    }

    @Override
    public List<DeliveryRuleAction> actions() {
        return this.innerModel().actions();
    }

    @Override
    public MatchProcessingBehavior matchProcessingBehavior() {
        return this.innerModel().matchProcessingBehavior();
    }

    @Override
    public AfdProvisioningState provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public DeploymentStatus deploymentStatus() {
        return this.innerModel().deploymentStatus();
    }

    @Override
    public Mono<Rule> createResourceAsync() {
        final RuleImpl self = this;
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getRules()
            .createAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(),
                this.parent().name(), this.name(), this.innerModel())
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<Rule> updateResourceAsync() {
        final RuleImpl self = this;
        RuleUpdateParameters parameters = new RuleUpdateParameters().withOrder(this.innerModel().order())
            .withConditions(this.innerModel().conditions())
            .withActions(this.innerModel().actions())
            .withMatchProcessingBehavior(this.innerModel().matchProcessingBehavior());
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getRules()
            .updateAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(),
                this.parent().name(), this.name(), parameters)
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getRules()
            .deleteAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(),
                this.parent().name(), this.name());
    }

    @Override
    protected Mono<RuleInner> getInnerAsync() {
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getRules()
            .getAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(), this.parent().name(),
                this.name());
    }

    @Override
    public RuleSetImpl attach() {
        return this.parent().withRule(this);
    }

    // ---- Fluent setters (shared by DefinitionStages, UpdateDefinitionStages, and Update) ----

    @Override
    public RuleImpl withOrder(int order) {
        this.innerModel().withOrder(order);
        return this;
    }

    @Override
    public RuleImpl withActions(List<DeliveryRuleAction> actions) {
        this.innerModel().withActions(actions);
        return this;
    }

    @Override
    public RuleImpl withConditions(List<DeliveryRuleCondition> conditions) {
        this.innerModel().withConditions(conditions);
        return this;
    }

    @Override
    public RuleImpl withMatchProcessingBehavior(MatchProcessingBehavior matchProcessingBehavior) {
        this.innerModel().withMatchProcessingBehavior(matchProcessingBehavior);
        return this;
    }
}
