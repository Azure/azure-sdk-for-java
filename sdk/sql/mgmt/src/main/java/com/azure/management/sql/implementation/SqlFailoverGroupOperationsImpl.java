/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.management.sql.SqlFailoverGroup;
import com.azure.management.sql.SqlFailoverGroupOperations;
import com.azure.management.sql.SqlServer;
import com.azure.management.sql.models.FailoverGroupInner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for SQL Failover Group operations.
 */
public class SqlFailoverGroupOperationsImpl
    extends
        SqlChildrenOperationsImpl<SqlFailoverGroup>
    implements
        SqlFailoverGroupOperations,
        SqlFailoverGroupOperations.SqlFailoverGroupActionsDefinition {

    SqlFailoverGroupOperationsImpl(SqlServer parent, SqlServerManager sqlServerManager) {
        super(parent, sqlServerManager);
        Objects.requireNonNull(parent);
    }

    SqlFailoverGroupOperationsImpl(SqlServerManager sqlServerManager) {
        super(null, sqlServerManager);
    }

    @Override
    public SqlFailoverGroup getBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        FailoverGroupInner failoverGroupInner = this.sqlServerManager.inner().failoverGroups()
            .get(resourceGroupName, sqlServerName, name);
        return failoverGroupInner != null ? new SqlFailoverGroupImpl(name, failoverGroupInner, this.sqlServerManager) : null;
    }

    @Override
    public Mono<SqlFailoverGroup> getBySqlServerAsync(final String resourceGroupName, final String sqlServerName, final String name) {
        final SqlFailoverGroupOperationsImpl self = this;
        return this.sqlServerManager.inner().failoverGroups()
            .getAsync(resourceGroupName, sqlServerName, name)
            .map(failoverGroupInner -> new SqlFailoverGroupImpl(name, failoverGroupInner, self.sqlServerManager));
    }

    @Override
    public SqlFailoverGroup getBySqlServer(SqlServer sqlServer, String name) {
        Objects.requireNonNull(sqlServer);
        FailoverGroupInner failoverGroupInner = sqlServer.manager().inner().failoverGroups()
            .get(sqlServer.resourceGroupName(), sqlServer.name(), name);
        return failoverGroupInner != null ? new SqlFailoverGroupImpl(name, (SqlServerImpl) sqlServer, failoverGroupInner, sqlServer.manager()) : null;
    }

    @Override
    public Mono<SqlFailoverGroup> getBySqlServerAsync(final SqlServer sqlServer, final String name) {
        Objects.requireNonNull(sqlServer);
        return sqlServer.manager().inner().failoverGroups()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name(), name)
            .map(failoverGroupInner -> new SqlFailoverGroupImpl(name, (SqlServerImpl) sqlServer, failoverGroupInner, sqlServer.manager()));
    }

    @Override
    public void deleteBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        this.sqlServerManager.inner().failoverGroups().delete(resourceGroupName, sqlServerName, name);
    }

    @Override
    public Mono<Void> deleteBySqlServerAsync(String resourceGroupName, String sqlServerName, String name) {
        return this.sqlServerManager.inner().failoverGroups().deleteAsync(resourceGroupName, sqlServerName, name);
    }

    @Override
    public List<SqlFailoverGroup> listBySqlServer(String resourceGroupName, String sqlServerName) {
        List<SqlFailoverGroup> failoverGroups = new ArrayList<>();
        PagedIterable<FailoverGroupInner> failoverGroupInners = this.sqlServerManager.inner().failoverGroups()
            .listByServer(resourceGroupName, sqlServerName);
        if (failoverGroupInners != null) {
            for (FailoverGroupInner inner : failoverGroupInners) {
                failoverGroups.add(new SqlFailoverGroupImpl(inner.getName(), inner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableList(failoverGroups);
    }

    @Override
    public PagedFlux<SqlFailoverGroup> listBySqlServerAsync(final String resourceGroupName, final String sqlServerName) {
        final SqlFailoverGroupOperationsImpl self = this;
        return this.sqlServerManager.inner().failoverGroups()
            .listByServerAsync(resourceGroupName, sqlServerName)
            .mapPage(failoverGroupInner -> new SqlFailoverGroupImpl(failoverGroupInner.getName(), failoverGroupInner, self.sqlServerManager));
    }

    @Override
    public List<SqlFailoverGroup> listBySqlServer(final SqlServer sqlServer) {
        List<SqlFailoverGroup> failoverGroups = new ArrayList<>();
        PagedIterable<FailoverGroupInner> failoverGroupInners = sqlServer.manager().inner().failoverGroups()
            .listByServer(sqlServer.resourceGroupName(), sqlServer.name());
        if (failoverGroupInners != null) {
            for (FailoverGroupInner inner : failoverGroupInners) {
                failoverGroups.add(new SqlFailoverGroupImpl(inner.getName(), (SqlServerImpl) sqlServer, inner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableList(failoverGroups);
    }

    @Override
    public PagedFlux<SqlFailoverGroup> listBySqlServerAsync(final SqlServer sqlServer) {
        return sqlServer.manager().inner().failoverGroups()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .mapPage(failoverGroupInner -> new SqlFailoverGroupImpl(failoverGroupInner.getName(), (SqlServerImpl) sqlServer, failoverGroupInner, sqlServer.manager()));
    }

    @Override
    public SqlFailoverGroupImpl define(String name) {
        SqlFailoverGroupImpl result = new SqlFailoverGroupImpl(name, new FailoverGroupInner(), this.sqlServerManager);
        result.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return (this.sqlServer != null) ? result.withExistingSqlServer(this.sqlServer) : result;
    }

    @Override
    public SqlFailoverGroup failover(String failoverGroupName) {
        Objects.requireNonNull(this.sqlServer);
        FailoverGroupInner failoverGroupInner = sqlServer.manager().inner().failoverGroups()
            .failover(sqlServer.resourceGroupName(), sqlServer.name(), failoverGroupName);
        return failoverGroupInner != null ? new SqlFailoverGroupImpl(failoverGroupInner.getName(), (SqlServerImpl) this.sqlServer, failoverGroupInner, sqlServer.manager()) : null;
    }

    @Override
    public Mono<SqlFailoverGroup> failoverAsync(String failoverGroupName) {
        Objects.requireNonNull(this.sqlServer);
        final SqlFailoverGroupOperationsImpl self = this;
        return sqlServer.manager().inner().failoverGroups()
            .failoverAsync(sqlServer.resourceGroupName(), sqlServer.name(), failoverGroupName)
            .map(failoverGroupInner -> new SqlFailoverGroupImpl(failoverGroupInner.getName(), (SqlServerImpl) sqlServer, failoverGroupInner, sqlServer.manager()));
    }

    @Override
    public SqlFailoverGroup forceFailoverAllowDataLoss(String failoverGroupName) {
        Objects.requireNonNull(this.sqlServer);
        FailoverGroupInner failoverGroupInner = sqlServer.manager().inner().failoverGroups()
            .forceFailoverAllowDataLoss(sqlServer.resourceGroupName(), sqlServer.name(), failoverGroupName);
        return failoverGroupInner != null ? new SqlFailoverGroupImpl(failoverGroupInner.getName(), (SqlServerImpl) this.sqlServer, failoverGroupInner, sqlServer.manager()) : null;
    }

    @Override
    public Mono<SqlFailoverGroup> forceFailoverAllowDataLossAsync(String failoverGroupName) {
        Objects.requireNonNull(this.sqlServer);
        final SqlFailoverGroupOperationsImpl self = this;
        return sqlServer.manager().inner().failoverGroups()
            .forceFailoverAllowDataLossAsync(sqlServer.resourceGroupName(), sqlServer.name(), failoverGroupName)
            .map(failoverGroupInner -> new SqlFailoverGroupImpl(failoverGroupInner.getName(), (SqlServerImpl) sqlServer, failoverGroupInner, sqlServer.manager()));
    }

    @Override
    public SqlFailoverGroup failover(String resourceGroupName, String serverName, String failoverGroupName) {
        FailoverGroupInner failoverGroupInner = this.sqlServerManager.inner().failoverGroups()
            .failover(resourceGroupName, serverName, failoverGroupName);
        return failoverGroupInner != null ? new SqlFailoverGroupImpl(failoverGroupInner.getName(), failoverGroupInner, this.sqlServerManager) : null;
    }

    @Override
    public Mono<SqlFailoverGroup> failoverAsync(final String resourceGroupName, final String serverName, final String failoverGroupName) {
        final SqlFailoverGroupOperationsImpl self = this;
        return this.sqlServerManager.inner().failoverGroups()
            .failoverAsync(resourceGroupName, serverName, failoverGroupName)
            .map(failoverGroupInner -> new SqlFailoverGroupImpl(failoverGroupInner.getName(), failoverGroupInner, self.sqlServerManager));
    }

    @Override
    public SqlFailoverGroup forceFailoverAllowDataLoss(String resourceGroupName, String serverName, String failoverGroupName) {
        FailoverGroupInner failoverGroupInner = this.sqlServerManager.inner().failoverGroups()
            .forceFailoverAllowDataLoss(resourceGroupName, serverName, failoverGroupName);
        return failoverGroupInner != null ? new SqlFailoverGroupImpl(failoverGroupInner.getName(), failoverGroupInner, this.sqlServerManager) : null;
    }

    @Override
    public Mono<SqlFailoverGroup> forceFailoverAllowDataLossAsync(final String resourceGroupName, final String serverName, String failoverGroupName) {
        final SqlFailoverGroupOperationsImpl self = this;
        return this.sqlServerManager.inner().failoverGroups()
            .forceFailoverAllowDataLossAsync(resourceGroupName, serverName, failoverGroupName)
            .map(failoverGroupInner -> new SqlFailoverGroupImpl(failoverGroupInner.getName(), failoverGroupInner, self.sqlServerManager));
    }
}
