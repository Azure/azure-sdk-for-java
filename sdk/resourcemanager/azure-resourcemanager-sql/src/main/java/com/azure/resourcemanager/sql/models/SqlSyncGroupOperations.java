// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

/** A representation of the Azure SQL Sync Group operations. */
@Fluent
public interface SqlSyncGroupOperations
    extends SupportsCreating<SqlSyncGroupOperations.DefinitionStages.WithSqlServer> {

    /**
     * Gets the information about a child resource from Azure SQL server, identifying it by its name and its resource
     * group.
     *
     * @param resourceGroupName the name of resource group
     * @param sqlServerName the name of SQL server resource
     * @param databaseName the name of SQL Database parent resource
     * @param name the name of the child resource
     * @return an immutable representation of the resource
     */
    SqlSyncGroup getBySqlServer(String resourceGroupName, String sqlServerName, String databaseName, String name);

    /**
     * Asynchronously gets the information about a child resource from Azure SQL server, identifying it by its name and
     * its resource group.
     *
     * @param resourceGroupName the name of resource group
     * @param sqlServerName the name of SQL server parent resource
     * @param databaseName the name of SQL Database parent resource
     * @param name the name of the child resource
     * @return a representation of the deferred computation of this call returning the found resource
     */
    Mono<SqlSyncGroup> getBySqlServerAsync(
        String resourceGroupName, String sqlServerName, String databaseName, String name);

    /**
     * Gets a collection of sync database ids.
     *
     * @param locationName The name of the region where the resource is located.
     * @return a paged list of database IDs if successful.
     */
    PagedIterable<String> listSyncDatabaseIds(String locationName);

    /**
     * Gets a collection of sync database ids.
     *
     * @param locationName The name of the region where the resource is located.
     * @return a paged list of database IDs if successful.
     */
    PagedFlux<String> listSyncDatabaseIdsAsync(String locationName);

    /**
     * Gets a collection of sync database ids.
     *
     * @param region the region where the resource is located.
     * @return a paged list of database IDs if successful.
     */
    PagedIterable<String> listSyncDatabaseIds(Region region);

    /**
     * Gets a collection of sync database ids.
     *
     * @param region the region where the resource is located.
     * @return a paged list of database IDs if successful.
     */
    PagedFlux<String> listSyncDatabaseIdsAsync(Region region);

    /** Container interface for all the definitions that need to be implemented. */
    interface SqlSyncGroupOperationsDefinition
        extends SqlSyncGroupOperations.DefinitionStages.WithSqlServer,
            SqlSyncGroupOperations.DefinitionStages.WithSyncGroupDatabase,
            SqlSyncGroupOperations.DefinitionStages.WithSyncDatabaseId,
            SqlSyncGroupOperations.DefinitionStages.WithDatabaseUserName,
            SqlSyncGroupOperations.DefinitionStages.WithDatabasePassword,
            SqlSyncGroupOperations.DefinitionStages.WithConflictResolutionPolicy,
            SqlSyncGroupOperations.DefinitionStages.WithInterval,
            SqlSyncGroupOperations.DefinitionStages.WithSchema,
            SqlSyncGroupOperations.DefinitionStages.WithCreate {
    }

    /** Grouping of all the SQL Sync Group definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL Sync Group definition. */
        interface WithSqlServer {
            /**
             * Sets the parent SQL server name and resource group it belongs to.
             *
             * @param resourceGroupName the name of the resource group the parent SQL server
             * @param sqlServerName the parent SQL server name
             * @return The next stage of the definition.
             */
            SqlSyncGroupOperations.DefinitionStages.WithSyncGroupDatabase withExistingSqlServer(
                String resourceGroupName, String sqlServerName);

            /**
             * Sets the parent SQL server for the new Sync Group.
             *
             * @param sqlDatabase the parent SQL database
             * @return The next stage of the definition.
             */
            SqlSyncGroupOperations.DefinitionStages.WithSyncDatabaseId withExistingSqlDatabase(SqlDatabase sqlDatabase);
        }

        /** The SQL Sync Group definition to set the parent database name. */
        interface WithSyncGroupDatabase {
            /**
             * Sets the name of the database on which the sync group is hosted.
             *
             * @param databaseName the name of the database on which the sync group is hosted
             * @return The next stage of the definition.
             */
            SqlSyncGroupOperations.DefinitionStages.WithSyncDatabaseId withExistingDatabaseName(String databaseName);
        }

        /** The SQL Sync Group definition to set the database ID to sync with. */
        interface WithSyncDatabaseId {
            /**
             * Sets the sync database ID.
             *
             * @param syncDatabaseId the sync database ID value to set
             * @return The next stage of the definition.
             */
            SqlSyncGroupOperations.DefinitionStages.WithDatabaseUserName withSyncDatabaseId(String syncDatabaseId);
        }

        /** The SQL Sync Group definition to set the database user name. */
        interface WithDatabaseUserName {
            /**
             * Sets the database user name.
             *
             * @param userName the database user name
             * @return The next stage of the definition.
             */
            SqlSyncGroupOperations.DefinitionStages.WithDatabasePassword withDatabaseUserName(String userName);
        }

        /** The SQL Sync Group definition to set the database login password. */
        interface WithDatabasePassword {
            /**
             * Sets the database login password.
             *
             * @param password the database login password
             * @return The next stage of the definition.
             */
            SqlSyncGroupOperations.DefinitionStages.WithConflictResolutionPolicy withDatabasePassword(String password);
        }

        /** The SQL Sync Group definition to set the conflict resolution policy. */
        interface WithConflictResolutionPolicy {
            /**
             * Sets the conflict resolution policy to "HubWin".
             *
             * @return The next stage of the definition.
             */
            SqlSyncGroupOperations.DefinitionStages.WithCreate withConflictResolutionPolicyHubWins();

            /**
             * Sets the conflict resolution policy to "MemberWin".
             *
             * @return The next stage of the definition.
             */
            SqlSyncGroupOperations.DefinitionStages.WithCreate withConflictResolutionPolicyMemberWins();
        }

        /** The SQL Sync Group definition to set the sync frequency. */
        interface WithInterval {
            /**
             * Sets the sync frequency.
             *
             * @param interval the sync frequency; set to -1 for manual sync
             * @return The next stage of the definition.
             */
            SqlSyncGroupOperations.DefinitionStages.WithCreate withInterval(int interval);
        }

        /** The SQL Sync Group definition to set the schema. */
        interface WithSchema {
            /**
             * Sets the schema.
             *
             * @param schema the schema object to set
             * @return The next stage of the definition.
             */
            SqlSyncGroupOperations.DefinitionStages.WithCreate withSchema(SyncGroupSchema schema);
        }

        /** The final stage of the SQL Sync Group definition. */
        interface WithCreate extends WithInterval, WithSchema, Creatable<SqlSyncGroup> {
        }
    }

    /** Grouping of the Azure SQL Server Sync Group common actions. */
    interface SqlSyncGroupActionsDefinition extends SqlChildrenOperations.SqlChildrenActionsDefinition<SqlSyncGroup> {
        /**
         * Begins the definition of a new SQL Sync Group to be added to this server.
         *
         * @param syncGroupName the name of the new SQL Sync Group
         * @return the first stage of the new SQL Virtual Network Rule definition
         */
        SqlSyncGroupOperations.DefinitionStages.WithSyncDatabaseId define(String syncGroupName);
    }
}
