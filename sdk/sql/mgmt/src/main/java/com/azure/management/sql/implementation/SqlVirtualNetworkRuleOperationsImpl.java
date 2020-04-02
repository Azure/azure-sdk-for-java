/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.management.sql.SqlServer;
import com.azure.management.sql.SqlVirtualNetworkRule;
import com.azure.management.sql.SqlVirtualNetworkRuleOperations;
import com.azure.management.sql.models.VirtualNetworkRuleInner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for SQL Virtual Network Rule operations.
 */
public class SqlVirtualNetworkRuleOperationsImpl
    extends
        SqlChildrenOperationsImpl<SqlVirtualNetworkRule>
    implements
        SqlVirtualNetworkRuleOperations,
        SqlVirtualNetworkRuleOperations.SqlVirtualNetworkRuleActionsDefinition {

    private SqlVirtualNetworkRulesAsExternalChildResourcesImpl sqlVirtualNetworkRules;

    SqlVirtualNetworkRuleOperationsImpl(SqlServer parent, SqlServerManager sqlServerManager) {
        super(parent, sqlServerManager);
        Objects.requireNonNull(parent);
        this.sqlVirtualNetworkRules = new SqlVirtualNetworkRulesAsExternalChildResourcesImpl(sqlServerManager, "SqlVirtualNetworkRule");
    }

    SqlVirtualNetworkRuleOperationsImpl(SqlServerManager sqlServerManager) {
        super(null, sqlServerManager);
        this.sqlVirtualNetworkRules = new SqlVirtualNetworkRulesAsExternalChildResourcesImpl(sqlServerManager, "SqlVirtualNetworkRule");
    }

    @Override
    public SqlVirtualNetworkRule getBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        VirtualNetworkRuleInner inner = this.sqlServerManager.inner().virtualNetworkRules()
            .get(resourceGroupName, sqlServerName, name);
        return (inner != null) ? new SqlVirtualNetworkRuleImpl(resourceGroupName, sqlServerName, inner.getName(), inner, sqlServerManager) : null;
    }

    @Override
    public Mono<SqlVirtualNetworkRule> getBySqlServerAsync(final String resourceGroupName, final String sqlServerName, final String name) {
        return this.sqlServerManager.inner().virtualNetworkRules()
            .getAsync(resourceGroupName, sqlServerName, name)
            .map(inner -> new SqlVirtualNetworkRuleImpl(resourceGroupName, sqlServerName, inner.getName(), inner, sqlServerManager));
    }

    @Override
    public SqlVirtualNetworkRule getBySqlServer(SqlServer sqlServer, String name) {
        if (sqlServer == null) {
            return null;
        }
        VirtualNetworkRuleInner inner = this.sqlServerManager.inner().virtualNetworkRules()
            .get(sqlServer.resourceGroupName(), sqlServer.name(), name);
        return (inner != null) ? new SqlVirtualNetworkRuleImpl(inner.getName(), (SqlServerImpl) sqlServer, inner, sqlServerManager) : null;
    }

    @Override
    public Mono<SqlVirtualNetworkRule> getBySqlServerAsync(final SqlServer sqlServer, final String name) {
        Objects.requireNonNull(sqlServer);
        return sqlServer.manager().inner().virtualNetworkRules()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name(), name)
            .map(inner -> new SqlVirtualNetworkRuleImpl(inner.getName(), (SqlServerImpl) sqlServer, inner, sqlServer.manager()));
    }

    @Override
    public void deleteBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        this.sqlServerManager.inner().virtualNetworkRules().delete(resourceGroupName, sqlServerName, name);
    }

    @Override
    public Mono<Void> deleteBySqlServerAsync(String resourceGroupName, String sqlServerName, String name) {
        return this.sqlServerManager.inner().virtualNetworkRules().deleteAsync(resourceGroupName, sqlServerName, name);
    }

    @Override
    public List<SqlVirtualNetworkRule> listBySqlServer(String resourceGroupName, String sqlServerName) {
        List<SqlVirtualNetworkRule> virtualNetworkRuleSet = new ArrayList<>();
        for (VirtualNetworkRuleInner inner : this.sqlServerManager.inner().virtualNetworkRules().listByServer(resourceGroupName, sqlServerName)) {
            virtualNetworkRuleSet.add(new SqlVirtualNetworkRuleImpl(resourceGroupName, sqlServerName, inner.getName(), inner, this.sqlServerManager));
        }
        return Collections.unmodifiableList(virtualNetworkRuleSet);
    }

    @Override
    public PagedFlux<SqlVirtualNetworkRule> listBySqlServerAsync(final String resourceGroupName, final String sqlServerName) {
        return this.sqlServerManager.inner().virtualNetworkRules().listByServerAsync(resourceGroupName, sqlServerName)
            .mapPage(inner -> new SqlVirtualNetworkRuleImpl(resourceGroupName, sqlServerName, inner.getName(), inner, sqlServerManager));
    }

    @Override
    public List<SqlVirtualNetworkRule> listBySqlServer(SqlServer sqlServer) {
        List<SqlVirtualNetworkRule> virtualNetworkRuleSet = new ArrayList<>();
        if (sqlServer != null) {
            for (VirtualNetworkRuleInner inner : this.sqlServerManager.inner().virtualNetworkRules().listByServer(sqlServer.resourceGroupName(), sqlServer.name())) {
                virtualNetworkRuleSet.add(new SqlVirtualNetworkRuleImpl(inner.getName(), (SqlServerImpl) sqlServer, inner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableList(virtualNetworkRuleSet);
    }

    @Override
    public PagedFlux<SqlVirtualNetworkRule> listBySqlServerAsync(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        return sqlServer.manager().inner().virtualNetworkRules()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .mapPage(inner -> new SqlVirtualNetworkRuleImpl(inner.getName(), (SqlServerImpl) sqlServer, inner, sqlServerManager));
    }

    @Override
    public SqlVirtualNetworkRuleImpl define(String name) {
        SqlVirtualNetworkRuleImpl result = sqlVirtualNetworkRules.defineIndependentVirtualNetworkRule(name);
        return (this.sqlServer != null) ? result.withExistingSqlServer(this.sqlServer) : result;
    }
}
