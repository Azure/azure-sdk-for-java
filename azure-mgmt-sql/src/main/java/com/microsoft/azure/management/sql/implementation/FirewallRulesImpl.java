/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlFirewallRules;
import com.microsoft.azure.management.sql.SqlServer;
import rx.Observable;

import java.util.List;

/**
 * Implementation of SqlServer.FirewallRules, which enables the creating the firewall rules from the SQLServer directly.
 */
@LangDefinition
class FirewallRulesImpl implements SqlServer.FirewallRules {

    private final String resourceGroupName;
    private final String sqlServerName;
    private final SqlFirewallRules.SqlFirewallRulesCreatable sqlFirewallRules;

    FirewallRulesImpl(ServersInner innerCollection, SqlServerManager manager, String resourceGroupName, String sqlServerName) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlFirewallRules = new SqlFirewallRulesImpl(innerCollection, manager);
    }

    @Override
    public SqlFirewallRule get(String firewallRuleName) {
        return this.sqlFirewallRules.getBySqlServer(this.resourceGroupName, this.sqlServerName, firewallRuleName);
    }

    @Override
    public SqlFirewallRule.DefinitionStages.Blank define(String firewallRuleName) {
        return this.sqlFirewallRules.definedWithSqlServer(this.resourceGroupName, this.sqlServerName, firewallRuleName);
    }

    @Override
    public List<SqlFirewallRule> list() {
        return this.sqlFirewallRules.listBySqlServer(this.resourceGroupName, this.sqlServerName);
    }

    @Override
    public void delete(String firewallRuleName) {
        this.sqlFirewallRules.deleteByParent(this.resourceGroupName, this.sqlServerName, firewallRuleName);
    }

    @Override
    public Observable<Void> deleteAsync(String firewallRuleName) {
        return this.sqlFirewallRules.deleteByParentAsync(this.resourceGroupName, this.sqlServerName, firewallRuleName);
    }

    protected SqlFirewallRules sqlFirewallRules() {
        return this.sqlFirewallRules;
    }
}
