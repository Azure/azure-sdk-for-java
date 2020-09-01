/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.resourcemanager.servicebus.models.AuthorizationKeys;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation for AuthorizationKeys.
 */
@LangDefinition
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
