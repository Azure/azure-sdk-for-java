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
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.ServerFirewallRuleInner;

/**
 * An immutable client-side representation of an Azure SQL Server FirewallRule.
 */
@Fluent
public interface SqlFirewallRule extends
        IndependentChild,
        Refreshable<SqlFirewallRule>,
        Updatable<SqlFirewallRule.Update>,
        Wrapper<ServerFirewallRuleInner> {

    /**
     * @return name of the SQL Server to which this firewall rule belongs
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
            SqlFirewallRule.DefinitionStages.WithIpAddress,
            SqlFirewallRule.DefinitionStages.WithIpAddressRange,
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
                SqlFirewallRule.DefinitionStages.WithIpAddressRange,
                SqlFirewallRule.DefinitionStages.WithIpAddress {
        }

        /**
         * The SQL Firewall Rule definition to set the starting IP Address for the server.
         */
        interface WithIpAddressRange {
            /**
             * Sets the starting IP address of SQL server's firewall rule.
             *
             * @param startIpAddress starting IP address in IPv4 format.
             * @param endIpAddress starting IP address in IPv4 format.
             * @return The next stage of the definition.
             */
            WithCreate withIpAddressRange(String startIpAddress, String endIpAddress);
        }

        /**
         * The SQL Firewall Rule definition to set the starting IP Address for the server.
         */
        interface WithIpAddress {
            /**
             * Sets the ending IP address of SQL server's firewall rule.
             *
             * @param ipAddress IP address in IPv4 format.
             * @return The next stage of the definition.
             */
            WithCreate withIpAddress(String ipAddress);
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
            UpdateStages.WithEndIpAddress,
            UpdateStages.WithStartIpAddress,
            Appliable<SqlFirewallRule> {
    }

    /**
     * Grouping of all the SqlFirewallRule update stages.
     */
    interface UpdateStages {
        /**
         * The SQL Firewall Rule definition to set the starting IP Address for the server.
         */
        interface WithStartIpAddress {
            /**
             * Sets the starting IP address of SQL server's firewall rule.
             *
             * @param startIpAddress start IP address in IPv4 format.
             * @return The next stage of the update.
             */
            Update withStartIpAddress(String startIpAddress);
        }

        /**
         * The SQL Firewall Rule definition to set the starting IP Address for the server.
         */
        interface WithEndIpAddress {
            /**
             * Sets the ending IP address of SQL server's firewall rule.
             *
             * @param endIpAddress end IP address in IPv4 format.
             * @return The next stage of the update.
             */
            Update withEndIpAddress(String endIpAddress);
        }
    }
}
