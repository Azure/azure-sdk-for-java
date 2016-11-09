/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.ServerInner;


/**
 * An immutable client-side representation of an Azure SQL Server.
 */
@Fluent
public interface SqlServer extends
        GroupableResource,
        Refreshable<SqlServer>,
        Updatable<SqlServer.Update>,
        Wrapper<ServerInner> {

    /**
     * @return fully qualified name of the SQL Server
     */
    String fullyQualifiedDomainName();

    /**
     * @return the version of the SQL Server
     */
    ServerVersion version();

    /**
     * @return the administrator login user name for the SQL Server
     */
    String administratorLogin();

    /**
     * @return returns entry point to manage FirewallRules in SqlServer.
     */
    FirewallRules firewallRules();

    /**
     * @return returns entry point to manage ElasticPools in SqlServer.
     */
    ElasticPools elasticPools();

    /**
     * @return entry point to manage Databases in SqlServer.
     */
    Databases databases();

    // TODO - ans - Testing pending for upgrade scenario. Need to implement startUpgrade first for the same.
    /**
     * Cancels a pending upgrade for the Azure SQL Server.
     */
    void cancelUpgrade();

    // TODO - ans - Testing pending for upgrade scenario. Need to implement startUpgrade first for the same.
    /**
     * @return get information about upgrade status of the an Azure SQL Server
     */
    ServerUpgradeResult getUpgrade();

    /**
     * @return returns the list of usages (ServerMetric) of Azure SQL Server
     */
    PagedList<ServerMetric> listUsages();

    /**
     * @return the list of information on all service objectives
     */
    PagedList<ServiceObjective> listServiceObjectives();

    /**
     * Gets the information on a particular Sql Server Service Objective.
     * @param serviceObjectiveName name of the service objective to be fetched
     * @return information of the service objective
     */
    ServiceObjective getServiceObjective(String serviceObjectiveName);

    /**
     * @return the entry point to RecommendedElasticPools.
     */
    RecommendedElasticPools recommendedElasticPools();

    /**
     * Entry point to access FirewallRules from the SQL Server.
     */
    interface FirewallRules {
        /**
         * Gets a particular firewall rule.
         *
         * @param firewallRuleName name of the firewall rule to get
         * @return Returns the SqlFirewall rule with in the SQL Server
         */
        SqlFirewallRule get(String firewallRuleName);

        /**
         * Creates a new firewall rule in SQL Server.
         *
         * @param firewallRuleName name of the firewall rule to be created
         * @return Returns a stage to specify arguments of the firewall rule
         */
        SqlFirewallRule.DefinitionStages.Blank define(String firewallRuleName);

        /**
         * Returns all the firewall rules for the server.
         *
         * @return list of firewall rules for the server.
         */
        PagedList<SqlFirewallRule> list();

        /**
         * Delete specified firewall rule in the server.
         *
         * @param firewallRuleName name of the firewall rule to delete
         */
        void delete(String firewallRuleName);
    }

    /**
     * Entry point to access RecommendedElasticPool from the SQL Server.
     */
    interface RecommendedElasticPools {
        /**
         * Gets a particular recommended elastic pool.
         *
         * @param recommendedElasticPoolName name of the elastic pool to get
         * @return Returns the elastic pool with in the SQL Server
         */
        RecommendedElasticPool get(String recommendedElasticPoolName);

        /**
         * Returns all the recommended elastic pools for the server.
         *
         * @return list of recommended elastic pools for the server.
         */
        PagedList<RecommendedElasticPool> list();
    }

    /**
     * Entry point to access ElasticPools from the SQL Server.
     */
    interface ElasticPools {
        /**
         * Gets a particular elastic pool.
         *
         * @param elasticPoolName name of the elastic pool to get
         * @return Returns the elastic pool with in the SQL Server
         */
        SqlElasticPool get(String elasticPoolName);

        /**
         * Creates a new elastic pool in SQL Server.
         *
         * @param elasticPoolName name of the elastic pool to be created
         * @return Returns a stage to specify arguments of the elastic pool
         */
        SqlElasticPool.DefinitionStages.Blank define(String elasticPoolName);

        /**
         * Returns all the elastic pools for the server.
         *
         * @return list of elastic pools for the server.
         */
        PagedList<SqlElasticPool> list();

        /**
         * Delete specified elastic pool in the server.
         *
         * @param elasticPoolName name of the elastic pool to delete
         */
        void delete(String elasticPoolName);
    }

    /**
     * Entry point to access ElasticPools from the SQL Server.
     */
    interface Databases {
        /**
         * Gets a particular sql database.
         *
         * @param databaseName name of the sql database to get
         * @return Returns the database with in the SQL Server
         */
        SqlDatabase get(String databaseName);

        /**
         * Creates a new database in SQL Server.
         *
         * @param databaseName name of the database to be created
         * @return Returns a stage to specify arguments of the database
         */
        SqlDatabase.DefinitionStages.Blank define(String databaseName);

        /**
         * Returns all the databases for the server.
         *
         * @return list of databases for the server.
         */
        PagedList<SqlDatabase> list();

        /**
         * Delete specified database in the server.
         *
         * @param databaseName name of the database to delete
         */
        void delete(String databaseName);
    }

    /**************************************************************
     * Fluent interfaces to provision a SqlServer
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithAdministratorLogin,
        DefinitionStages.WithAdministratorPassword,
        DefinitionStages.WithVersion,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the SQL Server definition.
         */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /**
         * A SQL Server definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithAdministratorLogin> {
        }

        /**
         * A SQL Server definition setting administrator user name.
         */
        interface WithAdministratorLogin {
            /**
             * Sets the administrator login user name.
             *
             * @param administratorLogin administrator login user name
             * @return Next stage of the SQL Server creation
             */
            WithAdministratorPassword withAdministratorLogin(String administratorLogin);
        }

        /**
         * A SQL Server definition setting admin user password.
         */
        interface WithAdministratorPassword {
            /**
             * Sets the administrator login password.
             *
             * @param administratorLoginPassword password for administrator login
             * @return Next stage of the SQL Server creation
             */
            WithCreate withAdministratorPassword(String administratorLoginPassword);
        }

        /**
         * A SQL Server definition setting version.
         */
        interface WithVersion {
            /**
             * Sets the version of SQL Server to be created.
             *
             * @param version Version of SQL server to be created
             * @return Next stage of the SQL Server creation
             */
            WithCreate withVersion(ServerVersion version);
        }

        /**
         * A SQL Server definition with sufficient inputs to create a new
         * SQL Server in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
            Creatable<SqlServer>,
            DefinitionWithTags<WithCreate>,
            WithVersion {
        }
    }
    /**
     * The template for a SQLServer update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<SqlServer>,
            UpdateStages.WithAdministratorPassword {
    }

    /**
     * Grouping of all the SQLServer update stages.
     */
    interface UpdateStages {

        /**
         * A SQL Server definition setting admin user password.
         */
        interface WithAdministratorPassword {
            /**
             * Sets the administrator login password.
             *
             * @param administratorLoginPassword password for administrator login
             * @return Next stage of the SQL Server creation.
             */
            Update withAdministratorPassword(String administratorLoginPassword);
        }
    }
}

