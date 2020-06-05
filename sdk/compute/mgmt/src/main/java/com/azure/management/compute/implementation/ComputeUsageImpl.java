// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.compute.implementation;

import com.azure.management.compute.models.ComputeUsage;
import com.azure.management.compute.models.ComputeUsageUnit;
import com.azure.management.compute.models.UsageName;
import com.azure.management.compute.fluent.inner.UsageInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/** The implementation of ComputeUsage. */
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
