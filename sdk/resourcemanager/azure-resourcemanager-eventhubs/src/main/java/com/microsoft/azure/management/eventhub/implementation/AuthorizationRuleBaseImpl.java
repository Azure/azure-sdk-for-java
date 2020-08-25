/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.AccessRights;
import com.microsoft.azure.management.eventhub.AuthorizationRule;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationKey;
import com.microsoft.azure.management.eventhub.KeyType;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import rx.Observable;
import rx.functions.Func1;

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
@LangDefinition
abstract class AuthorizationRuleBaseImpl<RuleT extends AuthorizationRule<RuleT>, RuleImpl extends IndexableRefreshableWrapperImpl<RuleT, AuthorizationRuleInner>>
        extends NestedResourceImpl<RuleT, AuthorizationRuleInner, RuleImpl> implements AuthorizationRule<RuleT> {

    protected AuthorizationRuleBaseImpl(String name, AuthorizationRuleInner inner, EventHubManager manager) {
        super(name, inner, manager);
    }

    @Override
    public Observable<EventHubAuthorizationKey> getKeysAsync() {
        return this.getKeysInnerAsync()
                .map(new Func1<AccessKeysInner, EventHubAuthorizationKey>() {
                    @Override
                    public EventHubAuthorizationKey call(AccessKeysInner inner) {
                        return new EventHubAuthorizationKeyImpl(inner);
                    }
                });
    }

    @Override
    public EventHubAuthorizationKey getKeys() {
        return getKeysAsync().toBlocking().last();
    }

    @Override
    public Observable<EventHubAuthorizationKey> regenerateKeyAsync(KeyType keyType) {
        return this.regenerateKeysInnerAsync(keyType)
                .map(new Func1<AccessKeysInner, EventHubAuthorizationKey>() {
                    @Override
                    public EventHubAuthorizationKey call(AccessKeysInner inner) {
                        return new EventHubAuthorizationKeyImpl(inner);
                    }
                });
    }

    @Override
    public EventHubAuthorizationKey regenerateKey(KeyType keyType) {
        return regenerateKeyAsync(keyType).toBlocking().last();
    }

    @Override
    public List<AccessRights> rights() {
        if (this.inner().rights() == null) {
            return Collections.unmodifiableList(new ArrayList<AccessRights>());
        }
        return Collections.unmodifiableList(this.inner().rights());
    }


    @SuppressWarnings("unchecked")
    public RuleImpl withListenAccess() {
        if (this.inner().rights() == null) {
            this.inner().withRights(new ArrayList<AccessRights>());
        }
        if (!this.inner().rights().contains(AccessRights.LISTEN)) {
            this.inner().rights().add(AccessRights.LISTEN);
        }
        return (RuleImpl) this;
    }

    @SuppressWarnings("unchecked")
    public RuleImpl withSendAccess() {
        if (this.inner().rights() == null) {
            this.inner().withRights(new ArrayList<AccessRights>());
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

    protected abstract Observable<AccessKeysInner> getKeysInnerAsync();
    protected abstract Observable<AccessKeysInner> regenerateKeysInnerAsync(KeyType keyType);
    protected abstract Observable<AuthorizationRuleInner> getInnerAsync();
    public abstract Observable<RuleT> createResourceAsync();
}
