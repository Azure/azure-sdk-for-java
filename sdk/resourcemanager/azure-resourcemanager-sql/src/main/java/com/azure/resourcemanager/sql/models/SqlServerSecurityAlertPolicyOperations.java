// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

/** A representation of the Azure SQL Server Security Alert Policy operations. */
@Fluent
public interface SqlServerSecurityAlertPolicyOperations {
    /**
     * Begins a definition for a new SQL Server Security Alert Policy resource.
     *
     * @return the first stage of the resource definition
     */
    SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithSqlServer define();

    /**
     * Gets the information about a SQL Server Security Alert Policy resource from Azure SQL server, identifying it by
     * its resource group.
     *
     * @param resourceGroupName the name of resource group
     * @param sqlServerName the name of SQL server parent resource
     * @return an immutable representation of the resource
     */
    SqlServerSecurityAlertPolicy getBySqlServer(String resourceGroupName, String sqlServerName);

    /**
     * Asynchronously gets the information about a SQL Server Security Alert Policy resource from Azure SQL server,
     * identifying it by its resource group.
     *
     * @param resourceGroupName the name of resource group
     * @param sqlServerName the name of SQL server parent resource
     * @return a representation of the deferred computation of this call returning the found resource
     */
    Mono<SqlServerSecurityAlertPolicy> getBySqlServerAsync(String resourceGroupName, String sqlServerName);

    /**
     * Gets the information about a SQL Server Security Alert Policy resource from Azure SQL server, identifying it by
     * its parent SQL Server.
     *
     * @param sqlServer the SQL server parent resource
     * @return an immutable representation of the resource
     */
    SqlServerSecurityAlertPolicy getBySqlServer(SqlServer sqlServer);

    /**
     * Asynchronously gets the information about a SQL Server Security Alert Policy resource from Azure SQL server,
     * identifying it by its parent SQL server.
     *
     * @param sqlServer the SQL server parent resource
     * @return a representation of the deferred computation of this call returning the found resource
     */
    Mono<SqlServerSecurityAlertPolicy> getBySqlServerAsync(SqlServer sqlServer);

    /**
     * Gets the information about a SQL Server Security Alert Policy resource from Azure SQL server using the resource
     * ID.
     *
     * @param id the ID of the resource.
     * @return an immutable representation of the resource
     */
    SqlServerSecurityAlertPolicy getById(String id);

    /**
     * Asynchronously gets the information about a SQL Server Security Alert Policy resource from Azure SQL server using
     * the resource ID.
     *
     * @param id the ID of the resource.
     * @return a representation of the deferred computation of this call
     */
    Mono<SqlServerSecurityAlertPolicy> getByIdAsync(String id);

    /** Container interface for all the definitions that need to be implemented. */
    interface SqlServerSecurityAlertPolicyOperationsDefinition
        extends SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithSqlServer,
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithState,
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithEmailAccountAdmins,
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithStorageAccount,
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithEmailAddresses,
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithDisabledAlerts,
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithRetentionDays,
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithCreate {
    }

    /** Grouping of all the SQL Server Security Alert Policy definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL Server Security Alert Policy definition. */
        interface WithSqlServer {
            /**
             * Sets the parent SQL server name and resource group it belongs to.
             *
             * @param resourceGroupName the name of the resource group the parent SQL server
             * @param sqlServerName the parent SQL server name
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithState withExistingSqlServer(
                String resourceGroupName, String sqlServerName);

            /**
             * Sets the parent SQL server for the new Server Security Alert Policy.
             *
             * @param sqlServerId the parent SQL server ID
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithState withExistingSqlServerId(
                String sqlServerId);

            /**
             * Sets the parent SQL server for the new Server Security Alert Policy.
             *
             * @param sqlServer the parent SQL server
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithState withExistingSqlServer(
                SqlServer sqlServer);
        }

        /** The SQL Server Security Alert Policy definition to set the state. */
        interface WithState {
            /**
             * Specifies the state of the policy, whether it is enabled or disabled.
             *
             * @param state the state of the policy, whether it is enabled or disabled
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithEmailAccountAdmins withState(
                SecurityAlertPolicyState state);
        }

        /**
         * The SQL Server Security Alert Policy definition to set if an alert will be sent to the account
         * administrators.
         */
        interface WithEmailAccountAdmins {
            /**
             * Specifies that an alert will be sent to the account administrators.
             *
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithStorageAccount withEmailAccountAdmins();

            /**
             * Specifies that an alert will not be sent to the account administrators.
             *
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithStorageAccount withoutEmailAccountAdmins();
        }

        /**
         * The SQL Server Security Alert Policy definition to specify the storage account blob endpoint and access key.
         */
        interface WithStorageAccount {
            /**
             * Specifies the storage account blob endpoint and access key.
             *
             * @param storageEndpointUri the blob storage endpoint (e.g. https://MyAccount.blob.core.windows.net); this
             *     blob storage will hold all Threat Detection audit logs
             * @param storageAccessKey the identifier key of the Threat Detection audit storage account
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithCreate withStorageEndpoint(
                String storageEndpointUri, String storageAccessKey);
        }

        /**
         * The SQL Server Security Alert Policy definition to set an array of e-mail addresses to which the alert is
         * sent.
         */
        interface WithEmailAddresses {
            /**
             * Specifies an array of e-mail addresses to which the alert is sent.
             *
             * @param emailAddresses an array of e-mail addresses to which the alert is sent to
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithCreate withEmailAddresses(
                String... emailAddresses);
        }

        /** The SQL Server Security Alert Policy definition to set an array of alerts that are disabled. */
        interface WithDisabledAlerts {
            /**
             * Specifies an array of alerts that are disabled.
             *
             * @param disabledAlerts an array of alerts that are disabled
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithCreate withDisabledAlerts(
                String... disabledAlerts);
        }

        /**
         * The SQL Server Security Alert Policy definition to set the number of days to keep in the Threat Detection
         * audit logs.
         */
        interface WithRetentionDays {
            /**
             * Specifies the number of days to keep in the Threat Detection audit logs.
             *
             * @param days the number of days to keep in the Threat Detection audit logs
             * @return The next stage of the definition.
             */
            SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithCreate withRetentionDays(int days);
        }

        /** The final stage of the SQL Server Security Alert Policy definition. */
        interface WithCreate
            extends SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithEmailAddresses,
                SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithDisabledAlerts,
                SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithRetentionDays,
                Creatable<SqlServerSecurityAlertPolicy> {
        }
    }

    /** Grouping of the Azure SQL Server Security Alert Policy common actions. */
    interface SqlServerSecurityAlertPolicyActionsDefinition {
        /**
         * Begins the definition of a new SQL Server Security Alert Policy to be added to this server.
         *
         * @return the first stage of the new SQL Server Security Alert Policy definition
         */
        SqlServerSecurityAlertPolicyOperations.DefinitionStages.WithState define();

        /**
         * Gets the information about a SQL Server Security Alert Policy resource from Azure SQL server.
         *
         * @return an immutable representation of the resource
         */
        SqlServerSecurityAlertPolicy get();

        /**
         * Asynchronously gets the information about a SQL Server Security Alert Policy resource from Azure SQL server.
         *
         * @return a representation of the deferred computation of this call returning the found resource
         */
        Mono<SqlServerSecurityAlertPolicy> getAsync();
    }
}
