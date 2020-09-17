// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.sql.fluent.inner.FirewallRuleInner;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure SQL Server Firewall Rule. */
@Fluent
public interface SqlFirewallRule
    extends ExternalChildResource<SqlFirewallRule, SqlServer>,
        HasInner<FirewallRuleInner>,
        HasResourceGroup,
        Refreshable<SqlFirewallRule>,
        Updatable<SqlFirewallRule.Update> {

    /** @return name of the SQL Server to which this Firewall Rule belongs */
    String sqlServerName();

    /** @return the start Ip address (in Ipv4 format) of the Azure SQL Server Firewall Rule. */
    String startIpAddress();

    /** @return the end Ip address (in Ipv4 format) of the Azure SQL Server Firewall Rule. */
    String endIpAddress();

    /** @return kind of SQL Server that contains this Firewall Rule. */
    String kind();

    /** @return region of SQL Server that contains this Firewall Rule. */
    Region region();

    /** @return the parent SQL server ID */
    String parentId();

    /** Deletes the firewall rule. */
    void delete();

    /**
     * Deletes the firewall rule asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync();

    /**************************************************************
     * Fluent interfaces to provision a SQL Firewall Rule
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface SqlFirewallRuleDefinition<ParentT>
        extends SqlFirewallRule.DefinitionStages.Blank<ParentT>,
            SqlFirewallRule.DefinitionStages.WithIpAddress<ParentT>,
            SqlFirewallRule.DefinitionStages.WithIpAddressRange<ParentT>,
            SqlFirewallRule.DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of all the SQL Firewall Rule definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the SQL Server Firewall Rule definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT>
            extends SqlFirewallRule.DefinitionStages.WithIpAddressRange<ParentT>,
                SqlFirewallRule.DefinitionStages.WithIpAddress<ParentT> {
        }

        /** The SQL Firewall Rule definition to set the Ip address range for the parent SQL Server. */
        interface WithIpAddressRange<ParentT> {
            /**
             * Sets the starting Ip address of SQL server's Firewall Rule.
             *
             * @param startIpAddress starting Ip address in Ipv4 format.
             * @param endIpAddress starting Ip address in Ipv4 format.
             * @return The next stage of the definition.
             */
            WithAttach<ParentT> withIpAddressRange(String startIpAddress, String endIpAddress);
        }

        /** The SQL Firewall Rule definition to set the Ip address for the parent SQL Server. */
        interface WithIpAddress<ParentT> {
            /**
             * Sets the ending Ip address of SQL server's Firewall Rule.
             *
             * @param ipAddress Ip address in Ipv4 format.
             * @return The next stage of the definition.
             */
            WithAttach<ParentT> withIpAddress(String ipAddress);
        }

        /**
         * The final stage of the SQL Firewall Rule definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the SQL Firewall Rule definition can
         * be attached to the parent SQL Server definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {
        }
    }

    /** The template for a SQL Firewall Rule update operation, containing all the settings that can be modified. */
    interface Update
        extends UpdateStages.WithEndIpAddress, UpdateStages.WithStartIpAddress, Appliable<SqlFirewallRule> {
    }

    /** Grouping of all the SQL Firewall Rule update stages. */
    interface UpdateStages {
        /** The SQL Firewall Rule definition to set the starting Ip Address for the server. */
        interface WithStartIpAddress {
            /**
             * Sets the starting Ip address of SQL server's Firewall Rule.
             *
             * @param startIpAddress start Ip address in Ipv4 format.
             * @return The next stage of the update.
             */
            Update withStartIpAddress(String startIpAddress);
        }

        /** The SQL Firewall Rule definition to set the starting Ip Address for the server. */
        interface WithEndIpAddress {
            /**
             * Sets the ending Ip address of SQL server's Firewall Rule.
             *
             * @param endIpAddress end Ip address in Ipv4 format.
             * @return The next stage of the update.
             */
            Update withEndIpAddress(String endIpAddress);
        }
    }
}
