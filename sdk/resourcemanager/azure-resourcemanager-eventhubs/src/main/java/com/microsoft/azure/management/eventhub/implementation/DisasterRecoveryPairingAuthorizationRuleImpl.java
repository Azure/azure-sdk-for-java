/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.AccessRights;
import com.microsoft.azure.management.eventhub.DisasterRecoveryPairingAuthorizationKey;
import com.microsoft.azure.management.eventhub.DisasterRecoveryPairingAuthorizationRule;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.Objects;

/**
 * Implementation for {@link DisasterRecoveryPairingAuthorizationRule}.
 */
@LangDefinition
class DisasterRecoveryPairingAuthorizationRuleImpl
        extends WrapperImpl<AuthorizationRuleInner>
        implements DisasterRecoveryPairingAuthorizationRule {

    private final EventHubManager manager;
    private final Ancestors.TwoAncestor ancestor;

    protected DisasterRecoveryPairingAuthorizationRuleImpl(AuthorizationRuleInner inner, EventHubManager manager) {
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
    public Observable<DisasterRecoveryPairingAuthorizationKey> getKeysAsync() {
        return this.manager.inner().disasterRecoveryConfigs()
                .listKeysAsync(this.ancestor().resourceGroupName(), this.ancestor.ancestor2Name(), this.ancestor().ancestor1Name(), this.name())
                .map(new Func1<AccessKeysInner, DisasterRecoveryPairingAuthorizationKey>() {
                    @Override
                    public DisasterRecoveryPairingAuthorizationKey call(AccessKeysInner accessKeysInner) {
                        return new DisasterRecoveryPairingAuthorizationKeyImpl(accessKeysInner);
                    }
                });
    }

    @Override
    public DisasterRecoveryPairingAuthorizationKey getKeys() {
        return this.getKeysAsync().toBlocking().last();
    }

    @Override
    public EventHubManager manager() {
        return this.manager;
    }

    private Ancestors.TwoAncestor ancestor() {
        Objects.requireNonNull(this.ancestor);
        return this.ancestor;
    }
}
