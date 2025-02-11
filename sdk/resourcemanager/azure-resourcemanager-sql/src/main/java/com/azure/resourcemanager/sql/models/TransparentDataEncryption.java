// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.sql.fluent.models.LogicalDatabaseTransparentDataEncryptionInner;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure SQL database's TransparentDataEncryption. */
@Fluent
public interface TransparentDataEncryption extends Refreshable<TransparentDataEncryption>,
    HasInnerModel<LogicalDatabaseTransparentDataEncryptionInner>, HasResourceGroup, HasName, HasId {
    /**
     * Gets the name of the SQL Server to which this replication belongs.
     *
     * @return name of the SQL Server to which this replication belongs
     */
    String sqlServerName();

    /**
     * Gets the name of the SQL Database to which this replication belongs.
     *
     * @return name of the SQL Database to which this replication belongs
     */
    String databaseName();

    /**
     * Gets the status of the Azure SQL Database Transparent Data Encryption.
     *
     * @return the status of the Azure SQL Database Transparent Data Encryption
     */
    TransparentDataEncryptionState status();

    /**
     * Updates the state of the transparent data encryption status.
     *
     * @param transparentDataEncryptionState state of the data encryption to set
     * @return the new encryption settings after the update operation
     */
    TransparentDataEncryption updateStatus(TransparentDataEncryptionState transparentDataEncryptionState);

    /**
     * Updates the state of the transparent data encryption status.
     *
     * @param transparentDataEncryptionState state of the data encryption to set
     * @return a representation of the deferred computation of the new encryption settings after the update operation
     */
    Mono<TransparentDataEncryption> updateStatusAsync(TransparentDataEncryptionState transparentDataEncryptionState);
}
