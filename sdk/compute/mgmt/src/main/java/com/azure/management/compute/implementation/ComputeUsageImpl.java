/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.management.compute.ComputeUsage;
import com.azure.management.compute.ComputeUsageUnit;
import com.azure.management.compute.UsageName;
import com.azure.management.compute.models.UsageInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The implementation of ComputeUsage.
 */
class ComputeUsageImpl extends WrapperImpl<UsageInner> implements ComputeUsage {
    ComputeUsageImpl(UsageInner innerObject) {
        super(innerObject);
    }

    @Override
    public ComputeUsageUnit unit() {
        return ComputeUsageUnit.fromString(inner().unit());
    }

    @Override
    public int currentValue() {
        return inner().currentValue();
    }

    @Override
    public long limit() {
        return inner().limit();
    }

    @Override
    public UsageName name() {
        return inner().name();
    }
}

