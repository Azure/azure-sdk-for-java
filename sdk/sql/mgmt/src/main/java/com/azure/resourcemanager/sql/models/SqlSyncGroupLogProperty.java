// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.sql.fluent.inner.SyncGroupLogPropertiesInner;
import java.time.OffsetDateTime;

/** An immutable client-side representation of an Azure SQL Server Sync Group. */
@Fluent
public interface SqlSyncGroupLogProperty extends HasInner<SyncGroupLogPropertiesInner> {

    /** @return timestamp of the sync group log */
    OffsetDateTime timestamp();

    /** @return the type of the sync group log */
    SyncGroupLogType type();

    /** @return the source of the sync group log. */
    String source();

    /** @return the details of the sync group log. */
    String details();

    /** @return the tracing ID of the sync group log. */
    String tracingId();

    /** @return operation status of the sync group log. */
    String operationStatus();
}
