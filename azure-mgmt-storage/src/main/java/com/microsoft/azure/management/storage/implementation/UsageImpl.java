package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.storage.StorageUsage;
import com.microsoft.azure.management.storage.UsageName;
import com.microsoft.azure.management.storage.UsageUnit;

/**
 * The implementation of {@link UsageInner}.
 */
class UsageImpl extends WrapperImpl<UsageInner> implements StorageUsage {
    UsageImpl(UsageInner innerObject) {
        super(innerObject);
    }

    @Override
    public UsageUnit unit() {
        return inner().unit();
    }

    @Override
    public int currentValue() {
        return  inner().currentValue();
    }

    @Override
    public int limit() {
        return inner().limit();
    }

    @Override
    public UsageName name() {
        return inner().name();
    }
}
