/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.sql.models.SyncFullSchemaPropertiesInner;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * An immutable client-side representation of an Azure SQL Server Sync Group.
 */
@Fluent
public interface SqlSyncFullSchemaProperty
    extends HasInner<SyncFullSchemaPropertiesInner> {

    /**
     * @return the list of tables in the database full schema.
     */
    List<SyncFullSchemaTable> tables();

    /**
     * @return last update time of the database schema.
     */
    OffsetDateTime lastUpdateTime();
}
