// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.search.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.search.fluent.models.AdminKeyResultInner;
import com.azure.resourcemanager.search.models.AdminKeys;

/**
 * Response containing the primary and secondary admin API keys for a given Azure Search service.
 */
class AdminKeysImpl extends WrapperImpl<AdminKeyResultInner> implements AdminKeys {

    AdminKeysImpl(AdminKeyResultInner inner) {
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
}
