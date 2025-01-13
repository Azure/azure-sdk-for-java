// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.sql.fluent.models.SyncGroupLogPropertiesInner;
import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure SQL Server Sync Group. */
@Fluent
public interface SqlSyncGroupLogProperty extends HasInnerModel<SyncGroupLogPropertiesInner> {

    /**
     * Gets timestamp of the sync group log.
     *
     * @return timestamp of the sync group log
     */
    OffsetDateTime timestamp();

    /**
     * Gets the type of the sync group log.
     *
     * @return the type of the sync group log
     */
    SyncGroupLogType type();

    /**
     * Gets the source of the sync group log.
     *
     * @return the source of the sync group log.
     */
    String source();

    /**
     * Gets the details of the sync group log.
     *
     * @return the details of the sync group log.
     */
    String details();

    /**
     * Gets the tracing ID of the sync group log.
     *
     * @return the tracing ID of the sync group log.
     */
    String tracingId();

    /**
     * Gets operation status of the sync group log.
     *
     * @return operation status of the sync group log.
     */
    String operationStatus();
}
