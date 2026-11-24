// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.storage.models.StorageAccount;

/** An immutable client-side representation of an Azure SQL Database import operation request. */
@Fluent
public interface SqlDatabaseImportRequest extends HasInnerModel<ImportExistingDatabaseDefinition>,
    Executable<SqlDatabaseImportExportResponse>, HasParent<SqlDatabase> {

    /** The entirety of database import operation definition. */
    interface SqlDatabaseImportRequestDefinition extends SqlDatabaseImportRequest.DefinitionStages.ImportFrom,
        SqlDatabaseImportRequest.DefinitionStages.WithStorageTypeAndKey,
        SqlDatabaseImportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword,
        SqlDatabaseImportRequest.DefinitionStages.WithExecute {
    }

    /** Grouping of database import definition stages. */
    interface DefinitionStages {
        /** Sets the storage URI to use. */
        interface ImportFrom {
            /**
             * Specifies the storage URI to use.
             *
             * @param storageUri the storage URI to use
             * @return next definition stage
             */
            SqlDatabaseImportRequest.DefinitionStages.WithStorageTypeAndKey importFrom(String storageUri);

            /**
             * Specifies the file from storage account.
             *
             * @param storageAccount an existing storage account to be used
             * @param containerName the container name within the storage account to use
             * @param fileName the exported database file name
             * @return next definition stage
             */
            SqlDatabaseImportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword
                importFrom(StorageAccount storageAccount, String containerName, String fileName);
        }

        /** Sets the storage key type and value to use. */
        interface WithStorageTypeAndKey {
            /**
             * Specifies the storage access key.
             *
             * @param storageAccessKey the storage access key to use
             * @return next definition stage
             */
            SqlDatabaseImportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword
                withStorageAccessKey(String storageAccessKey);

            /**
             * Specifies the share access key.
             *
             * @param sharedAccessKey the shared access key to use; it must be preceded with a "?."
             * @return next definition stage
             */
            SqlDatabaseImportRequest.DefinitionStages.WithAuthenticationTypeAndLoginPassword
                withSharedAccessKey(String sharedAccessKey);
        }

        /** Sets the authentication type and SQL or Active Directory administrator login and password. */
        interface WithAuthenticationTypeAndLoginPassword {
            /**
             * Specifies the SQL login administrator and login password.
             *
             * @param administratorLogin the SQL administrator login
             * @param administratorPassword the SQL administrator password
             * @return next definition stage
             */
            SqlDatabaseImportRequest.DefinitionStages.WithExecute
                withSqlAdministratorLoginAndPassword(String administratorLogin, String administratorPassword);

            /**
             * Specifies the Active Directory administrator and login password.
             *
             * @param administratorLogin the Active Directory administrator login
             * @param administratorPassword the Active Directory administrator password
             * @return next definition stage
             */
            SqlDatabaseImportRequest.DefinitionStages.WithExecute
                withActiveDirectoryLoginAndPassword(String administratorLogin, String administratorPassword);

            /**
             * Specifies the user-assigned managed identity (UAMI) used to authenticate to the SQL database for import.
             *
             * <p>The SQL server must have the specified UAMI assigned (and typically set as its primary identity), the
             * UAMI must be granted the appropriate role on the source storage account (e.g. {@code Storage Blob Data
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
            default SqlDatabaseImportRequest.DefinitionStages.WithExecute
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
