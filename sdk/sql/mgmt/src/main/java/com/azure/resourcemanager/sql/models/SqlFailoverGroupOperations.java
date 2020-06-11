// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

/** A representation of the Azure SQL Failover Group operations. */
@Fluent
public interface SqlFailoverGroupOperations
    extends SupportsCreating<SqlFailoverGroupOperations.DefinitionStages.WithSqlServer>,
        SqlChildrenOperations<SqlFailoverGroup> {

    /**
     * Fails over from the current primary server to this server.
     *
     * @param resourceGroupName the name of the resource group that contains the resource
     * @param serverName the name of the server containing the failover group
     * @param failoverGroupName the name of the failover group
     * @return the SqlFailoverGroup object
     */
    SqlFailoverGroup failover(String resourceGroupName, String serverName, String failoverGroupName);

    /**
     * Asynchronously fails over from the current primary server to this server.
     *
     * @param resourceGroupName the name of the resource group that contains the resource
     * @param serverName the name of the server containing the failover group
     * @param failoverGroupName the name of the failover group
     * @return a representation of the deferred computation of this call returning the SqlFailoverGroup object
     */
    Mono<SqlFailoverGroup> failoverAsync(String resourceGroupName, String serverName, String failoverGroupName);

    /**
     * Fails over from the current primary server to this server. This operation might result in data loss.
     *
     * @param resourceGroupName the name of the resource group that contains the resource
     * @param serverName the name of the server containing the failover group
     * @param failoverGroupName the name of the failover group
     * @return the SqlFailoverGroup object
     */
    SqlFailoverGroup forceFailoverAllowDataLoss(String resourceGroupName, String serverName, String failoverGroupName);

    /**
     * Fails over from the current primary server to this server. This operation might result in data loss.
     *
     * @param resourceGroupName the name of the resource group that contains the resource
     * @param serverName the name of the server containing the failover group
     * @param failoverGroupName the name of the failover group
     * @return a representation of the deferred computation of this call returning the SqlFailoverGroup object
     */
    Mono<SqlFailoverGroup> forceFailoverAllowDataLossAsync(
        String resourceGroupName, String serverName, String failoverGroupName);

    /** Container interface for all the definitions that need to be implemented. */
    interface SqlFailoverGroupOperationsDefinition
        extends SqlFailoverGroupOperations.DefinitionStages.WithSqlServer,
            SqlFailoverGroupOperations.DefinitionStages.WithReadWriteEndpointPolicy,
            SqlFailoverGroupOperations.DefinitionStages.WithReadOnlyEndpointPolicy,
            SqlFailoverGroupOperations.DefinitionStages.WithPartnerServer,
            SqlFailoverGroupOperations.DefinitionStages.WithDatabase,
            SqlFailoverGroupOperations.DefinitionStages.WithCreate {
    }

    /** Grouping of all the SQL Failover Group definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL Failover Group definition. */
        interface WithSqlServer {
            /**
             * Sets the parent SQL server name and resource group it belongs to.
             *
             * @param resourceGroupName the name of the resource group the parent SQL server
             * @param sqlServerName the parent SQL server name
             * @param location the parent SQL server location
             * @return the next stage of the definition
             */
            SqlFailoverGroupOperations.DefinitionStages.WithReadWriteEndpointPolicy withExistingSqlServer(
                String resourceGroupName, String sqlServerName, String location);

            /**
             * Sets the parent SQL server for the new Failover Group.
             *
             * @param sqlServer the parent SQL server
             * @return the next stage of the definition
             */
            SqlFailoverGroupOperations.DefinitionStages.WithReadWriteEndpointPolicy withExistingSqlServer(
                SqlServer sqlServer);
        }

        /** The SQL Failover Group definition to set the read-write endpoint failover policy. */
        interface WithReadWriteEndpointPolicy {
            /**
             * Sets the SQL Failover Group read-write endpoint failover policy as "Automatic".
             *
             * @param gracePeriodInMinutes the grace period before failover with data loss is attempted for the
             *     read-write endpoint
             * @return the next stage of the definition
             */
            SqlFailoverGroupOperations.DefinitionStages.WithPartnerServer
                withAutomaticReadWriteEndpointPolicyAndDataLossGracePeriod(int gracePeriodInMinutes);

            /**
             * Sets the SQL Failover Group read-write endpoint failover policy as "Manual".
             *
             * @return the next stage of the definition
             */
            SqlFailoverGroupOperations.DefinitionStages.WithPartnerServer withManualReadWriteEndpointPolicy();
        }

        /** The SQL Failover Group definition to set the failover policy of the read-only endpoint. */
        interface WithReadOnlyEndpointPolicy {
            /**
             * Sets the SQL Failover Group failover policy of the read-only endpoint to "Enabled".
             *
             * @return The next stage of the definition.
             */
            SqlFailoverGroupOperations.DefinitionStages.WithCreate withReadOnlyEndpointPolicyEnabled();

            /**
             * Sets the SQL Failover Group failover policy of the read-only endpoint to "Disabled".
             *
             * @return the next stage of the definition
             */
            SqlFailoverGroupOperations.DefinitionStages.WithCreate withReadOnlyEndpointPolicyDisabled();
        }

        /** The SQL Failover Group definition to set the partner servers. */
        interface WithPartnerServer extends SqlFailoverGroupOperations.DefinitionStages.WithCreate {
            /**
             * Sets the SQL Failover Group partner server.
             *
             * @param id the ID of the partner SQL server
             * @return The next stage of the definition.
             */
            SqlFailoverGroupOperations.DefinitionStages.WithPartnerServer withPartnerServerId(String id);
        }

        /** The SQL Failover Group definition to set the partner servers. */
        interface WithDatabase {
            /**
             * Sets the SQL Failover Group database.
             *
             * @param id the ID of the database
             * @return The next stage of the definition.
             */
            SqlFailoverGroupOperations.DefinitionStages.WithCreate withDatabaseId(String id);

            /**
             * Sets the SQL Failover Group partner servers.
             *
             * @param ids the IDs of the databases
             * @return the next stage of the definition
             */
            SqlFailoverGroupOperations.DefinitionStages.WithCreate withDatabaseIds(String... ids);
        }

        /** The final stage of the SQL Failover Group definition. */
        interface WithCreate
            extends SqlFailoverGroupOperations.DefinitionStages.WithReadOnlyEndpointPolicy,
                SqlFailoverGroupOperations.DefinitionStages.WithDatabase,
                Resource.DefinitionWithTags<SqlFailoverGroupOperations.DefinitionStages.WithCreate>,
                Creatable<SqlFailoverGroup> {
        }
    }

    /** Grouping of the Azure SQL Failover Group common actions. */
    interface SqlFailoverGroupActionsDefinition extends SqlChildrenActionsDefinition<SqlFailoverGroup> {
        /**
         * Begins the definition of a new SQL Failover Group to be added to this server.
         *
         * @param failoverGroupName the name of the new Failover Group to be created for the selected SQL server
         * @return the first stage of the new SQL Failover Group definition
         */
        SqlFailoverGroupOperations.DefinitionStages.WithReadWriteEndpointPolicy define(String failoverGroupName);

        /**
         * Fails over from the current primary server to this server.
         *
         * @param failoverGroupName the name of the failover group
         * @return the SqlFailoverGroup object
         */
        SqlFailoverGroup failover(String failoverGroupName);

        /**
         * Asynchronously fails over from the current primary server to this server.
         *
         * @param failoverGroupName the name of the failover group
         * @return a representation of the deferred computation of this call returning the SqlFailoverGroup object
         */
        Mono<SqlFailoverGroup> failoverAsync(String failoverGroupName);

        /**
         * Fails over from the current primary server to this server. This operation might result in data loss.
         *
         * @param failoverGroupName the name of the failover group
         * @return the SqlFailoverGroup object
         */
        SqlFailoverGroup forceFailoverAllowDataLoss(String failoverGroupName);

        /**
         * Fails over from the current primary server to this server. This operation might result in data loss.
         *
         * @param failoverGroupName the name of the failover group
         * @return a representation of the deferred computation of this call returning the SqlFailoverGroup object
         */
        Mono<SqlFailoverGroup> forceFailoverAllowDataLossAsync(String failoverGroupName);
    }
}
