// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.sql.fluent.models.RestorableDroppedDatabaseInner;

import java.time.OffsetDateTime;

/** Response containing Azure SQL restorable dropped database. */
@Fluent
public interface SqlRestorableDroppedDatabase extends Refreshable<SqlRestorableDroppedDatabase>,
    HasInnerModel<RestorableDroppedDatabaseInner>, HasResourceGroup, HasName, HasId {

    /**
     * Gets the geo-location where the resource lives.
     *
     * @return the geo-location where the resource lives
     */
    Region region();

    /**
     * Gets the name of the database.
     *
     * @return the name of the database
     */
    String databaseName();

    /**
     * Gets the edition of the database.
     *
     * @return the edition of the database
     */
    String edition();

    /**
     * Gets the max size in bytes of the database.
     *
     * @return the max size in bytes of the database
     */
    String maxSizeBytes();

    /**
     * Gets the creation date of the database.
     *
     * @return the creation date of the database (ISO8601 format)
     */
    OffsetDateTime creationDate();

    /**
     * Gets the deletion date of the database.
     *
     * @return the deletion date of the database (ISO8601 format)
     */
    OffsetDateTime deletionDate();

    /**
     * Gets the earliest restore date of the database.
     *
     * @return the earliest restore date of the database (ISO8601 format)
     */
    OffsetDateTime earliestRestoreDate();
}
