/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.sql.models.SyncGroupLogPropertiesInner;

import java.time.OffsetDateTime;

/**
 * An immutable client-side representation of an Azure SQL Server Sync Group.
 */
@Fluent
public interface SqlSyncGroupLogProperty
    extends HasInner<SyncGroupLogPropertiesInner> {

    /**
     * @return timestamp of the sync group log
     */
    OffsetDateTime timestamp();

    /**
     * @return the type of the sync group log
     */
    SyncGroupLogType type();

    /**
     * @return the source of the sync group log.
     */
    String source();

    /**
     * @return the details of the sync group log.
     */
    String details();

    /**
     * @return the tracing ID of the sync group log.
     */
    String tracingId();

    /**
     * @return operation status of the sync group log.
     */
    String operationStatus();
}
