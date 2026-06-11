// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.storage.models.StorageAccount;

/** An immutable client-side representation of an Azure SQL Database export operation request. */
@Fluent
public interface SqlDatabaseExportRequest extends HasInnerModel<ExportDatabaseDefinition>,
    Executable<SqlDatabaseImportExportResponse>, HasParent<SqlDatabase> {

    /** The entirety of database export operation definition. */
    interface SqlDatabaseExportRequestDefinition extends SqlDatabaseExportRequest.DefinitionStages.ExportTo,
        SqlDatabaseExportRequest.DefinitionStages.WithStorageTypeAndKey,
        SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword, DefinitionStages.WithExecute {
    }

    /** Grouping of database export definition stages. */
    interface DefinitionStages {
        /** Sets the storage URI to use. */
        interface ExportTo {
            /**
             * Sets the storage URI to use.
             *
             * @param storageUri the storage URI to use
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithStorageTypeAndKey exportTo(String storageUri);

            /**
             * Export database file to the container of storage.
             *
             * @param storageAccount an existing storage account to be used
             * @param containerName the container name within the storage account to use
             * @param fileName the exported database file name
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword
                exportTo(StorageAccount storageAccount, String containerName, String fileName);

            /**
             * Export database file to the container of storage.
             *
             * @param storageAccountCreatable a storage account to be created as part of this execution flow
             * @param containerName the container name within the storage account to use
             * @param fileName the exported database file name
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword
                exportTo(Creatable<StorageAccount> storageAccountCreatable, String containerName, String fileName);
        }

        /** Sets the storage key type and value to use. */
        interface WithStorageTypeAndKey {
            /**
             * Specifies storage access key.
             *
             * @param storageAccessKey the storage access key to use
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword
                withStorageAccessKey(String storageAccessKey);

            /**
             * Specifies share access key.
             *
             * @param sharedAccessKey the shared access key to use; it must be preceded with a "?."
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword
                withSharedAccessKey(String sharedAccessKey);
        }

        /** Sets the authentication type and SQL or Active Directory administrator login and password. */
        interface WithAuthenticationTypeAndLoginPassword {
            /**
             * Sets the SQL login administrator and login password.
             *
             * @param administratorLogin the SQL administrator login
             * @param administratorPassword the SQL administrator password
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithExecute
                withSqlAdministratorLoginAndPassword(String administratorLogin, String administratorPassword);

            /**
             * Sets the login Active Directory and login password.
             *
             * @param administratorLogin the Active Directory administrator login
             * @param administratorPassword the Active Directory administrator password
             * @return next definition stage
             */
            SqlDatabaseExportRequest.DefinitionStages.WithExecute
                withActiveDirectoryLoginAndPassword(String administratorLogin, String administratorPassword);

            /**
             * Sets the user-assigned managed identity (UAMI) used to authenticate to the SQL database for export.
             *
             * <p>The SQL server must have the specified UAMI assigned (and typically set as its primary identity), the
             * UAMI must be granted the appropriate role on the target storage account (e.g. {@code Storage Blob Data
             * Contributor}), and it must be mapped to a database user with the required privileges. When this method
             * is used, no administrator password is sent to the service.</p>
             *
             * <p>This method is for user-assigned managed identity. System-assigned managed identity is not supported.
             * See <a href="https://learn.microsoft.com/azure/azure-sql/database/database-import-export-managed-identity?view=azuresql&tabs=azure-portal#limitations">Limitations</a></p>
             *
             * @param managedIdentityResourceId the Azure resource ID of the user-assigned managed identity to use
             *                                  for both SQL and storage access
             * @return next definition stage
             */
            default SqlDatabaseExportRequest.DefinitionStages.WithExecute
                withManagedIdentity(String managedIdentityResourceId) {
                throw new UnsupportedOperationException("[withManagedIdentity] is not supported in " + getClass());
            }
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for execution, but also allows for
         * any other optional settings to be specified.
         */
        interface WithExecute extends Executable<SqlDatabaseImportExportResponse> {
        }
    }
}
