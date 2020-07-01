// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.storage.models.StorageAccount;

/** An immutable client-side representation of an Azure SQL Database export operation request. */
@Fluent
public interface SqlDatabaseExportRequest
    extends HasInner<ExportRequest>, Executable<SqlDatabaseImportExportResponse>, HasParent<SqlDatabase> {

    /** The entirety of database export operation definition. */
    interface SqlDatabaseExportRequestDefinition
        extends SqlDatabaseExportRequest.DefinitionStages.ExportTo,
            SqlDatabaseExportRequest.DefinitionStages.WithStorageTypeAndKey,
            SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword,
            DefinitionStages.WithExecute {
    }

    /** Grouping of database export definition stages. */
    interface DefinitionStages {
        /** Sets the storage URI to use. */
        interface ExportTo {
            /**
             * @param storageUri the storage URI to use
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithStorageTypeAndKey exportTo(String storageUri);

            /**
             * @param storageAccount an existing storage account to be used
             * @param containerName the container name within the storage account to use
             * @param fileName the exported database file name
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword exportTo(
                StorageAccount storageAccount, String containerName, String fileName);

            /**
             * @param storageAccountCreatable a storage account to be created as part of this execution flow
             * @param containerName the container name within the storage account to use
             * @param fileName the exported database file name
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword exportTo(
                Creatable<StorageAccount> storageAccountCreatable, String containerName, String fileName);
        }

        /** Sets the storage key type and value to use. */
        interface WithStorageTypeAndKey {
            /**
             * @param storageAccessKey the storage access key to use
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword withStorageAccessKey(
                String storageAccessKey);

            /**
             * @param sharedAccessKey the shared access key to use; it must be preceded with a "?."
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword withSharedAccessKey(
                String sharedAccessKey);
        }

        /** Sets the authentication type and SQL or Active Directory administrator login and password. */
        interface WithAuthenticationTypeAndLoginPassword {
            /**
             * @param administratorLogin the SQL administrator login
             * @param administratorPassword the SQL administrator password
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithExecute withSqlAdministratorLoginAndPassword(
                String administratorLogin, String administratorPassword);

            /**
             * @param administratorLogin the Active Directory administrator login
             * @param administratorPassword the Active Directory administrator password
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithExecute withActiveDirectoryLoginAndPassword(
                String administratorLogin, String administratorPassword);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for execution, but also allows for
         * any other optional settings to be specified.
         */
        interface WithExecute extends Executable<SqlDatabaseImportExportResponse> {
        }
    }
}
