// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerSecurityAlertPolicy;
import com.azure.resourcemanager.sql.models.SqlServerSecurityAlertPolicyOperations;
import com.azure.resourcemanager.sql.fluent.inner.ServerSecurityAlertPolicyInner;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SQL Server Security Alert Policy operations. */
public class SqlServerSecurityAlertPolicyOperationsImpl
    implements SqlServerSecurityAlertPolicyOperations,
        SqlServerSecurityAlertPolicyOperations.SqlServerSecurityAlertPolicyActionsDefinition {

    protected SqlServerManager sqlServerManager;
    protected SqlServer sqlServer;

    SqlServerSecurityAlertPolicyOperationsImpl(SqlServer parent, SqlServerManager sqlServerManager) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServer = parent;
        this.sqlServerManager = sqlServerManager;
    }

    SqlServerSecurityAlertPolicyOperationsImpl(SqlServerManager sqlServerManager) {
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl define() {
        SqlServerSecurityAlertPolicyImpl result =
            new SqlServerSecurityAlertPolicyImpl(new ServerSecurityAlertPolicyInner(), this.sqlServerManager);
        result.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return (this.sqlServer != null) ? result.withExistingSqlServer(this.sqlServer) : result;
    }

    @Override
    public SqlServerSecurityAlertPolicy get() {
        if (this.sqlServer == null) {
            return null;
        }
        return this.getBySqlServer(this.sqlServer);
    }

    @Override
    public Mono<SqlServerSecurityAlertPolicy> getAsync() {
        if (this.sqlServer == null) {
            return null;
        }
        return this.getBySqlServerAsync(this.sqlServer);
    }

    @Override
    public SqlServerSecurityAlertPolicy getBySqlServer(String resourceGroupName, String sqlServerName) {
        ServerSecurityAlertPolicyInner serverSecurityAlertPolicyInner =
            this.sqlServerManager.inner().getServerSecurityAlertPolicies().get(resourceGroupName, sqlServerName);
        return serverSecurityAlertPolicyInner != null
            ? new SqlServerSecurityAlertPolicyImpl(
                resourceGroupName, sqlServerName, serverSecurityAlertPolicyInner, this.sqlServerManager)
            : null;
    }

    @Override
    public Mono<SqlServerSecurityAlertPolicy> getBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName) {
        final SqlServerSecurityAlertPolicyOperationsImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getServerSecurityAlertPolicies()
            .getAsync(resourceGroupName, sqlServerName)
            .map(
                serverSecurityAlertPolicyInner ->
                    new SqlServerSecurityAlertPolicyImpl(
                        resourceGroupName, sqlServerName, serverSecurityAlertPolicyInner, self.sqlServerManager));
    }

    @Override
    public SqlServerSecurityAlertPolicy getBySqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        ServerSecurityAlertPolicyInner serverSecurityAlertPolicyInner =
            sqlServer
                .manager()
                .inner()
                .getServerSecurityAlertPolicies()
                .get(sqlServer.resourceGroupName(), sqlServer.name());
        return serverSecurityAlertPolicyInner != null
            ? new SqlServerSecurityAlertPolicyImpl(
                (SqlServerImpl) sqlServer, serverSecurityAlertPolicyInner, sqlServer.manager())
            : null;
    }

    @Override
    public Mono<SqlServerSecurityAlertPolicy> getBySqlServerAsync(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .inner()
            .getServerSecurityAlertPolicies()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .map(
                serverSecurityAlertPolicyInner ->
                    new SqlServerSecurityAlertPolicyImpl(
                        (SqlServerImpl) sqlServer, serverSecurityAlertPolicyInner, sqlServer.manager()));
    }

    @Override
    public SqlServerSecurityAlertPolicy getById(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServer(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)));
    }

    @Override
    public Mono<SqlServerSecurityAlertPolicy> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServerAsync(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)));
    }
}
