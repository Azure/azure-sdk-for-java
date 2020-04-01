/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.management.sql.SqlServer;
import com.azure.management.sql.SqlServerDnsAlias;
import com.azure.management.sql.SqlServerDnsAliasOperations;
import com.azure.management.sql.models.ServerDnsAliasInner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for SQL Server DNS alias operations.
 */
public class SqlServerDnsAliasOperationsImpl
    extends
        SqlChildrenOperationsImpl<SqlServerDnsAlias>
    implements
        SqlServerDnsAliasOperations,
        SqlServerDnsAliasOperations.SqlServerDnsAliasActionsDefinition {

    private static final String DNS_ALIASES = "/dnsAliases/";

    SqlServerDnsAliasOperationsImpl(SqlServer parent, SqlServerManager sqlServerManager) {
        super(parent, sqlServerManager);
        Objects.requireNonNull(parent);
    }

    SqlServerDnsAliasOperationsImpl(SqlServerManager sqlServerManager) {
        super(null, sqlServerManager);
    }

    @Override
    public SqlServerDnsAlias getBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        ServerDnsAliasInner serverDnsAliasInner = this.sqlServerManager.inner().serverDnsAliases()
            .get(resourceGroupName, sqlServerName, name);
        return serverDnsAliasInner != null ? new SqlServerDnsAliasImpl(resourceGroupName, sqlServerName, name, serverDnsAliasInner, this.sqlServerManager) : null;
    }

    @Override
    public Mono<SqlServerDnsAlias> getBySqlServerAsync(final String resourceGroupName, final String sqlServerName, final String name) {
        final SqlServerDnsAliasOperationsImpl self = this;
        return this.sqlServerManager.inner().serverDnsAliases()
            .getAsync(resourceGroupName, sqlServerName, name)
            .map(serverDnsAliasInner -> new SqlServerDnsAliasImpl(resourceGroupName, sqlServerName, name, serverDnsAliasInner, self.sqlServerManager));
    }

    @Override
    public SqlServerDnsAlias getBySqlServer(SqlServer sqlServer, String name) {
        Objects.requireNonNull(sqlServer);
        ServerDnsAliasInner serverDnsAliasInner = sqlServer.manager().inner().serverDnsAliases()
            .get(sqlServer.resourceGroupName(), sqlServer.name(), name);
        return serverDnsAliasInner != null ? new SqlServerDnsAliasImpl(name, (SqlServerImpl) sqlServer, serverDnsAliasInner, sqlServer.manager()) : null;
    }

    @Override
    public Mono<SqlServerDnsAlias> getBySqlServerAsync(final SqlServer sqlServer, final String name) {
        Objects.requireNonNull(sqlServer);
        return sqlServer.manager().inner().serverDnsAliases()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name(), name)
            .map(serverDnsAliasInner -> new SqlServerDnsAliasImpl(name, (SqlServerImpl) sqlServer, serverDnsAliasInner, sqlServer.manager()));
    }

    @Override
    public void deleteBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        this.sqlServerManager.inner().serverDnsAliases().delete(resourceGroupName, sqlServerName, name);
    }

    @Override
    public Mono<Void> deleteBySqlServerAsync(String resourceGroupName, String sqlServerName, String name) {
        return this.sqlServerManager.inner().serverDnsAliases().deleteAsync(resourceGroupName, sqlServerName, name);
    }

    @Override
    public List<SqlServerDnsAlias> listBySqlServer(String resourceGroupName, String sqlServerName) {
        List<SqlServerDnsAlias> serverDnsAliases = new ArrayList<>();
        PagedIterable<ServerDnsAliasInner> serverDnsAliasInners = this.sqlServerManager.inner().serverDnsAliases()
            .listByServer(resourceGroupName, sqlServerName);
        if (serverDnsAliasInners != null) {
            for (ServerDnsAliasInner inner : serverDnsAliasInners) {
                serverDnsAliases.add(new SqlServerDnsAliasImpl(resourceGroupName, sqlServerName, inner.getName(), inner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableList(serverDnsAliases);
    }

    @Override
    public PagedFlux<SqlServerDnsAlias> listBySqlServerAsync(final String resourceGroupName, final String sqlServerName) {
        final SqlServerDnsAliasOperationsImpl self = this;
        return this.sqlServerManager.inner().serverDnsAliases()
            .listByServerAsync(resourceGroupName, sqlServerName)
            .mapPage(serverDnsAliasInner -> new SqlServerDnsAliasImpl(resourceGroupName, sqlServerName, serverDnsAliasInner.getName(), serverDnsAliasInner, self.sqlServerManager));
    }

    @Override
    public List<SqlServerDnsAlias> listBySqlServer(SqlServer sqlServer) {
        List<SqlServerDnsAlias> serverDnsAliases = new ArrayList<>();
        PagedIterable<ServerDnsAliasInner> serverDnsAliasInners = sqlServer.manager().inner().serverDnsAliases()
            .listByServer(sqlServer.resourceGroupName(), sqlServer.name());
        if (serverDnsAliasInners != null) {
            for (ServerDnsAliasInner inner : serverDnsAliasInners) {
                serverDnsAliases.add(new SqlServerDnsAliasImpl(inner.getName(), (SqlServerImpl) sqlServer, inner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableList(serverDnsAliases);
    }

    @Override
    public PagedFlux<SqlServerDnsAlias> listBySqlServerAsync(final SqlServer sqlServer) {
        return sqlServer.manager().inner().serverDnsAliases()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .mapPage(serverDnsAliasInner -> new SqlServerDnsAliasImpl(serverDnsAliasInner.getName(), (SqlServerImpl) sqlServer, serverDnsAliasInner, sqlServer.manager()));
    }

    @Override
    public void acquire(String resourceGroupName, String serverName, String dnsAliasName, String sqlServerId) {
        this.sqlServerManager.inner().serverDnsAliases()
            .acquire(resourceGroupName, serverName, dnsAliasName, sqlServerId + DNS_ALIASES + dnsAliasName);
    }

    @Override
    public Mono<Void> acquireAsync(String resourceGroupName, String serverName, String dnsAliasName, String sqlServerId) {
        return this.sqlServerManager.inner().serverDnsAliases()
            .acquireAsync(resourceGroupName, serverName, dnsAliasName, sqlServerId + DNS_ALIASES + dnsAliasName);
    }

    @Override
    public void acquire(String dnsAliasName, String oldSqlServerId, String newSqlServerId) {
        Objects.requireNonNull(oldSqlServerId);
        ResourceId resourceId = ResourceId.fromString(oldSqlServerId);
        this.sqlServerManager.inner().serverDnsAliases()
            .acquire(resourceId.resourceGroupName(), resourceId.name(), dnsAliasName, newSqlServerId + DNS_ALIASES + dnsAliasName);
    }

    @Override
    public Mono<Void> acquireAsync(String dnsAliasName, String oldSqlServerId, String newSqlServerId) {
        Objects.requireNonNull(oldSqlServerId);
        ResourceId resourceId = ResourceId.fromString(oldSqlServerId);
        return this.sqlServerManager.inner().serverDnsAliases()
            .acquireAsync(resourceId.resourceGroupName(), resourceId.name(), dnsAliasName, newSqlServerId + DNS_ALIASES + dnsAliasName);
    }

    @Override
    public SqlServerDnsAliasImpl define(String name) {
        SqlServerDnsAliasImpl result = new SqlServerDnsAliasImpl(name, new ServerDnsAliasInner(), this.sqlServerManager);
        result.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return (this.sqlServer != null) ? result.withExistingSqlServer(this.sqlServer) : result;
    }

    @Override
    public void acquire(String dnsAliasName, String sqlServerId) {
        this.acquire(this.sqlServer.resourceGroupName(), this.sqlServer.name(), dnsAliasName, sqlServerId);
    }

    @Override
    public Mono<Void> acquireAsync(String dnsAliasName, String sqlServerId) {
        return this.acquireAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name(), dnsAliasName, sqlServerId);
    }
}
