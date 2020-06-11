// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.sql.fluent.inner.SyncFullSchemaPropertiesInner;
import java.time.OffsetDateTime;
import java.util.List;

/** An immutable client-side representation of an Azure SQL Server Sync Group. */
@Fluent
public interface SqlSyncFullSchemaProperty extends HasInner<SyncFullSchemaPropertiesInner> {

    /** @return the list of tables in the database full schema. */
    List<SyncFullSchemaTable> tables();

    /** @return last update time of the database schema. */
    OffsetDateTime lastUpdateTime();
}
