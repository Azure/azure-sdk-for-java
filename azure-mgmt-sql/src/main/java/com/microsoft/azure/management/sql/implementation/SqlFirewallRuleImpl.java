/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildImpl;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlServer;
import rx.Observable;
import rx.functions.Func1;


/**
 * Implementation for SqlFirewallRule and its parent interfaces.
 */
@LangDefinition
class SqlFirewallRuleImpl
        extends IndependentChildImpl<
                                    SqlFirewallRule,
                                    SqlServer,
                                    ServerFirewallRuleInner,
                                    SqlFirewallRuleImpl>
        implements SqlFirewallRule,
            SqlFirewallRule.Definition,
            SqlFirewallRule.Update,
            IndependentChild.DefinitionStages.WithParentResource<SqlFirewallRule, SqlServer> {
    private final ServersInner innerCollection;

    protected SqlFirewallRuleImpl(String name,
                                  ServerFirewallRuleInner innerObject,
                                  ServersInner innerCollection) {
        super(name, innerObject);
        this.innerCollection = innerCollection;
    }

    @Override
    public String sqlServerName() {
        return this.parentName;
    }

    @Override
    public String startIpAddress() {
        return this.inner().startIpAddress();
    }

    @Override
    public String endIpAddress() {
        return this.inner().endIpAddress();
    }

    @Override
    public String kind() {
        return this.inner().kind();
    }

    @Override
    public Region region() {
        return Region.findByLabelOrName(this.inner().location());
    }

    @Override
    public void delete() {
        this.innerCollection.deleteFirewallRule(this.resourceGroupName(), this.sqlServerName(), this.name());
    }

    @Override
    public SqlFirewallRule refresh() {
        this.innerCollection.getFirewallRule(this.resourceGroupName(), this.sqlServerName(), this.name());
        return this;
    }

    @Override
    protected Observable<SqlFirewallRule> createChildResourceAsync() {
        final SqlFirewallRule self = this;

        return this.innerCollection.createOrUpdateFirewallRuleAsync(this.resourceGroupName(), this.sqlServerName(), this.name(), this.inner())
                .map(new Func1<ServerFirewallRuleInner, SqlFirewallRule>() {
            @Override
            public SqlFirewallRule call(ServerFirewallRuleInner serverFirewallRuleInner) {
                setInner(serverFirewallRuleInner);

                return self;
            }
        });
    }

    @Override
    public SqlFirewallRuleImpl withStartIpAddress(String startIpAddress) {
        this.inner().withStartIpAddress(startIpAddress);
        return this;
    }

    @Override
    public SqlFirewallRuleImpl withEndIpAddress(String endIpAddress) {
        this.inner().withEndIpAddress(endIpAddress);
        return this;
    }

    @Override
    public String id() {
        if (this.inner() != null) {
            return this.inner().id();
        }

        return null;
    }

    @Override
    public SqlFirewallRuleImpl withIpAddressRange(String startIpAddress, String endIpAddress) {
        this.withStartIpAddress(startIpAddress).withEndIpAddress(endIpAddress);
        return this;
    }

    @Override
    public SqlFirewallRuleImpl withIpAddress(String ipAddress) {
        this.inner().withStartIpAddress(ipAddress).withEndIpAddress(ipAddress);
        return this;
    }
}
