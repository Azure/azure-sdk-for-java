// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.SqlSyncGroupLogProperty;
import com.azure.resourcemanager.sql.models.SyncGroupLogType;
import com.azure.resourcemanager.sql.fluent.inner.SyncGroupLogPropertiesInner;
import java.time.OffsetDateTime;

/** Implementation for SqlSyncGroupLogProperty. */
public class SqlSyncGroupLogPropertyImpl extends WrapperImpl<SyncGroupLogPropertiesInner>
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
