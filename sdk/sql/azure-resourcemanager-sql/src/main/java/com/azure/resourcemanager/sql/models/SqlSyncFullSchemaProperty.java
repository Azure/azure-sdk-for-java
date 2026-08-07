// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.sql.fluent.models.SyncFullSchemaPropertiesInner;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * An immutable client-side representation of an Azure SQL Server Sync Group schema.
 *
 * @deprecated Azure SQL Data Sync is scheduled for retirement and requires SQL authentication; it doesn't support
 * Microsoft Entra ID or managed identities. For distributed applications, use
 * {@link SqlDatabase.DefinitionStages.WithSourceDatabaseId#withSourceDatabase(String)} to create a database copy. For
 * globally distributed applications, additionally use
 * {@link SqlDatabase.DefinitionStages.WithCreateMode#withMode(CreateMode)} with {@link CreateMode#ONLINE_SECONDARY} for
 * active geo-replication. Refer to the
 * <a href="https://learn.microsoft.com/azure/azure-sql/database/sql-data-sync-retirement-migration">official retirement
 * migration guidance</a> for more alternatives.
 */
@Deprecated
@Fluent
public interface SqlSyncFullSchemaProperty extends HasInnerModel<SyncFullSchemaPropertiesInner> {

    /**
     * Gets the list of tables in the database full schema.
     *
     * @return the list of tables in the database full schema.
     */
    List<SyncFullSchemaTable> tables();

    /**
     * Gets last update time of the database schema.
     *
     * @return last update time of the database schema.
     */
    OffsetDateTime lastUpdateTime();
}
