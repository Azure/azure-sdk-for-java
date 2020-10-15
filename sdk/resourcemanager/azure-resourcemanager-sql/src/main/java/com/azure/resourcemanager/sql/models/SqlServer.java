// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.fluent.models.ServerInner;
import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an Azure SQL Server. */
@Fluent
public interface SqlServer
    extends GroupableResource<SqlServerManager, ServerInner>, Refreshable<SqlServer>, Updatable<SqlServer.Update> {

    /** @return fully qualified name of the SQL Server */
    String fullyQualifiedDomainName();

    /** @return the administrator login user name for the SQL Server */
    String administratorLogin();

    /** @return the SQL Server version */
    String version();

    /** @return the SQL Server "kind" */
    String kind();

    /** @return the state of the server. */
    String state();

    /** @return true if Managed Service Identity is enabled for the SQL server */
    boolean isManagedServiceIdentityEnabled();

    /**
     * @return the System Assigned (Local) Managed Service Identity specific Active Directory tenant ID assigned to the
     *     SQL server.
     */
    String systemAssignedManagedServiceIdentityTenantId();

    /**
     * @return the System Assigned (Local) Managed Service Identity specific Active Directory service principal ID
     *     assigned to the SQL server.
     */
    String systemAssignedManagedServiceIdentityPrincipalId();

    /** @return the type of Managed Service Identity used for the SQL server. */
    IdentityType managedServiceIdentityType();

    // Actions

    /** @return returns the list of usage metrics for an Azure SQL Server */
    List<ServerMetric> listUsageMetrics();

    /** @return the list of information on all service objectives */
    List<ServiceObjective> listServiceObjectives();

    /**
     * Gets the information on a particular Sql Server Service Objective.
     *
     * @param serviceObjectiveName name of the service objective to be fetched
     * @return information of the service objective
     */
    ServiceObjective getServiceObjective(String serviceObjectiveName);

    /**
     * Returns all the recommended elastic pools for the server.
     *
     * @return list of recommended elastic pools for the server
     */
    Map<String, RecommendedElasticPool> listRecommendedElasticPools();

    /** @return the list of all restorable dropped databases */
    List<SqlRestorableDroppedDatabase> listRestorableDroppedDatabases();

    /** @return the list of all restorable dropped databases */
    PagedFlux<SqlRestorableDroppedDatabase> listRestorableDroppedDatabasesAsync();

    /**
     * Sets the Azure services default access to this server to true.
     *
     * <p>A firewall rule named "AllowAllWindowsAzureIps" with the start IP "0.0.0.0" will be added to the SQL server if
     * one does not exist.
     *
     * @return the SQL Firewall rule
     */
    SqlFirewallRule enableAccessFromAzureServices();

    /**
     * Sets the Azure services default access to this server to false.
     *
     * <p>The firewall rule named "AllowAllWindowsAzureIps" will be removed from the SQL server.
     */
    void removeAccessFromAzureServices();

    /**
     * Sets an Active Directory administrator to this server.
     *
     * <p>Azure Active Directory authentication allows you to centrally manage identity and access to your Azure SQL
     * Database V12.
     *
     * @param userLogin the user or group login; it can be the name or the email address
     * @param id the user or group unique ID
     * @return a representation of a SQL Server Active Directory administrator object
     */
    SqlActiveDirectoryAdministrator setActiveDirectoryAdministrator(String userLogin, String id);

    /**
     * Gets the Active Directory administrator for this server.
     *
     * @return a representation of a SQL Server Active Directory administrator object (null if one is not set)
     */
    SqlActiveDirectoryAdministrator getActiveDirectoryAdministrator();

    /** Removes the Active Directory administrator from this server. */
    void removeActiveDirectoryAdministrator();

    /**
     * Gets a SQL server automatic tuning state and options.
     *
     * @return the SQL server automatic tuning state and options
     */
    SqlServerAutomaticTuning getServerAutomaticTuning();

    // Collections

    /** @return the entry point to manage SQL Firewall rules for this server */
    SqlFirewallRuleOperations.SqlFirewallRuleActionsDefinition firewallRules();

    /** @return the entry point to manage SQL Virtual Network Rule for this server */
    SqlVirtualNetworkRuleOperations.SqlVirtualNetworkRuleActionsDefinition virtualNetworkRules();

    /** @return the entry point to manage the SQL Elastic Pools for this server */
    SqlElasticPoolOperations.SqlElasticPoolActionsDefinition elasticPools();

    /** @return entry point to manage Databases for this SQL server */
    SqlDatabaseOperations.SqlDatabaseActionsDefinition databases();

    /** @return the entry point to manage SQL Server DNS aliases for this server */
    SqlServerDnsAliasOperations.SqlServerDnsAliasActionsDefinition dnsAliases();

    /** @return the entry point to manage SQL Failover Group for this server */
    SqlFailoverGroupOperations.SqlFailoverGroupActionsDefinition failoverGroups();

    /** @return the entry point to manage SQL Server Keys for this server */
    SqlServerKeyOperations.SqlServerKeyActionsDefinition serverKeys();

    /** @return the entry point to manage SQL Encryption Protector for this server */
    SqlEncryptionProtectorOperations.SqlEncryptionProtectorActionsDefinition encryptionProtectors();

    /** @return the entry point to manage SQL Server Security Alert Policy for this server */
    SqlServerSecurityAlertPolicyOperations.SqlServerSecurityAlertPolicyActionsDefinition serverSecurityAlertPolicies();

    /**************************************************************
     * Fluent interfaces to provision a SqlServer
     **************************************************************/

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithAdministratorLogin,
            DefinitionStages.WithAdministratorPassword,
            DefinitionStages.WithElasticPool,
            DefinitionStages.WithDatabase,
            DefinitionStages.WithFirewallRule,
            DefinitionStages.WithCreate {
    }

    /** Grouping of all the storage account definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL Server definition. */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /** A SQL Server definition allowing resource group to be set. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithAdministratorLogin> {
        }

        /** A SQL Server definition setting administrator user name. */
        interface WithAdministratorLogin {
            /**
             * Sets the administrator login user name.
             *
             * @param administratorLogin administrator login user name
             * @return Next stage of the SQL Server definition
             */
            WithAdministratorPassword withAdministratorLogin(String administratorLogin);
        }

        /** A SQL Server definition setting admin user password. */
        interface WithAdministratorPassword {
            /**
             * Sets the administrator login password.
             *
             * @param administratorLoginPassword password for administrator login
             * @return Next stage of the SQL Server definition
             */
            WithCreate withAdministratorPassword(String administratorLoginPassword);
        }

        /** A SQL Server definition setting the Active Directory administrator. */
        interface WithActiveDirectoryAdministrator {
            /**
             * Sets the SQL Active Directory administrator.
             *
             * <p>Azure Active Directory authentication allows you to centrally manage identity and access to your Azure
             * SQL Database V12.
             *
             * @param userLogin the user or group login; it can be the name or the email address
             * @param id the user or group unique ID
             * @return Next stage of the SQL Server definition
             */
            WithCreate withActiveDirectoryAdministrator(String userLogin, String id);
        }

        /** A SQL Server definition setting the managed service identity. */
        interface WithSystemAssignedManagedServiceIdentity {
            /**
             * Sets a system assigned (local) Managed Service Identity (MSI) for the SQL server resource.
             *
             * @return Next stage of the SQL Server definition
             */
            WithCreate withSystemAssignedManagedServiceIdentity();
        }

        /** A SQL Server definition for specifying elastic pool. */
        interface WithElasticPool {
            /**
             * Begins the definition of a new SQL Elastic Pool to be added to this server.
             *
             * @param elasticPoolName the name of the new SQL Elastic Pool
             * @return the first stage of the new SQL Elastic Pool definition
             */
            SqlElasticPool.DefinitionStages.Blank<? extends WithCreate> defineElasticPool(String elasticPoolName);
        }

        /** A SQL Server definition for specifying the databases. */
        interface WithDatabase {
            /**
             * Begins the definition of a new SQL Database to be added to this server.
             *
             * @param databaseName the name of the new SQL Database
             * @return the first stage of the new SQL Database definition
             */
            SqlDatabase.DefinitionStages.Blank<? extends WithCreate> defineDatabase(String databaseName);
        }

        /** The stage of the SQL Server definition allowing to specify the SQL Firewall rules. */
        interface WithFirewallRule {
            /**
             * Sets the Azure services default access to this server to false.
             *
             * <p>The default is to allow Azure services default access to this server via a special firewall rule named
             * "AllowAllWindowsAzureIps" with the start IP "0.0.0.0".
             *
             * @return Next stage of the SQL Server definition
             */
            WithCreate withoutAccessFromAzureServices();

            /**
             * Begins the definition of a new SQL Firewall rule to be added to this server.
             *
             * @param firewallRuleName the name of the new SQL Firewall rule
             * @return the first stage of the new SQL Firewall rule definition
             */
            SqlFirewallRule.DefinitionStages.Blank<? extends WithCreate> defineFirewallRule(String firewallRuleName);
        }

        /** The stage of the SQL Server definition allowing to specify the SQL Virtual Network Rules. */
        interface WithVirtualNetworkRule {
            /**
             * Begins the definition of a new SQL Virtual Network Rule to be added to this server.
             *
             * @param virtualNetworkRuleName the name of the new SQL Virtual Network Rule
             * @return the first stage of the new SQL Virtual Network Rule definition
             */
            SqlVirtualNetworkRule.DefinitionStages.Blank<? extends WithCreate> defineVirtualNetworkRule(
                String virtualNetworkRuleName);
        }

        /**
         * A SQL Server definition with sufficient inputs to create a new SQL Server in the cloud, but exposing
         * additional optional inputs to specify.
         */
        interface WithCreate
            extends Creatable<SqlServer>,
                WithActiveDirectoryAdministrator,
                WithSystemAssignedManagedServiceIdentity,
                WithElasticPool,
                WithDatabase,
                WithFirewallRule,
                WithVirtualNetworkRule,
                DefinitionWithTags<WithCreate> {
        }
    }

    /** The template for a SQLServer update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<SqlServer>,
            UpdateStages.WithAdministratorPassword,
            UpdateStages.WithElasticPool,
            UpdateStages.WithDatabase,
            UpdateStages.WithFirewallRule,
            UpdateStages.WithSystemAssignedManagedServiceIdentity,
            Resource.UpdateWithTags<Update> {
    }

    /** Grouping of all the SQLServer update stages. */
    interface UpdateStages {
        /** A SQL Server update stage setting admin user password. */
        interface WithAdministratorPassword {
            /**
             * Sets the administrator login password.
             *
             * @param administratorLoginPassword password for administrator login
             * @return Next stage of the update.
             */
            Update withAdministratorPassword(String administratorLoginPassword);
        }

        /** A SQL Server definition setting the managed service identity. */
        interface WithSystemAssignedManagedServiceIdentity {
            /**
             * Sets a system assigned (local) Managed Service Identity (MSI) for the SQL server resource.
             *
             * @return Next stage of the SQL Server definition
             */
            Update withSystemAssignedManagedServiceIdentity();
        }

        /** A SQL Server definition for specifying elastic pool. */
        interface WithElasticPool {
            /**
             * Begins the definition of a new SQL Elastic Pool to be added to this server.
             *
             * @param elasticPoolName the name of the new SQL Elastic Pool
             * @return the first stage of the new SQL Elastic Pool definition
             */
            SqlElasticPool.DefinitionStages.Blank<? extends Update> defineElasticPool(
                String elasticPoolName);

            /**
             * Removes elastic pool from the SQL Server.
             *
             * @param elasticPoolName name of the elastic pool to be removed
             * @return Next stage of the SQL Server update
             */
            Update withoutElasticPool(String elasticPoolName);
        }

        /** A SQL Server definition for specifying the databases. */
        interface WithDatabase {
            /**
             * Begins the definition of a new SQL Database to be added to this server.
             *
             * @param databaseName the name of the new SQL Database
             * @return the first stage of the new SQL Database definition
             */
            SqlDatabase.DefinitionStages.Blank<? extends Update> defineDatabase(String databaseName);

            /**
             * Remove database from the SQL Server.
             *
             * @param databaseName name of the database to be removed
             * @return Next stage of the SQL Server update
             */
            Update withoutDatabase(String databaseName);
        }

        /** The stage of the SQL Server update definition allowing to specify the SQL Firewall rules. */
        interface WithFirewallRule {
            /**
             * Begins the definition of a new SQL Firewall rule to be added to this server.
             *
             * @param firewallRuleName the name of the new SQL Firewall rule
             * @return the first stage of the new SQL Firewall rule definition
             */
            SqlFirewallRule.DefinitionStages.Blank<? extends Update> defineFirewallRule(
                String firewallRuleName);

            /**
             * Removes firewall rule from the SQL Server.
             *
             * @param firewallRuleName name of the firewall rule to be removed
             * @return Next stage of the SQL Server update
             */
            Update withoutFirewallRule(String firewallRuleName);
        }
    }
}
