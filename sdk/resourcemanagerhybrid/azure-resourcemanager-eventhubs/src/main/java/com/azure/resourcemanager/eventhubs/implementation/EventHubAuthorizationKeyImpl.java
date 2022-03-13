// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.resourcemanager.eventhubs.fluent.models.AccessKeysInner;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationKey;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation for AuthorizationKeys.
 */
class EventHubAuthorizationKeyImpl
        extends WrapperImpl<AccessKeysInner>
        implements EventHubAuthorizationKey {

    EventHubAuthorizationKeyImpl(AccessKeysInner inner) {
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
}
