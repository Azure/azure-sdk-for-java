/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.sql.SqlDatabase;
import com.azure.management.sql.SqlDatabaseOperations;
import com.azure.management.sql.SqlServer;
import com.azure.management.sql.models.DatabaseInner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for SQL Firewall Rule operations.
 */
public class SqlDatabaseOperationsImpl
    implements
        SqlDatabaseOperations,
        SqlDatabaseOperations.SqlDatabaseActionsDefinition {

    private SqlServerManager manager;
    private SqlServerImpl sqlServer;
    private SqlDatabasesAsExternalChildResourcesImpl sqlDatabases;

    SqlDatabaseOperationsImpl(SqlServerImpl parent, SqlServerManager manager) {
        Objects.requireNonNull(manager);
        this.sqlServer = parent;
        this.manager = manager;
        this.sqlDatabases = new SqlDatabasesAsExternalChildResourcesImpl(this.sqlServer.taskGroup(), manager, "SqlDatabase");
    }

    SqlDatabaseOperationsImpl(SqlServerManager manager) {
        Objects.requireNonNull(manager);
        this.manager = manager;
        this.sqlDatabases = new SqlDatabasesAsExternalChildResourcesImpl(null, manager, "SqlDatabase");
    }

    @Override
    public SqlDatabase getBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        DatabaseInner inner = this.manager.inner().databases().get(resourceGroupName, sqlServerName, name);
        return (inner != null) ? new SqlDatabaseImpl(resourceGroupName, sqlServerName, inner.getLocation(), inner.getName(), inner, manager) : null;
    }

    @Override
    public Mono<SqlDatabase> getBySqlServerAsync(final String resourceGroupName, final String sqlServerName, final String name) {
        return this.manager.inner().databases().getAsync(resourceGroupName, sqlServerName, name)
            .map(inner -> new SqlDatabaseImpl(resourceGroupName, sqlServerName, inner.getLocation(), inner.getName(), inner, manager));
    }

    @Override
    public SqlDatabase getBySqlServer(SqlServer sqlServer, String name) {
        if (sqlServer == null) {
            return null;
        }
        DatabaseInner inner = this.manager.inner().databases().get(sqlServer.resourceGroupName(), sqlServer.name(), name);
        return (inner != null) ? new SqlDatabaseImpl(inner.getName(), (SqlServerImpl) sqlServer, inner, manager) : null;
    }

    @Override
    public Mono<SqlDatabase> getBySqlServerAsync(final SqlServer sqlServer, String name) {
        Objects.requireNonNull(sqlServer);
        return sqlServer.manager().inner().databases().getAsync(sqlServer.resourceGroupName(), sqlServer.name(), name)
            .map(inner -> new SqlDatabaseImpl(inner.getName(), (SqlServerImpl) sqlServer, inner, manager));
    }

    @Override
    public SqlDatabase get(String name) {
        if (sqlServer == null) {
            return null;
        }
        return this.getBySqlServer(this.sqlServer.resourceGroupName(), this.sqlServer.name(), name);
    }

    @Override
    public Mono<SqlDatabase> getAsync(String name) {
        if (sqlServer == null) {
            return null;
        }
        return this.getBySqlServerAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name(), name);
    }

    @Override
    public SqlDatabase getById(String id) {
        Objects.requireNonNull(id);
        return this.getBySqlServer(ResourceUtils.groupFromResourceId(id),
            ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
            ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Mono<SqlDatabase> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        return this.getBySqlServerAsync(ResourceUtils.groupFromResourceId(id),
            ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
            ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void deleteBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        this.manager.inner().databases().delete(resourceGroupName, sqlServerName, name);
    }

    @Override
    public Mono<Void> deleteBySqlServerAsync(String resourceGroupName, String sqlServerName, String name) {
        return this.manager.inner().databases().deleteAsync(resourceGroupName, sqlServerName, name);
    }

    @Override
    public void deleteById(String id) {
        Objects.requireNonNull(id);
        this.deleteBySqlServer(ResourceUtils.groupFromResourceId(id),
            ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
            ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        Objects.requireNonNull(id);
        return this.deleteBySqlServerAsync(ResourceUtils.groupFromResourceId(id),
            ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
            ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String name) {
        if (sqlServer != null) {
            this.deleteBySqlServer(this.sqlServer.resourceGroupName(), this.sqlServer.name(), name);
        }
    }

    @Override
    public Mono<Void> deleteAsync(String name) {
        if (sqlServer == null) {
            return null;
        }
        return this.deleteBySqlServerAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name(), name);
    }

    @Override
    public List<SqlDatabase> listBySqlServer(String resourceGroupName, String sqlServerName) {
        List<SqlDatabase> databasesSet = new ArrayList<>();
        for (DatabaseInner inner : this.manager.inner().databases().listByServer(resourceGroupName, sqlServerName)) {
            databasesSet.add(new SqlDatabaseImpl(resourceGroupName, sqlServerName, inner.getLocation(), inner.getName(), inner, manager));
        }
        return Collections.unmodifiableList(databasesSet);
    }

    @Override
    public PagedFlux<SqlDatabase> listBySqlServerAsync(final String resourceGroupName, final String sqlServerName) {
        return this.manager.inner().databases().listByServerAsync(resourceGroupName, sqlServerName)
            .mapPage(inner -> new SqlDatabaseImpl(resourceGroupName, sqlServerName, inner.getLocation(), inner.getName(), inner, manager));
    }

    @Override
    public List<SqlDatabase> listBySqlServer(SqlServer sqlServer) {
        List<SqlDatabase> firewallRuleSet = new ArrayList<>();
        if (sqlServer != null) {
            for (DatabaseInner inner : this.manager.inner().databases().listByServer(sqlServer.resourceGroupName(), sqlServer.name())) {
                firewallRuleSet.add(new SqlDatabaseImpl(inner.getName(), (SqlServerImpl) sqlServer, inner, manager));
            }
        }
        return Collections.unmodifiableList(firewallRuleSet);
    }

    @Override
    public PagedFlux<SqlDatabase> listBySqlServerAsync(final SqlServer sqlServer) {
        return sqlServer.manager().inner().databases().listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .mapPage(inner -> new SqlDatabaseImpl(inner.getName(), (SqlServerImpl) sqlServer, inner, sqlServer.manager()));
    }

    @Override
    public List<SqlDatabase> list() {
        if (sqlServer == null) {
            return null;
        }
        return this.listBySqlServer(this.sqlServer.resourceGroupName(), this.sqlServer.name());
    }

    @Override
    public PagedFlux<SqlDatabase> listAsync() {
        if (sqlServer == null) {
            return null;
        }
        return this.listBySqlServerAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name());
    }

    @Override
    public SqlDatabaseImpl define(String name) {
        SqlDatabaseImpl result = this.sqlDatabases.defineIndependentDatabase(name);
        return (this.sqlServer != null) ? result.withExistingSqlServer(this.sqlServer) : result;
    }
}
