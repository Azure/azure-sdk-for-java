// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.servicebus.fluent.models.AccessKeysInner;
import com.azure.resourcemanager.servicebus.models.AuthorizationKeys;

/**
 * Implementation for AuthorizationKeys.
 */
class AuthorizationKeysImpl
        extends WrapperImpl<AccessKeysInner>
        implements AuthorizationKeys {

    AuthorizationKeysImpl(AccessKeysInner inner) {
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
