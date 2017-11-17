/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.storage.StorageUsage;
import com.microsoft.azure.management.storage.UsageName;
import com.microsoft.azure.management.storage.UsageUnit;

/**
 * The implementation of {@link UsageInner}.
 */
@LangDefinition
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
        return Utils.toPrimitiveInt(inner().currentValue());
    }

    @Override
    public int limit() {
        return Utils.toPrimitiveInt(inner().limit());
    }

    @Override
    public UsageName name() {
        return inner().name();
    }
}
