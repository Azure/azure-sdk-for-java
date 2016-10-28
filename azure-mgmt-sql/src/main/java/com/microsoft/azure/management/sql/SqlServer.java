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
     *
     * @return the administrator login user name for the SQL Server
     */
    String adminLogin();

    /**
     * @return returns entry point to manage FirewallRules in SqlServer.
     */
    FirewallRules firewallRules();

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
    /**************************************************************
     * Fluent interfaces to provision a SqlServer
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithAdminUserName,
        DefinitionStages.WithPassword,
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
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithAdminUserName> {
        }

        /**
         * A SQL Server definition setting admin user name.
         */
        interface WithAdminUserName {
            WithPassword withAdminUserName(String adminUserName);
        }

        /**
         * A SQL Server definition setting admin user password.
         */
        interface WithPassword {
            WithCreate withPassword(String password);
        }

        /**
         * A SQL Server definition setting version.
         */
        interface WithVersion {
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
            UpdateStages.WithAdminPassword {
    }

    /**
     * Grouping of all the SQLServer update stages.
     */
    interface UpdateStages {

        /**
         * A SQL Server definition setting administrator user password.
         */
        interface WithAdminPassword {
            Update withPassword(String administratorPassword);
        }
    }
}

