// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.models.AuthorizationRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import reactor.core.publisher.Mono;

abstract class AuthorizationRulesBaseImpl<InnerT, RuleT, RuleImpl>
    extends WrapperImpl<InnerT>
    implements HasManager<EventHubsManager>,
        SupportsGettingById<RuleT>,
        SupportsDeletingById {

    protected final EventHubsManager manager;

    protected AuthorizationRulesBaseImpl(EventHubsManager manager, InnerT inner) {
        super(inner);
        this.manager = manager;
    }

    @Override
    public EventHubsManager manager() {
        return this.manager;
    }

    @Override
    public RuleT getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public void deleteById(String id) {
        deleteByIdAsync(id).block();
    }

    protected abstract RuleImpl wrapModel(AuthorizationRuleInner innerModel);
    public abstract Mono<RuleT> getByIdAsync(String id);
}
