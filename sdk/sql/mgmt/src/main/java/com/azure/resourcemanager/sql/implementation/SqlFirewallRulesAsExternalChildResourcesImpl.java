// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlFirewallRule;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.fluent.inner.FirewallRuleInner;

/** Represents a SQL Firewall rules collection associated with an Azure SQL server. */
public class SqlFirewallRulesAsExternalChildResourcesImpl
    extends ExternalChildResourcesNonCachedImpl<
        SqlFirewallRuleImpl, SqlFirewallRule, FirewallRuleInner, SqlServerImpl, SqlServer> {

    SqlServerManager sqlServerManager;

    /**
     * Creates a new ExternalNonInlineChildResourcesImpl.
     *
     * @param parent the parent Azure resource
     * @param childResourceName the child resource name
     */
    protected SqlFirewallRulesAsExternalChildResourcesImpl(SqlServerImpl parent, String childResourceName) {
        super(parent, parent.taskGroup(), childResourceName);
        this.sqlServerManager = parent.manager();
    }

    /**
     * Creates a new ExternalChildResourcesNonCachedImpl.
     *
     * @param sqlServerManager the manager
     * @param childResourceName the child resource name (for logging)
     */
    protected SqlFirewallRulesAsExternalChildResourcesImpl(
        SqlServerManager sqlServerManager, String childResourceName) {
        super(null, null, childResourceName);
        this.sqlServerManager = sqlServerManager;
    }

    SqlFirewallRuleImpl defineIndependentFirewallRule(String name) {
        // resource group and server name will be set by the next method in the Fluent flow
        return prepareIndependentDefine(new SqlFirewallRuleImpl(name, new FirewallRuleInner(), this.sqlServerManager));
    }

    SqlFirewallRuleImpl defineInlineFirewallRule(String name) {
        if (this.getParent() == null) {
            // resource group and server name will be set by the next method in the Fluent flow
            return prepareInlineDefine(new SqlFirewallRuleImpl(name, new FirewallRuleInner(), this.sqlServerManager));
        } else {
            return prepareInlineDefine(
                new SqlFirewallRuleImpl(name, this.getParent(), new FirewallRuleInner(), this.getParent().manager()));
        }
    }

    SqlFirewallRuleImpl updateInlineFirewallRule(String name) {
        if (this.getParent() == null) {
            // resource group and server name will be set by the next method in the Fluent flow
            return prepareInlineUpdate(new SqlFirewallRuleImpl(name, new FirewallRuleInner(), this.sqlServerManager));
        } else {
            return prepareInlineUpdate(
                new SqlFirewallRuleImpl(name, this.getParent(), new FirewallRuleInner(), this.getParent().manager()));
        }
    }

    void removeInlineFirewallRule(String name) {
        if (this.getParent() == null) {
            // resource group and server name will be set by the next method in the Fluent flow
            prepareInlineRemove(new SqlFirewallRuleImpl(name, new FirewallRuleInner(), this.sqlServerManager));
        } else {
            prepareInlineRemove(
                new SqlFirewallRuleImpl(name, this.getParent(), new FirewallRuleInner(), this.getParent().manager()));
        }
    }
}
