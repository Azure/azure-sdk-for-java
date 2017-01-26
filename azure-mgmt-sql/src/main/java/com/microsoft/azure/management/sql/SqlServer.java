/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.ServerInner;
import com.microsoft.azure.management.sql.implementation.SqlServerManager;
import rx.Completable;

import java.util.List;
import java.util.Map;


/**
 * An immutable client-side representation of an Azure SQL Server.
 */
@Fluent
public interface SqlServer extends
        GroupableResource<SqlServerManager>,
        Refreshable<SqlServer>,
        Updatable<SqlServer.Update>,
        HasManager<SqlServerManager>,
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

    /**
     * @return returns the list of usages (ServerMetric) of Azure SQL Server
     */
    List<ServerMetric> listUsages();

    /**
     * @return the list of information on all service objectives
     */
    List<ServiceObjective> listServiceObjectives();

    /**
     * Gets the information on a particular Sql Server Service Objective.
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
        List<SqlFirewallRule> list();

        /**
         * Delete specified firewall rule in the server.
         *
         * @param firewallRuleName name of the firewall rule to delete
         */
        void delete(String firewallRuleName);

        /**
         * Delete specified firewall rule in the server.
         *
         * @param firewallRuleName name of the firewall rule to delete
         * @return observable for the delete operation
         */
        Completable deleteAsync(String firewallRuleName);
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
        List<SqlElasticPool> list();

        /**
         * Delete specified elastic pool in the server.
         *
         * @param elasticPoolName name of the elastic pool to delete
         */
        void delete(String elasticPoolName);

        /**
         * Delete specified elastic pool in the server.
         *
         * @param elasticPoolName name of the elastic pool to delete
         * @return observable for the delete operation
         */
        Completable deleteAsync(String elasticPoolName);
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
        List<SqlDatabase> list();

        /**
         * Delete specified database in the server.
         *
         * @param databaseName name of the database to delete
         */
        void delete(String databaseName);

        /**
         * Delete specified database in the server.
         *
         * @param databaseName name of the database to delete
         * @return observable for the delete operation
         */
        Completable deleteAsync(String databaseName);
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
        DefinitionStages.WithElasticPool,
        DefinitionStages.WithDatabase,
        DefinitionStages.WithFirewallRule,
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
             * @return Next stage of the SQL Server definition
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
             * @return Next stage of the SQL Server definition
             */
            WithCreate withAdministratorPassword(String administratorLoginPassword);
        }

        /**
         * A SQL Server definition for specifying elastic pool.
         */
        interface WithElasticPool {
            /**
             * Creates new elastic pool in the SQL Server.
             * @param elasticPoolName name of the elastic pool to be created
             * @param elasticPoolEdition edition of the elastic pool
             * @param databaseNames names of the database to be included in the elastic pool
             * @return Next stage of the SQL Server definition
             */
            WithCreate withNewElasticPool(String elasticPoolName, ElasticPoolEditions elasticPoolEdition, String... databaseNames);

            /**
             * Creates new elastic pool in the SQL Server.
             * @param elasticPoolName name of the elastic pool to be created
             * @param elasticPoolEdition edition of the elastic pool
             * @return Next stage of the SQL Server definition
             */
            WithCreate withNewElasticPool(String elasticPoolName, ElasticPoolEditions elasticPoolEdition);
        }

        /**
         * A SQL Server definition for specifying the databases.
         */
        interface WithDatabase {
            /**
             * Creates new database in the SQL Server.
             * @param databaseName name of the database to be created
             * @return Next stage of the SQL Server definition
             */
            WithCreate withNewDatabase(String databaseName);
        }

        /**
         * A SQL Server definition for specifying the firewall rule.
         */
        interface WithFirewallRule {
            /**
             * Creates new firewall rule in the SQL Server.
             *
             * @param ipAddress ipAddress for the firewall rule
             * @return Next stage of the SQL Server definition
             */
            WithCreate withNewFirewallRule(String ipAddress);

            /**
             * Creates new firewall rule in the SQL Server.
             *
             * @param startIpAddress start ipAddress for the firewall rule
             * @param endIpAddress end ipAddress for the firewall rule
             * @return Next stage of the SQL Server definition
             */
            WithCreate withNewFirewallRule(String startIpAddress, String endIpAddress);

            /**
             * Creates new firewall rule in the SQL Server.
             *
             * @param startIpAddress start ipAddress for the firewall rule
             * @param endIpAddress end ipAddress for the firewall rule
             * @param firewallRuleName name for the firewall rule
             * @return Next stage of the SQL Server definition
             */
            WithCreate withNewFirewallRule(String startIpAddress, String endIpAddress, String firewallRuleName);
        }
        /**
         * A SQL Server definition with sufficient inputs to create a new
         * SQL Server in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
            Creatable<SqlServer>,
            DefinitionWithTags<WithCreate>,
            WithElasticPool,
            WithDatabase,
            WithFirewallRule {
        }
    }

    /**
     * The template for a SQLServer update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<SqlServer>,
            UpdateStages.WithAdministratorPassword,
            UpdateStages.WithElasticPool,
            UpdateStages.WithDatabase,
            UpdateStages.WithFirewallRule {
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
             * @return Next stage of the update.
             */
            Update withAdministratorPassword(String administratorLoginPassword);
        }

        /**
         * A SQL Server definition for specifying elastic pool.
         */
        interface WithElasticPool {
            /**
             * Create new elastic pool in the SQL Server.
             * @param elasticPoolName name of the elastic pool to be created
             * @param elasticPoolEdition edition of the elastic pool
             * @param databaseNames names of the database to be included in the elastic pool
             * @return Next stage of the SQL Server update
             */
            Update withNewElasticPool(String elasticPoolName, ElasticPoolEditions elasticPoolEdition, String... databaseNames);

            /**
             * Create new elastic pool in the SQL Server.
             * @param elasticPoolName name of the elastic pool to be created
             * @param elasticPoolEdition edition of the elastic pool
             * @return Next stage of the SQL Server update
             */
            Update withNewElasticPool(String elasticPoolName, ElasticPoolEditions elasticPoolEdition);

            /**
             * Removes elastic pool from the SQL Server.
             * @param elasticPoolName name of the elastic pool to be removed
             * @return Next stage of the SQL Server update
             */
            Update withoutElasticPool(String elasticPoolName);
        }

        /**
         * A SQL Server definition for specifying the databases.
         */
        interface WithDatabase {
            /**
             * Create new database in the SQL Server.
             * @param databaseName name of the database to be created
             * @return Next stage of the SQL Server update
             */
            Update withNewDatabase(String databaseName);

            /**
             * Remove database from the SQL Server.
             * @param databaseName name of the database to be removed
             * @return Next stage of the SQL Server update
             */
            Update withoutDatabase(String databaseName);
        }


        /**
         * A SQL Server definition for specifying the firewall rule.
         */
        interface WithFirewallRule {
            /**
             * Create new firewall rule in the SQL Server.
             *
             * @param ipAddress ipAddress for the firewall rule
             * @return Next stage of the SQL Server update
             */
            Update withNewFirewallRule(String ipAddress);

            /**
             * Create new firewall rule in the SQL Server.
             *
             * @param startIpAddress Start ipAddress for the firewall rule
             * @param endIpAddress ipAddress for the firewall rule
             * @return Next stage of the SQL Server update
             */
            Update withNewFirewallRule(String startIpAddress, String endIpAddress);

            /**
             * Creates new firewall rule in the SQL Server.
             *
             * @param startIpAddress start ipAddress for the firewall rule
             * @param endIpAddress end ipAddress for the firewall rule
             * @param firewallRuleName name for the firewall rule
             * @return Next stage of the SQL Server update
             */
            Update withNewFirewallRule(String startIpAddress, String endIpAddress, String firewallRuleName);

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

