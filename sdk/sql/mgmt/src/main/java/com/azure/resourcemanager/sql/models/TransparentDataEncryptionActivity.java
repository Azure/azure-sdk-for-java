// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.sql.fluent.inner.TransparentDataEncryptionActivityInner;

/** An immutable client-side representation of an Azure SQL database's TransparentDataEncryptionActivity. */
@Fluent
public interface TransparentDataEncryptionActivity
    extends HasInner<TransparentDataEncryptionActivityInner>, HasResourceGroup, HasName, HasId {
    /** @return name of the SQL Server to which this replication belongs */
    String sqlServerName();

    /** @return name of the SQL Database to which this replication belongs */
    String databaseName();

    /** @return the status transparent data encryption of the Azure SQL Database */
    TransparentDataEncryptionActivityStatus status();

    /** @return the percent complete of the transparent data encryption scan for a Azure SQL Database. */
    double percentComplete();
}
