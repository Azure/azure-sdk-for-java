/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
 package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.sql.implementation.ServerFirewallRuleInner;
import com.microsoft.azure.management.sql.implementation.SqlServerManager;

/**
 * An immutable client-side representation of an Azure SQL Server FirewallRule.
 */
@Fluent
public interface SqlFirewallRule extends
        IndependentChild<SqlServerManager>,
        Refreshable<SqlFirewallRule>,
        Updatable<SqlFirewallRule.Update>,
        HasInner<ServerFirewallRuleInner> {

    /**
     * @return name of the SQL Server to which this firewall rule belongs
     */
    String sqlServerName();

    /**
     * @return the start IP address (in IPv4 format) of the Azure SQL Server Firewall Rule.
     */
    String startIPAddress();

    /**
     * @return the end IP address (in IPv4 format) of the Azure SQL Server Firewall Rule.
     */
    String endIPAddress();

    /**
     * @return kind of SQL Server that contains this firewall rule.
     */
    String kind();

    /**
     * @return region of SQL Server that contains this firewall rule.
     */
    Region region();

    /**
     * Deletes the firewall rule.
     */
    void delete();

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            SqlFirewallRule.DefinitionStages.Blank,
            SqlFirewallRule.DefinitionStages.WithIPAddress,
            SqlFirewallRule.DefinitionStages.WithIPAddressRange,
            SqlFirewallRule.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the SQL Server definition.
         */
        interface Blank extends
                SqlFirewallRule.DefinitionStages.WithIPAddressRange,
                SqlFirewallRule.DefinitionStages.WithIPAddress {
        }

        /**
         * The SQL Firewall Rule definition to set the starting IP Address for the server.
         */
        interface WithIPAddressRange {
            /**
             * Sets the starting IP address of SQL server's firewall rule.
             *
             * @param startIPAddress starting IP address in IPv4 format.
             * @param endIPAddress starting IP address in IPv4 format.
             * @return The next stage of the definition.
             */
            WithCreate withIPAddressRange(String startIPAddress, String endIPAddress);
        }

        /**
         * The SQL Firewall Rule definition to set the starting IP Address for the server.
         */
        interface WithIPAddress {
            /**
             * Sets the ending IP address of SQL server's firewall rule.
             *
             * @param ipAddress IP address in IPv4 format.
             * @return The next stage of the definition.
             */
            WithCreate withIPAddress(String ipAddress);
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
            UpdateStages.WithEndIPAddress,
            UpdateStages.WithStartIPAddress,
            Appliable<SqlFirewallRule> {
    }

    /**
     * Grouping of all the SqlFirewallRule update stages.
     */
    interface UpdateStages {
        /**
         * The SQL Firewall Rule definition to set the starting IP Address for the server.
         */
        interface WithStartIPAddress {
            /**
             * Sets the starting IP address of SQL server's firewall rule.
             *
             * @param startIPAddress start IP address in IPv4 format.
             * @return The next stage of the update.
             */
            Update withStartIPAddress(String startIPAddress);
        }

        /**
         * The SQL Firewall Rule definition to set the starting IP Address for the server.
         */
        interface WithEndIPAddress {
            /**
             * Sets the ending IP address of SQL server's firewall rule.
             *
             * @param endIPAddress end IP address in IPv4 format.
             * @return The next stage of the update.
             */
            Update withEndIPAddress(String endIPAddress);
        }
    }
}
