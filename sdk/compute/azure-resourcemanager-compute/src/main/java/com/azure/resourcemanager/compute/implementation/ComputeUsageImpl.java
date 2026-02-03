// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.ComputeUsage;
import com.azure.resourcemanager.compute.models.ComputeUsageUnit;
import com.azure.resourcemanager.compute.models.UsageName;
import com.azure.resourcemanager.compute.fluent.models.UsageInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** The implementation of ComputeUsage. */
class ComputeUsageImpl extends WrapperImpl<UsageInner> implements ComputeUsage {
    ComputeUsageImpl(UsageInner innerObject) {
        super(innerObject);
    }

    @Override
    public ComputeUsageUnit unit() {
        return ComputeUsageUnit.fromString(innerModel().unit());
    }

    @Override
    public int currentValue() {
        return innerModel().currentValue();
    }

    @Override
    public long limit() {
        return innerModel().limit();
    }

    @Override
    public UsageName name() {
        return innerModel().name();
    }
}
