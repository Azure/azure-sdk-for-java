// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.NetworkUsage;
import com.azure.resourcemanager.network.models.UsageName;
import com.azure.resourcemanager.network.fluent.models.UsageInner;
import com.azure.resourcemanager.network.models.UsageUnit;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

/** The implementation of {@link NetworkUsage}. */
class NetworkUsageImpl extends WrapperImpl<UsageInner> implements NetworkUsage {
    NetworkUsageImpl(UsageInner innerObject) {
        super(innerObject);
    }

    @Override
    public UsageUnit unit() {
        return innerModel().unit();
    }

    @Override
    public long currentValue() {
        return ResourceManagerUtils.toPrimitiveLong(innerModel().currentValue());
    }

    @Override
    public long limit() {
        return ResourceManagerUtils.toPrimitiveLong(innerModel().limit());
    }

    @Override
    public UsageName name() {
        return innerModel().name();
    }
}
