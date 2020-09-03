// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.inner.AccessKeysInner;
import com.azure.resourcemanager.eventhubs.fluent.inner.AuthorizationRuleInner;
import com.azure.resourcemanager.eventhubs.models.AccessRights;
import com.azure.resourcemanager.eventhubs.models.AuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationKey;
import com.azure.resourcemanager.eventhubs.models.KeyType;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Base implementation for authorization rule.
 * (Internal use only)
 *
 * @param <RuleT> rule fluent model
 * @param <RuleImpl> implementation of rule fluent model
 */
abstract class AuthorizationRuleBaseImpl<RuleT extends AuthorizationRule<RuleT>,
    RuleImpl extends IndexableRefreshableWrapperImpl<RuleT, AuthorizationRuleInner>>
    extends NestedResourceImpl<RuleT, AuthorizationRuleInner, RuleImpl> implements AuthorizationRule<RuleT> {

    protected AuthorizationRuleBaseImpl(String name, AuthorizationRuleInner inner, EventHubsManager manager) {
        super(name, inner, manager);
    }

    @Override
    public Mono<EventHubAuthorizationKey> getKeysAsync() {
        return this.getKeysInnerAsync()
            .map(EventHubAuthorizationKeyImpl::new);
    }

    @Override
    public EventHubAuthorizationKey getKeys() {
        return getKeysAsync().block();
    }

    @Override
    public Mono<EventHubAuthorizationKey> regenerateKeyAsync(KeyType keyType) {
        return this.regenerateKeysInnerAsync(keyType)
            .map(EventHubAuthorizationKeyImpl::new);
    }

    @Override
    public EventHubAuthorizationKey regenerateKey(KeyType keyType) {
        return regenerateKeyAsync(keyType).block();
    }

    @Override
    public List<AccessRights> rights() {
        if (this.inner().rights() == null) {
            return Collections.unmodifiableList(new ArrayList<>());
        }
        return Collections.unmodifiableList(this.inner().rights());
    }


    @SuppressWarnings("unchecked")
    public RuleImpl withListenAccess() {
        if (this.inner().rights() == null) {
            this.inner().withRights(new ArrayList<>());
        }
        if (!this.inner().rights().contains(AccessRights.LISTEN)) {
            this.inner().rights().add(AccessRights.LISTEN);
        }
        return (RuleImpl) this;
    }

    @SuppressWarnings("unchecked")
    public RuleImpl withSendAccess() {
        if (this.inner().rights() == null) {
            this.inner().withRights(new ArrayList<>());
        }
        if (!this.inner().rights().contains(AccessRights.SEND)) {
            this.inner().rights().add(AccessRights.SEND);
        }
        return (RuleImpl) this;
    }

    @SuppressWarnings("unchecked")
    public RuleImpl withSendAndListenAccess() {
        withListenAccess();
        withSendAccess();
        return (RuleImpl) this;
    }

    @SuppressWarnings("unchecked")
    public RuleImpl withManageAccess() {
        withListenAccess();
        withSendAccess();
        if (!this.inner().rights().contains(AccessRights.MANAGE)) {
            this.inner().rights().add(AccessRights.MANAGE);
        }
        return (RuleImpl) this;
    }

    protected abstract Mono<AccessKeysInner> getKeysInnerAsync();
    protected abstract Mono<AccessKeysInner> regenerateKeysInnerAsync(KeyType keyType);
    protected abstract Mono<AuthorizationRuleInner> getInnerAsync();
    public abstract Mono<RuleT> createResourceAsync();
}
