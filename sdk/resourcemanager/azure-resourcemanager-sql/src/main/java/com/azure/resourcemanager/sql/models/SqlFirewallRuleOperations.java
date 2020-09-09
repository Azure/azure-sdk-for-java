// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

/** A representation of the Azure SQL Firewall rule operations. */
@Fluent
public interface SqlFirewallRuleOperations
    extends SupportsCreating<SqlFirewallRuleOperations.DefinitionStages.WithSqlServer>,
        SqlChildrenOperations<SqlFirewallRule> {

    /** Container interface for all the definitions that need to be implemented. */
    interface SqlFirewallRuleOperationsDefinition
        extends SqlFirewallRuleOperations.DefinitionStages.WithSqlServer,
            SqlFirewallRuleOperations.DefinitionStages.WithIpAddressRange,
            SqlFirewallRuleOperations.DefinitionStages.WithCreate {
    }

    /** Grouping of all the SQL Firewall rule definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL Server Firewall rule definition. */
        interface WithSqlServer {
            /**
             * Sets the parent SQL server name and resource group it belongs to.
             *
             * @param resourceGroupName the name of the resource group the parent SQL server
             * @param sqlServerName the parent SQL server name
             * @return The next stage of the definition.
             */
            WithIpAddressRange withExistingSqlServer(String resourceGroupName, String sqlServerName);

            /**
             * Sets the parent SQL server for the new Firewall rule.
             *
             * @param sqlServerId the parent SQL server ID
             * @return The next stage of the definition.
             */
            WithIpAddressRange withExistingSqlServerId(String sqlServerId);

            /**
             * Sets the parent SQL server for the new Firewall rule.
             *
             * @param sqlServer the parent SQL server
             * @return The next stage of the definition.
             */
            WithIpAddressRange withExistingSqlServer(SqlServer sqlServer);
        }

        /** The SQL Firewall Rule definition to set the Ip address range for the parent SQL Server. */
        interface WithIpAddressRange {
            /**
             * Sets the starting Ip address of SQL server's firewall rule.
             *
             * @param startIpAddress starting Ip address in Ipv4 format.
             * @param endIpAddress starting Ip address in Ipv4 format.
             * @return The next stage of the definition.
             */
            WithCreate withIpAddressRange(String startIpAddress, String endIpAddress);

            /**
             * Sets the ending Ip address of SQL server's firewall rule.
             *
             * @param ipAddress Ip address in Ipv4 format.
             * @return The next stage of the definition.
             */
            WithCreate withIpAddress(String ipAddress);
        }

        /** The final stage of the SQL Firewall Rule definition. */
        interface WithCreate extends Creatable<SqlFirewallRule> {
        }
    }

    /** Grouping of the Azure SQL Server Firewall Rule common actions. */
    interface SqlFirewallRuleActionsDefinition extends SqlChildrenActionsDefinition<SqlFirewallRule> {
        /**
         * Begins the definition of a new SQL Firewall rule to be added to this server.
         *
         * @param firewallRuleName the name of the new SQL Firewall rule
         * @return the first stage of the new SQL Firewall rule definition
         */
        SqlFirewallRuleOperations.DefinitionStages.WithIpAddressRange define(String firewallRuleName);
    }
}
