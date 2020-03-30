/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.HasId;
import com.azure.management.resources.fluentcore.arm.models.HasName;
import com.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.sql.models.RestorePointInner;

import java.time.OffsetDateTime;


/**
 * An immutable client-side representation of an Azure SQL database's Restore Point.
 */
@Fluent
public interface RestorePoint extends
        HasInner<RestorePointInner>,
        HasResourceGroup,
        HasName,
        HasId {
    /**
     * @return name of the SQL Server to which this replication belongs
     */
    String sqlServerName();

    /**
     * @return name of the SQL Database to which this replication belongs
     */
    String databaseName();

    /**
     * @return the ID of the SQL Database to which this replication belongs
     */
    String databaseId();

    /**
     * @return the restore point type of the Azure SQL Database restore point.
     */
    RestorePointType restorePointType();

    /**
     * @return restore point creation time (ISO8601 format). Populated when
     * restorePointType = CONTINUOUS. Null otherwise.
     */
    OffsetDateTime restorePointCreationDate();

    /**
     * @return earliest restore time (ISO8601 format). Populated when restorePointType
     * = DISCRETE. Null otherwise.
     */
    OffsetDateTime earliestRestoreDate();
}

