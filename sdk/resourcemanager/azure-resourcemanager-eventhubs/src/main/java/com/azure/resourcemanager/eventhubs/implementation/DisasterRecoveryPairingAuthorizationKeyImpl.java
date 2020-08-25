// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.fluent.inner.AccessKeysInner;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationKey;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation for {@link DisasterRecoveryPairingAuthorizationKey}.
 */
class DisasterRecoveryPairingAuthorizationKeyImpl
        extends WrapperImpl<AccessKeysInner>
        implements DisasterRecoveryPairingAuthorizationKey {

    DisasterRecoveryPairingAuthorizationKeyImpl(AccessKeysInner inner) {
        super(inner);
    }

    @Override
    public String primaryKey() {
        return this.inner().primaryKey();
    }

    @Override
    public String secondaryKey() {
        return this.inner().secondaryKey();
    }

    @Override
    public String primaryConnectionString() {
        return this.inner().primaryConnectionString();
    }

    @Override
    public String secondaryConnectionString() {
        return this.inner().secondaryConnectionString();
    }

    @Override
    public String aliasPrimaryConnectionString() {
        return this.inner().aliasPrimaryConnectionString();
    }

    @Override
    public String aliasSecondaryConnectionString() {
        return this.inner().aliasSecondaryConnectionString();
    }
}
