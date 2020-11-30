// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.time.OffsetDateTime;

/** A representation of the Azure SQL Server Key operations. */
@Fluent
public interface SqlServerKeyOperations extends SqlChildrenOperations<SqlServerKey> {

    /**
     * Begins a definition for a new SQL Server Key resource.
     *
     * @return the first stage of the resource definition
     */
    SqlServerKeyOperations.DefinitionStages.WithSqlServer define();

    /** Container interface for all the definitions that need to be implemented. */
    interface SqlServerKeyOperationsDefinition
        extends SqlServerKeyOperations.DefinitionStages.WithSqlServer,
            SqlServerKeyOperations.DefinitionStages.WithServerKeyType,
            SqlServerKeyOperations.DefinitionStages.WithThumbprint,
            SqlServerKeyOperations.DefinitionStages.WithCreationDate,
            SqlServerKeyOperations.DefinitionStages.WithCreate {
    }

    /** Grouping of all the SQL Server Key definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL Server Key definition. */
        interface WithSqlServer {
            /**
             * Sets the parent SQL server name and resource group it belongs to.
             *
             * @param resourceGroupName the name of the resource group the parent SQL server
             * @param sqlServerName the parent SQL server name
             * @return The next stage of the definition.
             */
            SqlServerKeyOperations.DefinitionStages.WithServerKeyType withExistingSqlServer(
                String resourceGroupName, String sqlServerName);

            /**
             * Sets the parent SQL server for the new Server Key.
             *
             * @param sqlServerId the parent SQL server ID
             * @return The next stage of the definition.
             */
            SqlServerKeyOperations.DefinitionStages.WithServerKeyType withExistingSqlServerId(String sqlServerId);

            /**
             * Sets the parent SQL server for the new Server Key.
             *
             * @param sqlServer the parent SQL server
             * @return The next stage of the definition.
             */
            SqlServerKeyOperations.DefinitionStages.WithServerKeyType withExistingSqlServer(SqlServer sqlServer);
        }

        /** The SQL Server Key definition to set the server key type. */
        interface WithServerKeyType {
            /**
             * Sets the server key type as "AzureKeyVault" and the URI to the key.
             *
             * @param uri the URI of the server key
             * @return The next stage of the definition.
             */
            SqlServerKeyOperations.DefinitionStages.WithCreate withAzureKeyVaultKey(String uri);
        }

        /** The SQL Server Key definition to set the thumbprint. */
        interface WithThumbprint {
            /**
             * Sets the thumbprint of the server key.
             *
             * @param thumbprint the thumbprint of the server key
             * @return The next stage of the definition.
             */
            SqlServerKeyOperations.DefinitionStages.WithCreate withThumbprint(String thumbprint);
        }

        /** The SQL Server Key definition to set the server key creation date. */
        interface WithCreationDate {
            /**
             * Sets the server key creation date.
             *
             * @param creationDate the server key creation date
             * @return The next stage of the definition.
             */
            SqlServerKeyOperations.DefinitionStages.WithCreate withCreationDate(OffsetDateTime creationDate);
        }

        /** The final stage of the SQL Server Key definition. */
        interface WithCreate
            extends SqlServerKeyOperations.DefinitionStages.WithThumbprint,
                SqlServerKeyOperations.DefinitionStages.WithCreationDate,
                Creatable<SqlServerKey> {
        }
    }

    /** Grouping of the Azure SQL Server Key common actions. */
    interface SqlServerKeyActionsDefinition extends SqlChildrenActionsDefinition<SqlServerKey> {
        /**
         * Begins the definition of a new SQL Server key to be added to this server.
         *
         * @return the first stage of the new SQL Server key definition
         */
        SqlServerKeyOperations.DefinitionStages.WithServerKeyType define();
    }
}
