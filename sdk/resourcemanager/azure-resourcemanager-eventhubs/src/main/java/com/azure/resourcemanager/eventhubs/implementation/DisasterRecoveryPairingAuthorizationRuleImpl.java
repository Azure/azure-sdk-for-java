// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.inner.AuthorizationRuleInner;
import com.azure.resourcemanager.eventhubs.models.AccessRights;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationKey;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationRule;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * Implementation for {@link DisasterRecoveryPairingAuthorizationRule}.
 */
class DisasterRecoveryPairingAuthorizationRuleImpl
        extends WrapperImpl<AuthorizationRuleInner>
        implements DisasterRecoveryPairingAuthorizationRule {

    private final EventHubsManager manager;
    private final Ancestors.TwoAncestor ancestor;

    protected DisasterRecoveryPairingAuthorizationRuleImpl(AuthorizationRuleInner inner, EventHubsManager manager) {
        super(inner);
        this.manager = manager;
        this.ancestor =  new Ancestors().new TwoAncestor(inner.id());
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public List<AccessRights> rights() {
        return this.inner().rights();
    }

    @Override
    public Mono<DisasterRecoveryPairingAuthorizationKey> getKeysAsync() {
        return this.manager.inner().getDisasterRecoveryConfigs()
            .listKeysAsync(this.ancestor().resourceGroupName(),
                this.ancestor.ancestor2Name(),
                this.ancestor().ancestor1Name(),
                this.name())
            .map(DisasterRecoveryPairingAuthorizationKeyImpl::new);
    }

    @Override
    public DisasterRecoveryPairingAuthorizationKey getKeys() {
        return this.getKeysAsync().block();
    }

    @Override
    public EventHubsManager manager() {
        return this.manager;
    }

    private Ancestors.TwoAncestor ancestor() {
        Objects.requireNonNull(this.ancestor);
        return this.ancestor;
    }
}
