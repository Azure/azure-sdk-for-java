package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.storage.Usage;
import com.microsoft.azure.management.storage.implementation.api.UsageName;
import com.microsoft.azure.management.storage.implementation.api.UsageUnit;

/**
 * The implementation of Usage and its parent interfaces.
 */
class UsageImpl extends WrapperImpl<com.microsoft.azure.management.storage.implementation.api.Usage> implements Usage {
    UsageImpl(com.microsoft.azure.management.storage.implementation.api.Usage innerObject) {
        super(innerObject);
    }

    @Override
    public UsageUnit unit() {
        return inner().unit();
    }

    @Override
    public int currentValue() {
        return inner().currentValue();
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
