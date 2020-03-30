/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.SqlSyncGroupLogProperty;
import com.azure.management.sql.SyncGroupLogType;
import com.azure.management.sql.models.SyncGroupLogPropertiesInner;

import java.time.OffsetDateTime;

/**
 * Implementation for SqlSyncGroupLogProperty.
 */
public class SqlSyncGroupLogPropertyImpl
    extends WrapperImpl<SyncGroupLogPropertiesInner>
    implements SqlSyncGroupLogProperty {
    protected SqlSyncGroupLogPropertyImpl(SyncGroupLogPropertiesInner innerObject) {
        super(innerObject);
    }

    @Override
    public OffsetDateTime timestamp() {
        return this.inner().timestamp();
    }

    @Override
    public SyncGroupLogType type() {
        return this.inner().type();
    }

    @Override
    public String source() {
        return this.inner().source();
    }

    @Override
    public String details() {
        return this.inner().details();
    }

    @Override
    public String tracingId() {
        return this.inner().tracingId().toString();
    }

    @Override
    public String operationStatus() {
        return this.inner().operationStatus();
    }
}
