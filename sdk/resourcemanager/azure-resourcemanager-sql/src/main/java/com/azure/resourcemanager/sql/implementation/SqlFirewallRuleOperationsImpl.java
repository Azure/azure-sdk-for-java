// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlFirewallRule;
import com.azure.resourcemanager.sql.models.SqlFirewallRuleOperations;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.fluent.inner.FirewallRuleInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SQL Firewall Rule operations. */
public class SqlFirewallRuleOperationsImpl
    implements SqlFirewallRuleOperations, SqlFirewallRuleOperations.SqlFirewallRuleActionsDefinition {

    private SqlServerManager sqlServerManager;
    private SqlServer sqlServer;
    private SqlFirewallRulesAsExternalChildResourcesImpl sqlFirewallRules;

    SqlFirewallRuleOperationsImpl(SqlServer parent, SqlServerManager sqlServerManager) {
        Objects.requireNonNull(sqlServerManager);
        this.sqlServer = parent;
        this.sqlServerManager = sqlServerManager;
        this.sqlFirewallRules = new SqlFirewallRulesAsExternalChildResourcesImpl(sqlServerManager, "SqlFirewallRule");
    }

    SqlFirewallRuleOperationsImpl(SqlServerManager sqlServerManager) {
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.sqlFirewallRules = new SqlFirewallRulesAsExternalChildResourcesImpl(sqlServerManager, "SqlFirewallRule");
    }

    @Override
    public SqlFirewallRule getBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        FirewallRuleInner inner =
            this.sqlServerManager.inner().getFirewallRules().get(resourceGroupName, sqlServerName, name);
        return (inner != null)
            ? new SqlFirewallRuleImpl(resourceGroupName, sqlServerName, inner.name(), inner, sqlServerManager)
            : null;
    }

    @Override
    public Mono<SqlFirewallRule> getBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName, final String name) {
        return this
            .sqlServerManager
            .inner()
            .getFirewallRules()
            .getAsync(resourceGroupName, sqlServerName, name)
            .map(
                inner ->
                    new SqlFirewallRuleImpl(
                        resourceGroupName, sqlServerName, inner.name(), inner, sqlServerManager));
    }

    @Override
    public SqlFirewallRule getBySqlServer(SqlServer sqlServer, String name) {
        Objects.requireNonNull(sqlServer);
        FirewallRuleInner inner =
            this.sqlServerManager.inner().getFirewallRules().get(sqlServer.resourceGroupName(), sqlServer.name(), name);
        return (inner != null)
            ? new SqlFirewallRuleImpl(inner.name(), (SqlServerImpl) sqlServer, inner, sqlServer.manager())
            : null;
    }

    @Override
    public Mono<SqlFirewallRule> getBySqlServerAsync(final SqlServer sqlServer, final String name) {
        Objects.requireNonNull(sqlServer);
        return this
            .sqlServerManager
            .inner()
            .getFirewallRules()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name(), name)
            .map(inner -> new SqlFirewallRuleImpl(name, (SqlServerImpl) sqlServer, inner, sqlServer.manager()));
    }

    @Override
    public SqlFirewallRule get(String name) {
        if (this.sqlServer == null) {
            return null;
        }
        return this.getBySqlServer(this.sqlServer, name);
    }

    @Override
    public Mono<SqlFirewallRule> getAsync(String name) {
        if (this.sqlServer == null) {
            return null;
        }
        return this.getBySqlServerAsync(this.sqlServer, name);
    }

    @Override
    public SqlFirewallRule getById(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServer(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Mono<SqlFirewallRule> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServerAsync(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void deleteBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        this.sqlServerManager.inner().getFirewallRules().delete(resourceGroupName, sqlServerName, name);
    }

    @Override
    public Mono<Void> deleteBySqlServerAsync(String resourceGroupName, String sqlServerName, String name) {
        return this.sqlServerManager.inner().getFirewallRules().deleteAsync(resourceGroupName, sqlServerName, name);
    }

    @Override
    public void deleteById(String id) {
        Objects.requireNonNull(id);
        this
            .deleteBySqlServer(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        Objects.requireNonNull(id);
        return this
            .deleteBySqlServerAsync(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String name) {
        if (this.sqlServer != null) {
            this.deleteBySqlServer(this.sqlServer.resourceGroupName(), this.sqlServer.name(), name);
        }
    }

    @Override
    public Mono<Void> deleteAsync(String name) {
        if (this.sqlServer == null) {
            return null;
        }
        return this.deleteBySqlServerAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name(), name);
    }

    @Override
    public List<SqlFirewallRule> listBySqlServer(String resourceGroupName, String sqlServerName) {
        List<SqlFirewallRule> firewallRuleSet = new ArrayList<>();
        PagedIterable<FirewallRuleInner> firewallRuleInners =
            this.sqlServerManager.inner().getFirewallRules().listByServer(resourceGroupName, sqlServerName);
        for (FirewallRuleInner inner : firewallRuleInners) {
            firewallRuleSet
                .add(
                    new SqlFirewallRuleImpl(
                        resourceGroupName, sqlServerName, inner.name(), inner, this.sqlServerManager));
        }
        return Collections.unmodifiableList(firewallRuleSet);
    }

    @Override
    public PagedFlux<SqlFirewallRule> listBySqlServerAsync(final String resourceGroupName, final String sqlServerName) {
        return this
            .sqlServerManager
            .inner()
            .getFirewallRules()
            .listByServerAsync(resourceGroupName, sqlServerName)
            .mapPage(
                inner ->
                    new SqlFirewallRuleImpl(
                        resourceGroupName, sqlServerName, inner.name(), inner, sqlServerManager));
    }

    @Override
    public List<SqlFirewallRule> listBySqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        List<SqlFirewallRule> firewallRuleSet = new ArrayList<>();
        for (FirewallRuleInner inner
            : sqlServer
                .manager()
                .inner()
                .getFirewallRules()
                .listByServer(sqlServer.resourceGroupName(), sqlServer.name())) {
            firewallRuleSet
                .add(new SqlFirewallRuleImpl(inner.name(), (SqlServerImpl) sqlServer, inner, sqlServer.manager()));
        }
        return Collections.unmodifiableList(firewallRuleSet);
    }

    @Override
    public PagedFlux<SqlFirewallRule> listBySqlServerAsync(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .inner()
            .getFirewallRules()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .mapPage(
                inner ->
                    new SqlFirewallRuleImpl(inner.name(), (SqlServerImpl) sqlServer, inner, sqlServer.manager()));
    }

    @Override
    public List<SqlFirewallRule> list() {
        if (this.sqlServer == null) {
            return null;
        }
        return this.listBySqlServer(this.sqlServer);
    }

    @Override
    public PagedFlux<SqlFirewallRule> listAsync() {
        if (sqlServer == null) {
            return null;
        }
        return this.listBySqlServerAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name());
    }

    @Override
    public SqlFirewallRuleImpl define(String name) {
        SqlFirewallRuleImpl result = sqlFirewallRules.defineIndependentFirewallRule(name);
        return (this.sqlServer != null) ? result.withExistingSqlServer(this.sqlServer) : result;
    }
}
