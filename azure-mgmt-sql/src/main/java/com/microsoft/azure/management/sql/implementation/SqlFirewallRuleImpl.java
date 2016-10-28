/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlServer;
import rx.Observable;
import rx.functions.Func1;


/**
 * Implementation for SqlFirewallRule and its parent interfaces.
 * @param <CreateStageT> The final stage for which return creatable for FirewallRule
 */
public class SqlFirewallRuleImpl<CreateStageT>
        extends IndependentChildImpl<
                                    SqlFirewallRule,
                                    SqlServer,
                                    ServerFirewallRuleInner,
                                    SqlFirewallRuleImpl<CreateStageT>>
        implements SqlFirewallRule,
            SqlFirewallRule.Definition<CreateStageT>,
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
    public SqlFirewallRule refresh() {
        this.innerCollection.getFirewallRule(this.resourceGroupName(), this.sqlServerName(), this.name());
        return this;
    }

    @Override
    protected void setParentName(ServerFirewallRuleInner inner) {
        if (inner.id() != null) {
            this.parentName = ResourceId.parseResourceId(inner.id()).parent().name();
        }
    }

    @Override
    protected Observable<SqlFirewallRule> createChildResourceAsync() {
        final SqlFirewallRule self = this;

        return this.innerCollection.createOrUpdateFirewallRuleAsync(this.resourceGroupName(), this.sqlServerName(), this.name(), this.inner())
                .map(new Func1<ServerFirewallRuleInner, SqlFirewallRule>() {
            @Override
            public SqlFirewallRule call(ServerFirewallRuleInner databaseInner) {
                setInner(databaseInner);

                return self;
            }
        });
    }


    @Override
    public Creatable<SqlFirewallRule> withExistingSqlServer(String groupName, String sqlServerName) {
        return withExistingParentResource(groupName, sqlServerName);
    }

    @Override
    public Creatable<SqlFirewallRule> withNewSqlServer(Creatable<SqlServer> sqlServerCreatable) {
        return withNewParentResource(sqlServerCreatable);
    }

    @Override
    public Creatable<SqlFirewallRule> withExistingSqlServer(SqlServer existingSqlServer) {
        return withExistingParentResource(existingSqlServer);
    }

    @Override
    public SqlFirewallRuleImpl<CreateStageT> withStartIpAddress(String startIpAddress) {
        this.inner().withStartIpAddress(startIpAddress);
        return this;
    }

    @Override
    public CreateStageT withEndIpAddress(String endIpAddress) {
        this.inner().withEndIpAddress(endIpAddress);
        return (CreateStageT) this;
    }

    @Override
    public String id() {
        return this.inner().id();
    }
}
