// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.UsageInner;
import com.azure.resourcemanager.cdn.models.Usage;
import com.azure.resourcemanager.cdn.models.UsageName;
import com.azure.resourcemanager.cdn.models.UsageUnit;

/**
 * Default implementation of {@link Usage} backed by {@link UsageInner}.
 */
final class UsageImpl implements Usage {
    private final UsageInner inner;

    UsageImpl(UsageInner inner) {
        this.inner = inner;
    }

    @Override
    public String id() {
        return this.inner.id();
    }

    @Override
    public UsageUnit unit() {
        return this.inner.unit();
    }

    @Override
    public long currentValue() {
        return this.inner.currentValue();
    }

    @Override
    public long limit() {
        return this.inner.limit();
    }

    @Override
    public UsageName name() {
        return this.inner.name();
    }
}
