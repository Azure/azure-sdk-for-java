/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.models.HasId;
import com.azure.management.resources.fluentcore.arm.models.HasName;
import com.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.sql.models.RestorableDroppedDatabaseInner;

import java.time.OffsetDateTime;


/**
 * Response containing Azure SQL restorable dropped database.
 */
@Fluent
public interface SqlRestorableDroppedDatabase extends
        Refreshable<SqlRestorableDroppedDatabase>,
        HasInner<RestorableDroppedDatabaseInner>,
        HasResourceGroup,
        HasName,
        HasId {

    /**
     * @return the geo-location where the resource lives
     */
    Region region();

    /**
     * @return the name of the database
     */
    String databaseName();

    /**
     * @return the edition of the database
     */
    String edition();

    /**
     * @return the max size in bytes of the database
     */
    String maxSizeBytes();

    /**
     * @return the service level objective name of the database
     */
    String serviceLevelObjective();

    /**
     * @return the elastic pool name of the database
     */
    String elasticPoolName();

    /**
     * @return the creation date of the database (ISO8601 format)
     */
    OffsetDateTime creationDate();

    /**
     * @return the deletion date of the database (ISO8601 format)
     */
    OffsetDateTime deletionDate();

    /**
     * @return the earliest restore date of the database (ISO8601 format)
     */
    OffsetDateTime earliestRestoreDate();
}
