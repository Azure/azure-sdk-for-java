// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlVirtualNetworkRule;
import com.azure.resourcemanager.sql.models.SqlVirtualNetworkRuleOperations;
import com.azure.resourcemanager.sql.fluent.inner.VirtualNetworkRuleInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SQL Virtual Network Rule operations. */
public class SqlVirtualNetworkRuleOperationsImpl extends SqlChildrenOperationsImpl<SqlVirtualNetworkRule>
    implements SqlVirtualNetworkRuleOperations, SqlVirtualNetworkRuleOperations.SqlVirtualNetworkRuleActionsDefinition {

    private SqlVirtualNetworkRulesAsExternalChildResourcesImpl sqlVirtualNetworkRules;

    SqlVirtualNetworkRuleOperationsImpl(SqlServer parent, SqlServerManager sqlServerManager) {
        super(parent, sqlServerManager);
        Objects.requireNonNull(parent);
        this.sqlVirtualNetworkRules =
            new SqlVirtualNetworkRulesAsExternalChildResourcesImpl(sqlServerManager, "SqlVirtualNetworkRule");
    }

    SqlVirtualNetworkRuleOperationsImpl(SqlServerManager sqlServerManager) {
        super(null, sqlServerManager);
        this.sqlVirtualNetworkRules =
            new SqlVirtualNetworkRulesAsExternalChildResourcesImpl(sqlServerManager, "SqlVirtualNetworkRule");
    }

    @Override
    public SqlVirtualNetworkRule getBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        VirtualNetworkRuleInner inner =
            this.sqlServerManager.inner().getVirtualNetworkRules().get(resourceGroupName, sqlServerName, name);
        return (inner != null)
            ? new SqlVirtualNetworkRuleImpl(resourceGroupName, sqlServerName, inner.name(), inner, sqlServerManager)
            : null;
    }

    @Override
    public Mono<SqlVirtualNetworkRule> getBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName, final String name) {
        return this
            .sqlServerManager
            .inner()
            .getVirtualNetworkRules()
            .getAsync(resourceGroupName, sqlServerName, name)
            .map(
                inner ->
                    new SqlVirtualNetworkRuleImpl(
                        resourceGroupName, sqlServerName, inner.name(), inner, sqlServerManager));
    }

    @Override
    public SqlVirtualNetworkRule getBySqlServer(SqlServer sqlServer, String name) {
        if (sqlServer == null) {
            return null;
        }
        VirtualNetworkRuleInner inner =
            this
                .sqlServerManager
                .inner()
                .getVirtualNetworkRules()
                .get(sqlServer.resourceGroupName(), sqlServer.name(), name);
        return (inner != null)
            ? new SqlVirtualNetworkRuleImpl(inner.name(), (SqlServerImpl) sqlServer, inner, sqlServerManager)
            : null;
    }

    @Override
    public Mono<SqlVirtualNetworkRule> getBySqlServerAsync(final SqlServer sqlServer, final String name) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .inner()
            .getVirtualNetworkRules()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name(), name)
            .map(
                inner ->
                    new SqlVirtualNetworkRuleImpl(
                        inner.name(), (SqlServerImpl) sqlServer, inner, sqlServer.manager()));
    }

    @Override
    public void deleteBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        this.sqlServerManager.inner().getVirtualNetworkRules().delete(resourceGroupName, sqlServerName, name);
    }

    @Override
    public Mono<Void> deleteBySqlServerAsync(String resourceGroupName, String sqlServerName, String name) {
        return this.sqlServerManager.inner().getVirtualNetworkRules()
            .deleteAsync(resourceGroupName, sqlServerName, name);
    }

    @Override
    public List<SqlVirtualNetworkRule> listBySqlServer(String resourceGroupName, String sqlServerName) {
        List<SqlVirtualNetworkRule> virtualNetworkRuleSet = new ArrayList<>();
        for (VirtualNetworkRuleInner inner
            : this.sqlServerManager.inner().getVirtualNetworkRules().listByServer(resourceGroupName, sqlServerName)) {
            virtualNetworkRuleSet
                .add(
                    new SqlVirtualNetworkRuleImpl(
                        resourceGroupName, sqlServerName, inner.name(), inner, this.sqlServerManager));
        }
        return Collections.unmodifiableList(virtualNetworkRuleSet);
    }

    @Override
    public PagedFlux<SqlVirtualNetworkRule> listBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName) {
        return this
            .sqlServerManager
            .inner()
            .getVirtualNetworkRules()
            .listByServerAsync(resourceGroupName, sqlServerName)
            .mapPage(
                inner ->
                    new SqlVirtualNetworkRuleImpl(
                        resourceGroupName, sqlServerName, inner.name(), inner, sqlServerManager));
    }

    @Override
    public List<SqlVirtualNetworkRule> listBySqlServer(SqlServer sqlServer) {
        List<SqlVirtualNetworkRule> virtualNetworkRuleSet = new ArrayList<>();
        if (sqlServer != null) {
            for (VirtualNetworkRuleInner inner
                : this
                    .sqlServerManager
                    .inner()
                    .getVirtualNetworkRules()
                    .listByServer(sqlServer.resourceGroupName(), sqlServer.name())) {
                virtualNetworkRuleSet
                    .add(
                        new SqlVirtualNetworkRuleImpl(
                            inner.name(), (SqlServerImpl) sqlServer, inner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableList(virtualNetworkRuleSet);
    }

    @Override
    public PagedFlux<SqlVirtualNetworkRule> listBySqlServerAsync(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .inner()
            .getVirtualNetworkRules()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .mapPage(
                inner ->
                    new SqlVirtualNetworkRuleImpl(inner.name(), (SqlServerImpl) sqlServer, inner, sqlServerManager));
    }

    @Override
    public SqlVirtualNetworkRuleImpl define(String name) {
        SqlVirtualNetworkRuleImpl result = sqlVirtualNetworkRules.defineIndependentVirtualNetworkRule(name);
        return (this.sqlServer != null) ? result.withExistingSqlServer(this.sqlServer) : result;
    }
}
