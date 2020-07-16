// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.SqlSyncFullSchemaProperty;
import com.azure.resourcemanager.sql.models.SyncFullSchemaTable;
import com.azure.resourcemanager.sql.fluent.inner.SyncFullSchemaPropertiesInner;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementation for SqlSyncGroup. */
public class SqlSyncFullSchemaPropertyImpl extends WrapperImpl<SyncFullSchemaPropertiesInner>
    implements SqlSyncFullSchemaProperty {

    protected SqlSyncFullSchemaPropertyImpl(SyncFullSchemaPropertiesInner innerObject) {
        super(innerObject);
    }

    @Override
    public List<SyncFullSchemaTable> tables() {
        return Collections
            .unmodifiableList(
                this.inner().tables() != null ? this.inner().tables() : new ArrayList<SyncFullSchemaTable>());
    }

    @Override
    public OffsetDateTime lastUpdateTime() {
        return this.inner().lastUpdateTime();
    }
}
