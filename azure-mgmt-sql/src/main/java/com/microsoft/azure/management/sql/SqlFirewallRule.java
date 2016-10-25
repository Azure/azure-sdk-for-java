/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
 package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.FirewallRuleInner;

/**
 * An immutable client-side representation of an Azure SQL Server FirewallRule.
 */
@Fluent
public interface SqlFirewallRule extends
        IndependentChild,
        Refreshable<SqlFirewallRule>,
        Updatable<SqlFirewallRule.Update>,
        Wrapper<FirewallRuleInner> {

    /**
     * @return the SQL Server name to which this firewall rule belongs
     */
    String sqlServerName();

    /**
     * @return the start IP address (in IPv4 format) of the Azure SQL Server Firewall Rule.
     */
    String startIpAddress();

    /**
     * @return the end IP address (in IPv4 format) of the Azure SQL Server Firewall Rule.
     */
    String endIpAddress();

    /**
     * Container interface for all the definitions that need to be implemented.
     * @param <CreateStageT> The final stage for which return creatable for FirewallRule
     */
    interface Definition<CreateStageT> extends
            SqlFirewallRule.DefinitionStages.Blank<CreateStageT>,
            SqlFirewallRule.DefinitionStages.WithStartIpAddress<CreateStageT>,
            SqlFirewallRule.DefinitionStages.WithEndIpAddress<CreateStageT>,
            SqlFirewallRule.DefinitionStages.Parentable,
            SqlFirewallRule.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the SQL Server definition.
         * @param <CreateStageT> The final stage for which return creatable for FirewallRule
         */
        interface Blank<CreateStageT> extends SqlFirewallRule.DefinitionStages.WithStartIpAddress<CreateStageT> {
        }

        /**
         * The SQL Firewall Rule definition to set the starting IP Address for the server.
         * @param <CreateStageT> The final stage for which return creatable for FirewallRule
         */
        interface WithStartIpAddress<CreateStageT> {
            /**
             * Sets the starting IP address of SQL server's firewall rule.
             *
             * @param startIpAddress start IP address in IPv4 format.
             * @return The next stage of definition.
             */
            SqlFirewallRule.DefinitionStages.WithEndIpAddress<CreateStageT> withStartIpAddress(String startIpAddress);
        }

        /**
         * The SQL Firewall Rule definition to set the starting IP Address for the server.
         * @param <CreateStageT> The final stage for which return creatable for FirewallRule
         */
        interface WithEndIpAddress<CreateStageT> {
            /**
             * Sets the ending IP address of SQL server's firewall rule.
             *
             * @param endIpAddress end IP address in IPv4 format.
             * @return The next stage of definition.
             */
            CreateStageT withEndIpAddress(String endIpAddress);
        }

        /**
         * A resource definition allowing SQLServer to be attached with SQLFirewallRule.
         */
        interface WithSqlServer {
            /**
             * Creates a new SqlFirewallRule resource under SQLServer.
             *
             * @param groupName the name of the resource group for SQLServer.
             * @param sqlServerName the name of the sQLServer.
             * @return the creatable for the child resource
             */
            Creatable<SqlFirewallRule> withExistingSqlServer(String groupName, String sqlServerName);

            /**
             * Creates a new SqlFirewallRule resource under SQLServer.
             *
             * @param sqlServerCreatable a creatable definition for the SQLServer
             * @return the creatable for the SQLFirewallRule
             */
            Creatable<SqlFirewallRule> withNewSqlServer(Creatable<SqlServer> sqlServerCreatable);

            /**
             * Creates a new SqlFirewallRule resource under SQLServer.
             *
             * @param existingSqlServer the SQLServer under which this SqlFirewallRule to be created.
             * @return the creatable for the SQLFirewallRule
             */
            Creatable<SqlFirewallRule> withExistingSqlServer(SqlServer existingSqlServer);
        }
        /**
         * A SQL Server definition with sufficient inputs to create a new
         * SQL Server in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface Parentable extends
                SqlFirewallRule.DefinitionStages.WithSqlServer {
        }

        /**
         * A SQL Server definition with sufficient inputs to create a new
         * SQL Server in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends Creatable<SqlFirewallRule> {
        }
    }

    /**
     * The template for a SqlFirewallRule update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<SqlFirewallRule> {
    }

    /**
     * Grouping of all the SqlFirewallRule update stages.
     */
    interface UpdateStages {
    }
}
