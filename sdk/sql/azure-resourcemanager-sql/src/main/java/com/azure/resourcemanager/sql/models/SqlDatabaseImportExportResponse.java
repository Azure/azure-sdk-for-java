// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.sql.fluent.models.ImportExportOperationResultInner;

/** Response containing result of the Azure SQL Database import or export operation. */
@Fluent
public interface SqlDatabaseImportExportResponse
    extends Indexable, HasInnerModel<ImportExportOperationResultInner>, HasId, HasName {
    /**
     * Gets the request type of the operation.
     *
     * @return the request type of the operation
     */
    String requestType();

    /**
     * Gets the UUID of the operation.
     *
     * @return the UUID of the operation
     */
    String requestId();

    /**
     * Gets the name of the server.
     *
     * @return the name of the server
     */
    String serverName();

    /**
     * Gets the name of the database.
     *
     * @return the name of the database
     */
    String databaseName();

    /**
     * Gets the status message returned from the server.
     *
     * @return the status message returned from the server
     */
    String status();

    /**
     * Gets the operation status last modified time.
     *
     * @return the operation status last modified time
     */
    String lastModifiedTime();

    /**
     * Gets the operation queued time.
     *
     * @return the operation queued time
     */
    String queuedTime();

    /**
     * Gets the blob uri.
     *
     * @return the blob uri
     */
    String blobUri();

    /**
     * Gets the error message returned from the server.
     *
     * @return the error message returned from the server
     */
    String errorMessage();
}
