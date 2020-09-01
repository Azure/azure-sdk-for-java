// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.servicebus.fluent.inner.ResourceListKeysInner;
import com.azure.resourcemanager.servicebus.models.AuthorizationKeys;

/**
 * Implementation for AuthorizationKeys.
 */
class AuthorizationKeysImpl
        extends WrapperImpl<ResourceListKeysInner>
        implements AuthorizationKeys {

    AuthorizationKeysImpl(ResourceListKeysInner inner) {
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
}
