/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildrenImpl;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlFirewallRules;
import com.microsoft.azure.management.sql.SqlServer;

import org.apache.commons.lang3.NotImplementedException;
import rx.Completable;

import java.util.List;

/**
 * Implementation for SQLElasticPools and its parent interfaces.
 */
@LangDefinition
class SqlFirewallRulesImpl extends IndependentChildrenImpl<
            SqlFirewallRule,
            SqlFirewallRuleImpl,
            ServerFirewallRuleInner,
            ServersInner,
            SqlServerManager,
            SqlServer>
        implements SqlFirewallRules,
        SupportsGettingByParent<SqlFirewallRule, SqlServer, SqlServerManager>,
        SupportsListingByParent<SqlFirewallRule, SqlServer, SqlServerManager>,
        SqlFirewallRules.SqlFirewallRulesCreatable {
    protected SqlFirewallRulesImpl(ServersInner innerCollection, SqlServerManager manager) {
        super(innerCollection, manager);
    }

    @Override
    protected SqlFirewallRuleImpl wrapModel(String name) {
        throw new NotImplementedException("Should never hit this code, currently not exposed");
    }

    @Override
    public SqlFirewallRule getByParent(String resourceGroup, String parentName, String name) {
        return wrapModel(this.innerCollection.getFirewallRule(resourceGroup, parentName, name));
    }

    @Override
    public PagedList<SqlFirewallRule> listByParent(String resourceGroupName, String parentName) {
        return wrapList(this.innerCollection.listFirewallRules(resourceGroupName, parentName));
    }

    @Override
    protected SqlFirewallRuleImpl wrapModel(ServerFirewallRuleInner inner) {
        if (inner == null) {
            return null;
        }
        return new SqlFirewallRuleImpl(inner.name(), inner, this.innerCollection, this.manager());
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return this.innerCollection.deleteFirewallRuleAsync(groupName, parentName, name).toCompletable();
    }

    @Override
    public SqlFirewallRule getBySqlServer(String resourceGroup, String sqlServerName, String name) {
        return this.getByParent(resourceGroup, sqlServerName, name);
    }

    @Override
    public SqlFirewallRule getBySqlServer(SqlServer sqlServer, String name) {
        return this.getByParent(sqlServer, name);
    }

    @Override
    public List<SqlFirewallRule> listBySqlServer(String resourceGroupName, String sqlServerName) {
        return this.listByParent(resourceGroupName, sqlServerName);
    }

    @Override
    public List<SqlFirewallRule> listBySqlServer(SqlServer sqlServer) {
        return this.listByParent(sqlServer);
    }

    @Override
    public SqlFirewallRuleImpl definedWithSqlServer(String resourceGroupName, String sqlServerName, String firewallRuleName) {
        ServerFirewallRuleInner inner = new ServerFirewallRuleInner();

        return new SqlFirewallRuleImpl(
                firewallRuleName,
                inner,
                this.innerCollection,
                this.manager()).withExistingParentResource(resourceGroupName, sqlServerName);
    }
}
