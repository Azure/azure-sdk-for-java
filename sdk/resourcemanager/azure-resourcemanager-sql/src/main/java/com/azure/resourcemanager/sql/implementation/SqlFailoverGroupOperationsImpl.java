// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlFailoverGroup;
import com.azure.resourcemanager.sql.models.SqlFailoverGroupOperations;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.fluent.models.FailoverGroupInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** Implementation for SQL Failover Group operations. */
public class SqlFailoverGroupOperationsImpl extends SqlChildrenOperationsImpl<SqlFailoverGroup>
    implements SqlFailoverGroupOperations, SqlFailoverGroupOperations.SqlFailoverGroupActionsDefinition {

    SqlFailoverGroupOperationsImpl(SqlServer parent, SqlServerManager sqlServerManager) {
        super(parent, sqlServerManager);
        Objects.requireNonNull(parent);
    }

    SqlFailoverGroupOperationsImpl(SqlServerManager sqlServerManager) {
        super(null, sqlServerManager);
    }

    @Override
    public SqlFailoverGroup getBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        FailoverGroupInner failoverGroupInner =
            this.sqlServerManager.serviceClient().getFailoverGroups().get(resourceGroupName, sqlServerName, name);
        return failoverGroupInner != null
            ? new SqlFailoverGroupImpl(name, failoverGroupInner, this.sqlServerManager)
            : null;
    }

    @Override
    public Mono<SqlFailoverGroup> getBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName, final String name) {
        final SqlFailoverGroupOperationsImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getFailoverGroups()
            .getAsync(resourceGroupName, sqlServerName, name)
            .map(failoverGroupInner -> new SqlFailoverGroupImpl(name, failoverGroupInner, self.sqlServerManager));
    }

    @Override
    public SqlFailoverGroup getBySqlServer(SqlServer sqlServer, String name) {
        Objects.requireNonNull(sqlServer);
        FailoverGroupInner failoverGroupInner =
            sqlServer
                .manager()
                .serviceClient()
                .getFailoverGroups()
                .get(sqlServer.resourceGroupName(), sqlServer.name(), name);
        return failoverGroupInner != null
            ? new SqlFailoverGroupImpl(name, (SqlServerImpl) sqlServer, failoverGroupInner, sqlServer.manager())
            : null;
    }

    @Override
    public Mono<SqlFailoverGroup> getBySqlServerAsync(final SqlServer sqlServer, final String name) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .serviceClient()
            .getFailoverGroups()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name(), name)
            .map(
                failoverGroupInner ->
                    new SqlFailoverGroupImpl(name, (SqlServerImpl) sqlServer, failoverGroupInner, sqlServer.manager()));
    }

    @Override
    public void deleteBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        this.sqlServerManager.serviceClient().getFailoverGroups().delete(resourceGroupName, sqlServerName, name);
    }

    @Override
    public Mono<Void> deleteBySqlServerAsync(String resourceGroupName, String sqlServerName, String name) {
        return this
            .sqlServerManager
            .serviceClient()
            .getFailoverGroups()
            .deleteAsync(resourceGroupName, sqlServerName, name);
    }

    @Override
    public List<SqlFailoverGroup> listBySqlServer(String resourceGroupName, String sqlServerName) {
        List<SqlFailoverGroup> failoverGroups = new ArrayList<>();
        PagedIterable<FailoverGroupInner> failoverGroupInners =
            this.sqlServerManager.serviceClient().getFailoverGroups().listByServer(resourceGroupName, sqlServerName);
        for (FailoverGroupInner inner : failoverGroupInners) {
            failoverGroups.add(new SqlFailoverGroupImpl(inner.name(), inner, this.sqlServerManager));
        }
        return Collections.unmodifiableList(failoverGroups);
    }

    @Override
    public PagedFlux<SqlFailoverGroup> listBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName) {
        final SqlFailoverGroupOperationsImpl self = this;
        return PagedConverter.mapPage(this
            .sqlServerManager
            .serviceClient()
            .getFailoverGroups()
            .listByServerAsync(resourceGroupName, sqlServerName),
                failoverGroupInner ->
                    new SqlFailoverGroupImpl(failoverGroupInner.name(), failoverGroupInner, self.sqlServerManager));
    }

    @Override
    public List<SqlFailoverGroup> listBySqlServer(final SqlServer sqlServer) {
        List<SqlFailoverGroup> failoverGroups = new ArrayList<>();
        PagedIterable<FailoverGroupInner> failoverGroupInners =
            sqlServer
                .manager()
                .serviceClient()
                .getFailoverGroups()
                .listByServer(sqlServer.resourceGroupName(), sqlServer.name());
        for (FailoverGroupInner inner : failoverGroupInners) {
            failoverGroups
                .add(new SqlFailoverGroupImpl(inner.name(), (SqlServerImpl) sqlServer, inner, this.sqlServerManager));
        }
        return Collections.unmodifiableList(failoverGroups);
    }

    @Override
    public PagedFlux<SqlFailoverGroup> listBySqlServerAsync(final SqlServer sqlServer) {
        return PagedConverter.mapPage(sqlServer
            .manager()
            .serviceClient()
            .getFailoverGroups()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name()),
                failoverGroupInner ->
                    new SqlFailoverGroupImpl(
                        failoverGroupInner.name(), (SqlServerImpl) sqlServer, failoverGroupInner, sqlServer.manager()));
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
        FailoverGroupInner failoverGroupInner =
            sqlServer
                .manager()
                .serviceClient()
                .getFailoverGroups()
                .failover(sqlServer.resourceGroupName(), sqlServer.name(), failoverGroupName);
        return failoverGroupInner != null
            ? new SqlFailoverGroupImpl(
                failoverGroupInner.name(), (SqlServerImpl) this.sqlServer, failoverGroupInner, sqlServer.manager())
            : null;
    }

    @Override
    public Mono<SqlFailoverGroup> failoverAsync(String failoverGroupName) {
        Objects.requireNonNull(this.sqlServer);
        return sqlServer
            .manager()
            .serviceClient()
            .getFailoverGroups()
            .failoverAsync(sqlServer.resourceGroupName(), sqlServer.name(), failoverGroupName)
            .map(
                failoverGroupInner ->
                    new SqlFailoverGroupImpl(
                        failoverGroupInner.name(), (SqlServerImpl) sqlServer, failoverGroupInner, sqlServer.manager()));
    }

    @Override
    public SqlFailoverGroup forceFailoverAllowDataLoss(String failoverGroupName) {
        Objects.requireNonNull(this.sqlServer);
        FailoverGroupInner failoverGroupInner =
            sqlServer
                .manager()
                .serviceClient()
                .getFailoverGroups()
                .forceFailoverAllowDataLoss(sqlServer.resourceGroupName(), sqlServer.name(), failoverGroupName);
        return failoverGroupInner != null
            ? new SqlFailoverGroupImpl(
                failoverGroupInner.name(), (SqlServerImpl) this.sqlServer, failoverGroupInner, sqlServer.manager())
            : null;
    }

    @Override
    public Mono<SqlFailoverGroup> forceFailoverAllowDataLossAsync(String failoverGroupName) {
        Objects.requireNonNull(this.sqlServer);
        return sqlServer
            .manager()
            .serviceClient()
            .getFailoverGroups()
            .forceFailoverAllowDataLossAsync(sqlServer.resourceGroupName(), sqlServer.name(), failoverGroupName)
            .map(
                failoverGroupInner ->
                    new SqlFailoverGroupImpl(
                        failoverGroupInner.name(), (SqlServerImpl) sqlServer, failoverGroupInner, sqlServer.manager()));
    }

    @Override
    public SqlFailoverGroup failover(String resourceGroupName, String serverName, String failoverGroupName) {
        FailoverGroupInner failoverGroupInner =
            this
                .sqlServerManager
                .serviceClient()
                .getFailoverGroups()
                .failover(resourceGroupName, serverName, failoverGroupName);
        return failoverGroupInner != null
            ? new SqlFailoverGroupImpl(failoverGroupInner.name(), failoverGroupInner, this.sqlServerManager)
            : null;
    }

    @Override
    public Mono<SqlFailoverGroup> failoverAsync(
        final String resourceGroupName, final String serverName, final String failoverGroupName) {
        final SqlFailoverGroupOperationsImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getFailoverGroups()
            .failoverAsync(resourceGroupName, serverName, failoverGroupName)
            .map(
                failoverGroupInner ->
                    new SqlFailoverGroupImpl(failoverGroupInner.name(), failoverGroupInner, self.sqlServerManager));
    }

    @Override
    public SqlFailoverGroup forceFailoverAllowDataLoss(
        String resourceGroupName, String serverName, String failoverGroupName) {
        FailoverGroupInner failoverGroupInner =
            this
                .sqlServerManager
                .serviceClient()
                .getFailoverGroups()
                .forceFailoverAllowDataLoss(resourceGroupName, serverName, failoverGroupName);
        return failoverGroupInner != null
            ? new SqlFailoverGroupImpl(failoverGroupInner.name(), failoverGroupInner, this.sqlServerManager)
            : null;
    }

    @Override
    public Mono<SqlFailoverGroup> forceFailoverAllowDataLossAsync(
        final String resourceGroupName, final String serverName, String failoverGroupName) {
        final SqlFailoverGroupOperationsImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getFailoverGroups()
            .forceFailoverAllowDataLossAsync(resourceGroupName, serverName, failoverGroupName)
            .map(
                failoverGroupInner ->
                    new SqlFailoverGroupImpl(failoverGroupInner.name(), failoverGroupInner, self.sqlServerManager));
    }
}
