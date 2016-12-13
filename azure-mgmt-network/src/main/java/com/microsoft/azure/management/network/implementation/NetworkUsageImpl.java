/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.NetworkUsage;
import com.microsoft.azure.management.network.NetworkUsageUnit;
import com.microsoft.azure.management.network.UsageName;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

/**
 * The implementation of {@link NetworkUsage}.
 */
class NetworkUsageImpl extends WrapperImpl<UsageInner> implements NetworkUsage {
    NetworkUsageImpl(UsageInner innerObject) {
        super(innerObject);
    }

    @Override
    public NetworkUsageUnit unit() {
        return new NetworkUsageUnit(inner().unit());
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

