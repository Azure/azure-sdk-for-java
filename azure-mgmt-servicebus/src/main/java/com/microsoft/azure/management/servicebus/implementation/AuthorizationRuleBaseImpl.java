/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.servicebus.AccessRights;
import com.microsoft.azure.management.servicebus.AuthorizationKeys;
import com.microsoft.azure.management.servicebus.Policykey;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base type for various entity specific authorization rules.
 */
@LangDefinition
abstract class AuthorizationRuleBaseImpl<
        FluentModelT extends IndependentChildResource<ManagerT, InnerModelT>,
        FluentParentModelT extends Resource & HasResourceGroup,
        InnerModelT extends SharedAccessAuthorizationRuleInner,
        FluentModelImplT extends IndependentChildResourceImpl<FluentModelT, FluentParentModelT, InnerModelT, FluentModelImplT, ManagerT>,
        ManagerT> extends IndependentChildResourceImpl<FluentModelT,
        FluentParentModelT,
        InnerModelT,
        FluentModelImplT,
        ManagerT> {
    protected AuthorizationRuleBaseImpl(String name, InnerModelT innerObject, ManagerT manager) {
        super(name, innerObject, manager);
    }

    /**
     * @return stream that emits primary, secondary keys and connection strings
     */
    public Observable<AuthorizationKeys> getKeysAsync() {
        return this.getKeysInnerAsync()
                .map(new Func1<ResourceListKeysInner, AuthorizationKeys>() {
                    @Override
                    public AuthorizationKeys call(ResourceListKeysInner inner) {
                        return new AuthorizationKeysImpl(inner);
                    }
                });
    }

    /**
     * @return primary, secondary keys and connection strings
     */
    public AuthorizationKeys getKeys() {
        return getKeysAsync().toBlocking().last();
    }

    /**
     * Regenerates primary or secondary keys.
     *
     * @param policykey the key to regenerate
     * @return stream that emits primary, secondary keys and connection strings.
     */
    public Observable<AuthorizationKeys> regenerateKeyAsync(Policykey policykey) {
        return this.regenerateKeysInnerAsync(policykey)
                .map(new Func1<ResourceListKeysInner, AuthorizationKeys>() {
                    @Override
                    public AuthorizationKeys call(ResourceListKeysInner inner) {
                        return new AuthorizationKeysImpl(inner);
                    }
                });
    }

    /**
     * Regenerates primary or secondary keys.
     *
     * @param policykey the key to regenerate
     * @return primary, secondary keys and connection strings.
     */
    public AuthorizationKeys regenerateKey(Policykey policykey) {
        return regenerateKeyAsync(policykey).toBlocking().last();
    }

    public List<AccessRights> rights() {
        if (this.inner().rights() == null) {
            return Collections.unmodifiableList(new ArrayList<AccessRights>());
        }
        return Collections.unmodifiableList(this.inner().rights());
    }

    @SuppressWarnings("unchecked")
    public FluentModelImplT withAccessRight(AccessRights rights) {
        if (rights == null) {
            return (FluentModelImplT) this;
        }
        if (this.inner().rights() == null) {
            this.inner().withRights(new ArrayList<AccessRights>());
        }
        if (!this.inner().rights().contains(rights)) {
            if (rights.equals(AccessRights.MANAGE)) {
                // Manage permission should also include Send and Listen.
                //
                if (!this.inner().rights().contains(AccessRights.LISTEN)) {
                    this.inner().rights().add(AccessRights.LISTEN);
                }
                if (!this.inner().rights().contains(AccessRights.SEND)) {
                    this.inner().rights().add(AccessRights.SEND);
                }
            }
            this.inner().rights().add(rights);
        }
        return (FluentModelImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentModelImplT withAccessRights(AccessRights... rights) {
        if (rights == null) {
            return (FluentModelImplT) this;
        }
        for (AccessRights r : rights) {
            withAccessRight(r);
        }
        return (FluentModelImplT) this;
    }

    @SuppressWarnings("unchecked")
    public FluentModelImplT withoutAccessRight(AccessRights rights) {
        if (this.inner().rights() != null
                && this.inner().rights().contains(rights)) {
            this.inner().rights().remove(rights);
        }
        return (FluentModelImplT) this;
    }

    protected abstract Observable<ResourceListKeysInner> getKeysInnerAsync();
    protected abstract Observable<ResourceListKeysInner> regenerateKeysInnerAsync(Policykey policykey);
}