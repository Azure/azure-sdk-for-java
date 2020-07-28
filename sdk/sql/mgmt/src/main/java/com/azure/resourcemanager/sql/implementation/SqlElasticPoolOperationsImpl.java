// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlElasticPool;
import com.azure.resourcemanager.sql.models.SqlElasticPoolOperations;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.fluent.inner.ElasticPoolInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SQL Elastic Pool operations. */
public class SqlElasticPoolOperationsImpl
    implements SqlElasticPoolOperations, SqlElasticPoolOperations.SqlElasticPoolActionsDefinition {

    private SqlServerManager manager;
    private SqlServerImpl sqlServer;
    private SqlElasticPoolsAsExternalChildResourcesImpl sqlElasticPools;

    SqlElasticPoolOperationsImpl(SqlServerImpl parent, SqlServerManager manager) {
        Objects.requireNonNull(manager);
        this.sqlServer = parent;
        this.manager = manager;
        this.sqlElasticPools = new SqlElasticPoolsAsExternalChildResourcesImpl(manager, "SqlElasticPool");
    }

    SqlElasticPoolOperationsImpl(SqlServerManager manager) {
        Objects.requireNonNull(manager);
        this.manager = manager;
        this.sqlElasticPools = new SqlElasticPoolsAsExternalChildResourcesImpl(manager, "SqlElasticPool");
    }

    @Override
    public SqlElasticPool getBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        ElasticPoolInner inner = this.manager.inner().getElasticPools().get(resourceGroupName, sqlServerName, name);
        return (inner != null)
            ? new SqlElasticPoolImpl(
                resourceGroupName, sqlServerName, inner.location(), inner.name(), inner, manager)
            : null;
    }

    @Override
    public Mono<SqlElasticPool> getBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName, final String name) {
        return this
            .manager
            .inner()
            .getElasticPools()
            .getAsync(resourceGroupName, sqlServerName, name)
            .map(
                inner ->
                    new SqlElasticPoolImpl(
                        resourceGroupName, sqlServerName, inner.location(), inner.name(), inner, manager));
    }

    @Override
    public SqlElasticPool getBySqlServer(SqlServer sqlServer, String name) {
        if (sqlServer == null) {
            return null;
        }
        ElasticPoolInner inner =
            this.manager.inner().getElasticPools().get(sqlServer.resourceGroupName(), sqlServer.name(), name);
        return (inner != null)
            ? new SqlElasticPoolImpl(inner.name(), (SqlServerImpl) sqlServer, inner, manager)
            : null;
    }

    @Override
    public Mono<SqlElasticPool> getBySqlServerAsync(final SqlServer sqlServer, String name) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .inner()
            .getElasticPools()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name(), name)
            .map(inner -> new SqlElasticPoolImpl(inner.name(), (SqlServerImpl) sqlServer, inner, manager));
    }

    @Override
    public SqlElasticPool get(String name) {
        if (sqlServer == null) {
            return null;
        }
        return this.getBySqlServer(this.sqlServer.resourceGroupName(), this.sqlServer.name(), name);
    }

    @Override
    public Mono<SqlElasticPool> getAsync(String name) {
        if (sqlServer == null) {
            return null;
        }
        return this.getBySqlServerAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name(), name);
    }

    @Override
    public SqlElasticPool getById(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServer(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Mono<SqlElasticPool> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServerAsync(
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
    public void deleteBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        this.manager.inner().getElasticPools().delete(resourceGroupName, sqlServerName, name);
    }

    @Override
    public Mono<Void> deleteBySqlServerAsync(String resourceGroupName, String sqlServerName, String name) {
        return this.manager.inner().getElasticPools().deleteAsync(resourceGroupName, sqlServerName, name);
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
    public List<SqlElasticPool> list() {
        if (sqlServer == null) {
            return null;
        }
        return this.listBySqlServer(this.sqlServer.resourceGroupName(), this.sqlServer.name());
    }

    @Override
    public PagedFlux<SqlElasticPool> listAsync() {
        if (sqlServer == null) {
            return null;
        }
        return this.listBySqlServerAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name());
    }

    @Override
    public List<SqlElasticPool> listBySqlServer(String resourceGroupName, String sqlServerName) {
        List<SqlElasticPool> elasticPoolSet = new ArrayList<>();
        for (ElasticPoolInner inner
            : this.manager.inner().getElasticPools().listByServer(resourceGroupName, sqlServerName)) {
            elasticPoolSet
                .add(
                    new SqlElasticPoolImpl(
                        resourceGroupName, sqlServerName, inner.location(), inner.name(), inner, manager));
        }
        return Collections.unmodifiableList(elasticPoolSet);
    }

    @Override
    public PagedFlux<SqlElasticPool> listBySqlServerAsync(final String resourceGroupName, final String sqlServerName) {
        return this
            .manager
            .inner()
            .getElasticPools()
            .listByServerAsync(resourceGroupName, sqlServerName)
            .mapPage(
                inner ->
                    new SqlElasticPoolImpl(
                        resourceGroupName, sqlServerName, inner.location(), inner.name(), inner, manager));
    }

    @Override
    public List<SqlElasticPool> listBySqlServer(SqlServer sqlServer) {
        List<SqlElasticPool> elasticPoolSet = new ArrayList<>();
        if (sqlServer != null) {
            for (ElasticPoolInner inner
                : this.manager.inner().getElasticPools()
                    .listByServer(sqlServer.resourceGroupName(), sqlServer.name())) {
                elasticPoolSet.add(new SqlElasticPoolImpl(inner.name(), (SqlServerImpl) sqlServer, inner, manager));
            }
        }
        return Collections.unmodifiableList(elasticPoolSet);
    }

    @Override
    public PagedFlux<SqlElasticPool> listBySqlServerAsync(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .inner()
            .getElasticPools()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .mapPage(inner -> new SqlElasticPoolImpl(inner.name(), (SqlServerImpl) sqlServer, inner, manager));
    }

    @Override
    public SqlElasticPoolImpl define(String name) {
        SqlElasticPoolImpl result = sqlElasticPools.defineIndependentElasticPool(name);
        return (this.sqlServer != null) ? result.withExistingSqlServer(this.sqlServer) : result;
    }
}
