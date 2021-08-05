// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerKey;
import com.azure.resourcemanager.sql.models.SqlServerKeyOperations;
import com.azure.resourcemanager.sql.fluent.models.ServerKeyInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** Implementation for SQL Server Key operations. */
public class SqlServerKeyOperationsImpl extends SqlChildrenOperationsImpl<SqlServerKey>
    implements SqlServerKeyOperations, SqlServerKeyOperations.SqlServerKeyActionsDefinition {

    SqlServerKeyOperationsImpl(SqlServer parent, SqlServerManager sqlServerManager) {
        super(parent, sqlServerManager);
        Objects.requireNonNull(parent);
    }

    SqlServerKeyOperationsImpl(SqlServerManager sqlServerManager) {
        super(null, sqlServerManager);
    }

    @Override
    public SqlServerKey getBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        ServerKeyInner serverKeyInner =
            this.sqlServerManager.serviceClient().getServerKeys().get(resourceGroupName, sqlServerName, name);
        return serverKeyInner != null
            ? new SqlServerKeyImpl(resourceGroupName, sqlServerName, name, serverKeyInner, this.sqlServerManager)
            : null;
    }

    @Override
    public Mono<SqlServerKey> getBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName, final String name) {
        final SqlServerKeyOperationsImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getServerKeys()
            .getAsync(resourceGroupName, sqlServerName, name)
            .map(
                serverKeyInner ->
                    new SqlServerKeyImpl(
                        resourceGroupName, sqlServerName, name, serverKeyInner, self.sqlServerManager));
    }

    @Override
    public SqlServerKey getBySqlServer(final SqlServer sqlServer, final String name) {
        Objects.requireNonNull(sqlServer);
        ServerKeyInner serverKeyInner =
            sqlServer
                .manager()
                .serviceClient()
                .getServerKeys()
                .get(sqlServer.resourceGroupName(), sqlServer.name(), name);
        return serverKeyInner != null
            ? new SqlServerKeyImpl(name, (SqlServerImpl) sqlServer, serverKeyInner, sqlServer.manager())
            : null;
    }

    @Override
    public Mono<SqlServerKey> getBySqlServerAsync(final SqlServer sqlServer, final String name) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .serviceClient()
            .getServerKeys()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name(), name)
            .map(
                serverKeyInner ->
                    new SqlServerKeyImpl(name, (SqlServerImpl) sqlServer, serverKeyInner, sqlServer.manager()));
    }

    @Override
    public void deleteBySqlServer(String resourceGroupName, String sqlServerName, String name) {
        this.sqlServerManager.serviceClient().getServerKeys().delete(resourceGroupName, sqlServerName, name);
    }

    @Override
    public Mono<Void> deleteBySqlServerAsync(String resourceGroupName, String sqlServerName, String name) {
        return this
            .sqlServerManager
            .serviceClient()
            .getServerKeys()
            .deleteAsync(resourceGroupName, sqlServerName, name);
    }

    @Override
    public List<SqlServerKey> listBySqlServer(String resourceGroupName, String sqlServerName) {
        List<SqlServerKey> serverKeys = new ArrayList<>();
        PagedIterable<ServerKeyInner> serverKeyInners =
            this.sqlServerManager.serviceClient().getServerKeys().listByServer(resourceGroupName, sqlServerName);
        for (ServerKeyInner inner : serverKeyInners) {
            serverKeys
                .add(
                    new SqlServerKeyImpl(resourceGroupName, sqlServerName, inner.name(), inner, this.sqlServerManager));
        }
        return Collections.unmodifiableList(serverKeys);
    }

    @Override
    public PagedFlux<SqlServerKey> listBySqlServerAsync(final String resourceGroupName, final String sqlServerName) {
        final SqlServerKeyOperationsImpl self = this;
        return PagedConverter.mapPage(this
            .sqlServerManager
            .serviceClient()
            .getServerKeys()
            .listByServerAsync(resourceGroupName, sqlServerName),
                serverKeyInner ->
                    new SqlServerKeyImpl(
                        resourceGroupName,
                        sqlServerName,
                        serverKeyInner.name(),
                        serverKeyInner,
                        self.sqlServerManager));
    }

    @Override
    public List<SqlServerKey> listBySqlServer(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        List<SqlServerKey> serverKeys = new ArrayList<>();
        PagedIterable<ServerKeyInner> serverKeyInners =
            sqlServer
                .manager()
                .serviceClient()
                .getServerKeys()
                .listByServer(sqlServer.resourceGroupName(), sqlServer.name());
        for (ServerKeyInner inner : serverKeyInners) {
            serverKeys.add(new SqlServerKeyImpl(inner.name(), (SqlServerImpl) sqlServer, inner, sqlServer.manager()));
        }
        return Collections.unmodifiableList(serverKeys);
    }

    @Override
    public PagedFlux<SqlServerKey> listBySqlServerAsync(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        return PagedConverter.mapPage(sqlServer
            .manager()
            .serviceClient()
            .getServerKeys()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name()),
                serverKeyInner ->
                    new SqlServerKeyImpl(
                        serverKeyInner.name(), (SqlServerImpl) sqlServer, serverKeyInner, sqlServer.manager()));
    }

    @Override
    public SqlServerKeyImpl define() {
        SqlServerKeyImpl result = new SqlServerKeyImpl("", new ServerKeyInner(), this.sqlServerManager);
        result.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return (this.sqlServer != null) ? result.withExistingSqlServer(this.sqlServer) : result;
    }
}
