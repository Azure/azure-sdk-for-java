/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.sql.implementation.ServersInner;
import com.microsoft.azure.management.sql.implementation.SqlServerManager;

import java.util.List;

/**
 *  Entry point to SQL FirewallRule management API.
 */
@Fluent
public interface SqlFirewallRules extends
        SupportsDeletingById,
        SupportsGettingById<SqlFirewallRule>,
        SupportsBatchCreation<SqlFirewallRule>,
        SupportsDeletingByParent,
        HasManager<SqlServerManager>,
        HasInner<ServersInner> {

    /**
     * Gets the SQLDatabase based on the resource group name, SQLServer name and FirewallRule name.
     *
     * @param resourceGroup the name of resource group.
     * @param sqlServerName the name of SQLServer.
     * @param name the name of SQLDatabase.
     * @return an immutable representation of the SQLDatabase
     */
    SqlFirewallRule getBySqlServer(String resourceGroup, String sqlServerName, String name);

    /**
     * Gets the SQLDatabase based on the SQLServer instance and FirewallRule name.
     *
     * @param sqlServer the instance of SQLServer.
     * @param name the name of SQLDatabase
     * @return an immutable representation of the SQLDatabase
     */
    SqlFirewallRule getBySqlServer(SqlServer sqlServer, String name);

    /**
     * Lists resources of the specified type in the specified resource group and SQLServer.
     *
     * @param resourceGroupName the name of the resource group to list the resources from
     * @param sqlServerName the name of SQLServer
     * @return the list of SQLDatabases in a SQLServer
     */
    List<SqlFirewallRule> listBySqlServer(String resourceGroupName, String sqlServerName);

    /**
     * Gets the SQLDatabase based on the SQLServer.
     *
     * @param sqlServer the instance of SQLServer
     * @return the list of SQLDatabases in a SQLServer
     */
    List<SqlFirewallRule> listBySqlServer(SqlServer sqlServer);

    /**
     * Entry point to SQL FirewallRule management API, which already have the SQLServer specified.
     */
    interface SqlFirewallRulesCreatable extends SqlFirewallRules {
        SqlFirewallRule.DefinitionStages.Blank definedWithSqlServer(String resourceGroupName, String sqlServerName, String firewallRuleName);
    }
}


