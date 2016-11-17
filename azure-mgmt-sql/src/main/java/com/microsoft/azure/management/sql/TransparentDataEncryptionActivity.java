/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.TransparentDataEncryptionActivityInner;


/**
 * An immutable client-side representation of an Azure SQL database's TransparentDataEncryptionActivity.
 */
@Fluent
public interface TransparentDataEncryptionActivity extends
        Wrapper<TransparentDataEncryptionActivityInner>,
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
    TransparentDataEncryptionActivityStates status();

    /**
     * @return the percent complete of the transparent data encryption scan for a
     * Azure SQL Database.
     */
    double percentComplete();
}

