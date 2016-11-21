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
import com.microsoft.azure.management.sql.implementation.TransparentDataEncryptionInner;

import java.util.List;


/**
 * An immutable client-side representation of an Azure SQL database's TransparentDataEncryption.
 */
@Fluent
public interface TransparentDataEncryption extends
        Wrapper<TransparentDataEncryptionInner>,
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
     * @return the status of the Azure SQL Database Transparent Data Encryption
     */
    TransparentDataEncryptionStates status();

    /**
     * Updates the state of the transparent data encryption status.
     *
     * @param transparentDataEncryptionState state of the data encryption to set
     * @return the new encryption settings after modifyState
     */
    TransparentDataEncryption updateStatus(TransparentDataEncryptionStates transparentDataEncryptionState);

    /**
     * @return an Azure SQL Database Transparent Data Encryption Activities
     */
    List<TransparentDataEncryptionActivity> listActivities();
}

