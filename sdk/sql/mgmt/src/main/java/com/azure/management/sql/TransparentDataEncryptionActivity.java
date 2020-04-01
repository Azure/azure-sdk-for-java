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
import com.azure.management.sql.models.TransparentDataEncryptionActivityInner;


/**
 * An immutable client-side representation of an Azure SQL database's TransparentDataEncryptionActivity.
 */
@Fluent
public interface TransparentDataEncryptionActivity extends
        HasInner<TransparentDataEncryptionActivityInner>,
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
     * @return the status transparent data encryption of the Azure SQL Database
     */
    TransparentDataEncryptionActivityStatus status();

    /**
     * @return the percent complete of the transparent data encryption scan for a
     * Azure SQL Database.
     */
    double percentComplete();
}

