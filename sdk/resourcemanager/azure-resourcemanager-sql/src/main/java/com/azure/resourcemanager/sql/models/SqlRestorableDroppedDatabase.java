// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.sql.fluent.inner.RestorableDroppedDatabaseInner;
import java.time.OffsetDateTime;

/** Response containing Azure SQL restorable dropped database. */
@Fluent
public interface SqlRestorableDroppedDatabase
    extends Refreshable<SqlRestorableDroppedDatabase>,
        HasInner<RestorableDroppedDatabaseInner>,
        HasResourceGroup,
        HasName,
        HasId {

    /** @return the geo-location where the resource lives */
    Region region();

    /** @return the name of the database */
    String databaseName();

    /** @return the edition of the database */
    String edition();

    /** @return the max size in bytes of the database */
    String maxSizeBytes();

    /** @return the service level objective name of the database */
    String serviceLevelObjective();

    /** @return the elastic pool name of the database */
    String elasticPoolName();

    /** @return the creation date of the database (ISO8601 format) */
    OffsetDateTime creationDate();

    /** @return the deletion date of the database (ISO8601 format) */
    OffsetDateTime deletionDate();

    /** @return the earliest restore date of the database (ISO8601 format) */
    OffsetDateTime earliestRestoreDate();
}
