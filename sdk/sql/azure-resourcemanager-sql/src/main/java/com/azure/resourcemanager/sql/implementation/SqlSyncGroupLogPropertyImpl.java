// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.SqlSyncGroupLogProperty;
import com.azure.resourcemanager.sql.models.SyncGroupLogType;
import com.azure.resourcemanager.sql.fluent.models.SyncGroupLogPropertiesInner;
import java.time.OffsetDateTime;

/** Implementation for SqlSyncGroupLogProperty. */
public class SqlSyncGroupLogPropertyImpl extends WrapperImpl<SyncGroupLogPropertiesInner>
    implements SqlSyncGroupLogProperty {
    protected SqlSyncGroupLogPropertyImpl(SyncGroupLogPropertiesInner innerObject) {
        super(innerObject);
    }

    @Override
    public OffsetDateTime timestamp() {
        return this.innerModel().timestamp();
    }

    @Override
    public SyncGroupLogType type() {
        return this.innerModel().type();
    }

    @Override
    public String source() {
        return this.innerModel().source();
    }

    @Override
    public String details() {
        return this.innerModel().details();
    }

    @Override
    public String tracingId() {
        return this.innerModel().tracingId().toString();
    }

    @Override
    public String operationStatus() {
        return this.innerModel().operationStatus();
    }
}
