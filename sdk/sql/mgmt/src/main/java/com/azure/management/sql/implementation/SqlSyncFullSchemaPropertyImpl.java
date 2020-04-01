/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;


import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.sql.SqlSyncFullSchemaProperty;
import com.azure.management.sql.SyncFullSchemaTable;
import com.azure.management.sql.models.SyncFullSchemaPropertiesInner;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation for SqlSyncGroup.
 */
public class SqlSyncFullSchemaPropertyImpl
    extends
        WrapperImpl<SyncFullSchemaPropertiesInner>
    implements
        SqlSyncFullSchemaProperty {

    protected SqlSyncFullSchemaPropertyImpl(SyncFullSchemaPropertiesInner innerObject) {
        super(innerObject);
    }

    @Override
    public List<SyncFullSchemaTable> tables() {
        return Collections.unmodifiableList(this.inner().tables() != null ? this.inner().tables() : new ArrayList<SyncFullSchemaTable>());
    }

    @Override
    public OffsetDateTime lastUpdateTime() {
        return this.inner().lastUpdateTime();
    }
}
