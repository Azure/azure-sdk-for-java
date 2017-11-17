/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.ComputeUsage;
import com.microsoft.azure.management.compute.ComputeUsageUnit;
import com.microsoft.azure.management.compute.UsageName;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The implementation of ComputeUsage.
 */
@LangDefinition
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

