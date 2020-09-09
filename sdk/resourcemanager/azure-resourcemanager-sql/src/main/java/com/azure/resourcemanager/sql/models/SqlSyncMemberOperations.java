// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

/** A representation of the Azure SQL Sync Member operations. */
@Fluent
public interface SqlSyncMemberOperations
    extends SupportsCreating<SqlSyncMemberOperations.DefinitionStages.WithSqlServer> {

    /**
     * Gets the information about a child resource from Azure SQL server, identifying it by its name and its resource
     * group.
     *
     * @param resourceGroupName the name of resource group
     * @param sqlServerName the name of SQL server resource
     * @param databaseName the name of SQL Database parent resource
     * @param syncGroupName the name of the sync group on which the Sync Member is hosted
     * @param name the name of the child resource
     * @return an immutable representation of the resource
     */
    SqlSyncMember getBySqlServer(
        String resourceGroupName, String sqlServerName, String databaseName, String syncGroupName, String name);

    /**
     * Asynchronously gets the information about a child resource from Azure SQL server, identifying it by its name and
     * its resource group.
     *
     * @param resourceGroupName the name of resource group
     * @param sqlServerName the name of SQL server parent resource
     * @param databaseName the name of SQL Database parent resource
     * @param syncGroupName the name of the sync group on which the Sync Member is hosted
     * @param name the name of the child resource
     * @return a representation of the deferred computation of this call returning the found resource
     */
    Mono<SqlSyncMember> getBySqlServerAsync(
        String resourceGroupName, String sqlServerName, String databaseName, String syncGroupName, String name);

    /** Container interface for all the definitions that need to be implemented. */
    interface SqlSyncMemberOperationsDefinition
        extends SqlSyncMemberOperations.DefinitionStages.WithSqlServer,
            SqlSyncMemberOperations.DefinitionStages.WithSyncMemberDatabase,
            SqlSyncMemberOperations.DefinitionStages.WithSyncGroupName,
            SqlSyncMemberOperations.DefinitionStages.WithMemberSqlServer,
            SqlSyncMemberOperations.DefinitionStages.WithMemberSqlDatabase,
            SqlSyncMemberOperations.DefinitionStages.WithMemberUserName,
            SqlSyncMemberOperations.DefinitionStages.WithMemberPassword,
            SqlSyncMemberOperations.DefinitionStages.WithMemberDatabaseType,
            SqlSyncMemberOperations.DefinitionStages.WithSyncDirection,
            SqlSyncMemberOperations.DefinitionStages.WithCreate {
    }

    /** Grouping of all the SQL Sync Member definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL Sync Member definition. */
        interface WithSqlServer {
            /**
             * Sets the parent SQL server name and resource group it belongs to.
             *
             * @param resourceGroupName the name of the resource group the parent SQL server
             * @param sqlServerName the parent SQL server name
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithSyncMemberDatabase withExistingSqlServer(
                String resourceGroupName, String sqlServerName);

            /**
             * Sets the parent SQL server for the new Sync Member.
             *
             * @param sqlSyncGroup the parent SQL Sync Group
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithMemberSqlServer withExistingSyncGroup(
                SqlSyncGroup sqlSyncGroup);
        }

        /** The SQL Sync Member definition to set the parent database name. */
        interface WithSyncMemberDatabase {
            /**
             * Sets the name of the database on which the sync Member is hosted.
             *
             * @param databaseName the name of the database on which the sync Member is hosted
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithSyncGroupName withExistingDatabaseName(String databaseName);
        }

        /** The SQL Sync Member definition to set the parent database name. */
        interface WithSyncGroupName {
            /**
             * Sets the name of the database on which the sync Member is hosted.
             *
             * @param syncGroupName the name of the sync group on which the Sync Member is hosted
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithMemberSqlServer withExistingSyncGroupName(
                String syncGroupName);
        }

        /** The SQL Sync Member definition to set the member server and database. */
        interface WithMemberSqlServer {
            /**
             * Sets the member SQL server name.
             *
             * @param sqlServerName the member SQL server name value to set
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithMemberSqlDatabase withMemberSqlServerName(
                String sqlServerName);

            /**
             * Sets the member SQL Database.
             *
             * @param sqlDatabase the member SQL Database value to set
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithMemberUserName withMemberSqlDatabase(SqlDatabase sqlDatabase);
        }

        /** The SQL Sync Member definition to set the member database. */
        interface WithMemberSqlDatabase {
            /**
             * Sets the member SQL Database name.
             *
             * @param sqlDatabaseName the member SQL Database name value to set
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithMemberUserName withMemberSqlDatabaseName(
                String sqlDatabaseName);
        }

        /** The SQL Sync Member definition to set the member database user name. */
        interface WithMemberUserName {
            /**
             * Sets the member SQL Database username.
             *
             * @param userName the member SQL Database username value to set
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithMemberPassword withMemberUserName(String userName);
        }

        /** The SQL Sync Member definition to set the member database password. */
        interface WithMemberPassword {
            /**
             * Sets the member SQL Database password.
             *
             * @param password the member SQL Database password value to set
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithMemberDatabaseType withMemberPassword(String password);
        }

        /** The SQL Sync Member definition to set the database type. */
        interface WithMemberDatabaseType {
            /**
             * Sets the member database type.
             *
             * @param databaseType the database type value to set
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithSyncDirection withMemberDatabaseType(
                SyncMemberDbType databaseType);
        }

        /** The SQL Sync Member definition to set the sync direction. */
        interface WithSyncDirection {
            /**
             * Sets the sync direction.
             *
             * @param syncDirection the sync direction value to set
             * @return The next stage of the definition.
             */
            SqlSyncMemberOperations.DefinitionStages.WithCreate withDatabaseType(SyncDirection syncDirection);
        }

        /** The final stage of the SQL Sync Member definition. */
        interface WithCreate extends Creatable<SqlSyncMember> {
        }
    }

    /** Grouping of the Azure SQL Server Sync Member common actions. */
    interface SqlSyncMemberActionsDefinition extends SqlChildrenOperations.SqlChildrenActionsDefinition<SqlSyncMember> {
        /**
         * Begins the definition of a new SQL Sync Member to be added to this server.
         *
         * @param syncMemberName the name of the new SQL Sync Member
         * @return the first stage of the new SQL Virtual Network Rule definition
         */
        SqlSyncMemberOperations.DefinitionStages.WithMemberSqlServer define(String syncMemberName);
    }
}
