// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.fluent.models.AccessKeysInner;
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
        return this.innerModel().primaryKey();
    }

    @Override
    public String secondaryKey() {
        return this.innerModel().secondaryKey();
    }

    @Override
    public String primaryConnectionString() {
        return this.innerModel().primaryConnectionString();
    }

    @Override
    public String secondaryConnectionString() {
        return this.innerModel().secondaryConnectionString();
    }

    @Override
    public String aliasPrimaryConnectionString() {
        return this.innerModel().aliasPrimaryConnectionString();
    }

    @Override
    public String aliasSecondaryConnectionString() {
        return this.innerModel().aliasSecondaryConnectionString();
    }
}
