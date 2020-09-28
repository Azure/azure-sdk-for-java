// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.sql.fluent.models.EncryptionProtectorInner;

/** An immutable client-side representation of an Azure SQL Encryption Protector. */
@Fluent
public interface SqlEncryptionProtector
    extends HasId,
        HasInnerModel<EncryptionProtectorInner>,
        HasResourceGroup,
        Indexable,
        Refreshable<SqlEncryptionProtector>,
        Updatable<SqlEncryptionProtector.Update> {

    /** @return name of the SQL Server to which this DNS alias belongs */
    String sqlServerName();

    /** @return the parent SQL server ID */
    String parentId();

    /** @return the kind of encryption protector; this is metadata used for the Azure Portal experience */
    String kind();

    /** @return the resource location */
    Region region();

    /** @return the name of the server key */
    String serverKeyName();

    /** @return the encryption protector type */
    ServerKeyType serverKeyType();

    /** @return the URI of the server key */
    String uri();

    /** @return thumbprint of the server key */
    String thumbprint();

    /**
     * The template for a SQL Encryption Protector update operation, containing all the settings that can be modified.
     */
    interface Update
        extends SqlEncryptionProtector.UpdateStages.WithServerKeyNameAndType, Appliable<SqlEncryptionProtector> {
    }

    /** Grouping of all the SQL Encryption Protector update stages. */
    interface UpdateStages {
        /** The SQL Encryption Protector update definition to set the server key name and type. */
        interface WithServerKeyNameAndType {
            /**
             * Updates the Encryption Protector to use an AzureKeyVault server key.
             *
             * @param serverKeyName the server key name
             * @return The next stage of the definition.
             */
            SqlEncryptionProtector.Update withAzureKeyVaultServerKey(String serverKeyName);

            /**
             * Updates the Encryption Protector to use the default service managed server key.
             *
             * @return The next stage of the definition.
             */
            SqlEncryptionProtector.Update withServiceManagedServerKey();
        }
    }
}
