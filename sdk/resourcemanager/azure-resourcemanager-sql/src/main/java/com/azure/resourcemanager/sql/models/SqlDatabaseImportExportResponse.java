// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.sql.fluent.inner.ImportExportResponseInner;

/** Response containing result of the Azure SQL Database import or export operation. */
@Fluent
public interface SqlDatabaseImportExportResponse
    extends Indexable, HasInner<ImportExportResponseInner>, HasId, HasName {
    /** @return the request type of the operation */
    String requestType();

    /** @return the UUID of the operation */
    String requestId();

    /** @return the name of the server */
    String serverName();

    /** @return the name of the database */
    String databaseName();

    /** @return the status message returned from the server */
    String status();

    /** @return the operation status last modified time */
    String lastModifiedTime();

    /** @return the operation queued time */
    String queuedTime();

    /** @return the blob uri */
    String blobUri();

    /** @return the error message returned from the server */
    String errorMessage();
}
