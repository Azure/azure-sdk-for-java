// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseOperations;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.fluent.models.DatabaseInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** Implementation for SQL Firewall Rule operations. */
public class SqlDatabaseOperationsImpl
    implements SqlDatabaseOperations, SqlDatabaseOperations.SqlDatabaseActionsDefinition {

    private SqlServerManager manager;
    private SqlServerImpl sqlServer;
    private SqlDatabasesAsExternalChildResourcesImpl sqlDatabases;

    SqlDatabaseOperationsImpl(SqlServerImpl parent, SqlServerManager manager) {
        Objects.requireNonNull(manager);
        this.sqlServer = parent;
        this.manager = manager;
        this.sqlDatabases =
            new SqlDatabasesAsExternalChildResourcesImpl(this.sqlServer.taskGroup(), manager, "SqlDatabase");
    }

    SqlDatabaseOperationsImpl(SqlServerManager manager) {
        Objects.requireNonNull(manager);
        this.manager = manager;
        this.sqlDatabases = new SqlDatabasesAsExternalChildResourcesImpl(null, manager, "SqlDatabase");
    }

    @Override
    public SqlDatabase getBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        DatabaseInner inner = this.manager.serviceClient().getDatabases().get(resourceGroupName, sqlServerName, name);
        return (inner != null)
            ? new SqlDatabaseImpl(resourceGroupName, sqlServerName, inner.location(), inner.name(), inner, manager)
            : null;
    }

    @Override
    public Mono<SqlDatabase> getBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName, final String name) {
        return this
            .manager
            .serviceClient()
            .getDatabases()
            .getAsync(resourceGroupName, sqlServerName, name)
            .map(
                inner ->
                    new SqlDatabaseImpl(
                        resourceGroupName, sqlServerName, inner.location(), inner.name(), inner, manager));
    }

    @Override
    public SqlDatabase getBySqlServer(SqlServer sqlServer, String name) {
        if (sqlServer == null) {
            return null;
        }
        DatabaseInner inner =
            this.manager.serviceClient().getDatabases().get(sqlServer.resourceGroupName(), sqlServer.name(), name);
        return (inner != null) ? new SqlDatabaseImpl(inner.name(), (SqlServerImpl) sqlServer, inner, manager) : null;
    }

    @Override
    public Mono<SqlDatabase> getBySqlServerAsync(final SqlServer sqlServer, String name) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .serviceClient()
            .getDatabases()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name(), name)
            .map(inner -> new SqlDatabaseImpl(inner.name(), (SqlServerImpl) sqlServer, inner, manager));
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
        return this
            .getBySqlServer(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Mono<SqlDatabase> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServerAsync(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void deleteBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        this.manager.serviceClient().getDatabases().delete(resourceGroupName, sqlServerName, name);
    }

    @Override
    public Mono<Void> deleteBySqlServerAsync(String resourceGroupName, String sqlServerName, String name) {
        return this.manager.serviceClient().getDatabases().deleteAsync(resourceGroupName, sqlServerName, name);
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
        for (DatabaseInner inner
            : this.manager.serviceClient().getDatabases().listByServer(resourceGroupName, sqlServerName)) {
            databasesSet
                .add(
                    new SqlDatabaseImpl(
                        resourceGroupName, sqlServerName, inner.location(), inner.name(), inner, manager));
        }
        return Collections.unmodifiableList(databasesSet);
    }

    @Override
    public PagedFlux<SqlDatabase> listBySqlServerAsync(final String resourceGroupName, final String sqlServerName) {
        return PagedConverter.mapPage(this
            .manager
            .serviceClient()
            .getDatabases()
            .listByServerAsync(resourceGroupName, sqlServerName),
                inner ->
                    new SqlDatabaseImpl(
                        resourceGroupName, sqlServerName, inner.location(), inner.name(), inner, manager));
    }

    @Override
    public List<SqlDatabase> listBySqlServer(SqlServer sqlServer) {
        List<SqlDatabase> firewallRuleSet = new ArrayList<>();
        if (sqlServer != null) {
            for (DatabaseInner inner
                : this
                    .manager
                    .serviceClient()
                    .getDatabases()
                    .listByServer(sqlServer.resourceGroupName(), sqlServer.name())) {
                firewallRuleSet.add(new SqlDatabaseImpl(inner.name(), (SqlServerImpl) sqlServer, inner, manager));
            }
        }
        return Collections.unmodifiableList(firewallRuleSet);
    }

    @Override
    public PagedFlux<SqlDatabase> listBySqlServerAsync(final SqlServer sqlServer) {
        return PagedConverter.mapPage(sqlServer
            .manager()
            .serviceClient()
            .getDatabases()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name()),
            inner -> new SqlDatabaseImpl(inner.name(), (SqlServerImpl) sqlServer, inner, sqlServer.manager()));
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
