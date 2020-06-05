// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkUsage;
import com.azure.resourcemanager.network.NetworkUsageUnit;
import com.azure.resourcemanager.network.UsageName;
import com.azure.resourcemanager.network.models.UsageInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;

/** The implementation of {@link NetworkUsage}. */
class NetworkUsageImpl extends WrapperImpl<UsageInner> implements NetworkUsage {
    NetworkUsageImpl(UsageInner innerObject) {
        super(innerObject);
    }

    @Override
    public NetworkUsageUnit unit() {
        return NetworkUsageUnit.fromString(inner().unit());
    }

    @Override
    public long currentValue() {
        return Utils.toPrimitiveLong(inner().currentValue());
    }

    @Override
    public long limit() {
        return Utils.toPrimitiveLong(inner().limit());
    }

    @Override
    public UsageName name() {
        return inner().name();
    }
}
