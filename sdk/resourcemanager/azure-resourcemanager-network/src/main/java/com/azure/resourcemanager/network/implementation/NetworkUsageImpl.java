// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.NetworkUsage;
import com.azure.resourcemanager.network.models.UsageName;
import com.azure.resourcemanager.network.fluent.inner.UsageInner;
import com.azure.resourcemanager.network.models.UsageUnit;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;

/** The implementation of {@link NetworkUsage}. */
class NetworkUsageImpl extends WrapperImpl<UsageInner> implements NetworkUsage {
    NetworkUsageImpl(UsageInner innerObject) {
        super(innerObject);
    }

    @Override
    public UsageUnit unit() {
        return inner().unit();
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
