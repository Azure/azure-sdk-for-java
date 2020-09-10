// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.EncryptionProtectorName;
import com.azure.resourcemanager.sql.models.SqlEncryptionProtector;
import com.azure.resourcemanager.sql.models.SqlEncryptionProtectorOperations;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.fluent.inner.EncryptionProtectorInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SQL Encryption Protector operations. */
public class SqlEncryptionProtectorOperationsImpl
    implements SqlEncryptionProtectorOperations,
        SqlEncryptionProtectorOperations.SqlEncryptionProtectorActionsDefinition {

    protected SqlServerManager sqlServerManager;
    protected SqlServer sqlServer;

    SqlEncryptionProtectorOperationsImpl(SqlServer parent, SqlServerManager sqlServerManager) {
        Objects.requireNonNull(sqlServerManager);
        Objects.requireNonNull(parent);
        this.sqlServer = parent;
        this.sqlServerManager = sqlServerManager;
    }

    SqlEncryptionProtectorOperationsImpl(SqlServerManager sqlServerManager) {
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
    }

    @Override
    public SqlEncryptionProtector getBySqlServer(String resourceGroupName, String sqlServerName) {
        EncryptionProtectorInner encryptionProtectorInner =
            this.sqlServerManager.inner().getEncryptionProtectors().get(resourceGroupName, sqlServerName,
                EncryptionProtectorName.CURRENT);
        return encryptionProtectorInner != null
            ? new SqlEncryptionProtectorImpl(
                resourceGroupName, sqlServerName, encryptionProtectorInner, this.sqlServerManager)
            : null;
    }

    @Override
    public Mono<SqlEncryptionProtector> getBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName) {
        final SqlEncryptionProtectorOperationsImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getEncryptionProtectors()
            .getAsync(resourceGroupName, sqlServerName, EncryptionProtectorName.CURRENT)
            .map(
                encryptionProtectorInner ->
                    new SqlEncryptionProtectorImpl(
                        resourceGroupName, sqlServerName, encryptionProtectorInner, self.sqlServerManager));
    }

    @Override
    public SqlEncryptionProtector getBySqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        EncryptionProtectorInner encryptionProtectorInner =
            sqlServer.manager().inner().getEncryptionProtectors().get(sqlServer.resourceGroupName(), sqlServer.name(),
                EncryptionProtectorName.CURRENT);
        return encryptionProtectorInner != null
            ? new SqlEncryptionProtectorImpl((SqlServerImpl) sqlServer, encryptionProtectorInner, sqlServer.manager())
            : null;
    }

    @Override
    public Mono<SqlEncryptionProtector> getBySqlServerAsync(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .inner()
            .getEncryptionProtectors()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name(), EncryptionProtectorName.CURRENT)
            .map(
                encryptionProtectorInner ->
                    new SqlEncryptionProtectorImpl(
                        (SqlServerImpl) sqlServer, encryptionProtectorInner, sqlServer.manager()));
    }

    @Override
    public SqlEncryptionProtector get() {
        if (this.sqlServer == null) {
            return null;
        }
        return this.getBySqlServer(this.sqlServer);
    }

    @Override
    public Mono<SqlEncryptionProtector> getAsync() {
        if (this.sqlServer == null) {
            return null;
        }
        return this.getBySqlServerAsync(this.sqlServer);
    }

    @Override
    public SqlEncryptionProtector getById(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServer(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)));
    }

    @Override
    public Mono<SqlEncryptionProtector> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServerAsync(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)));
    }

    @Override
    public List<SqlEncryptionProtector> list() {
        if (this.sqlServer == null) {
            return null;
        }
        return this.listBySqlServer(this.sqlServer);
    }

    @Override
    public PagedFlux<SqlEncryptionProtector> listAsync() {
        if (sqlServer == null) {
            return null;
        }
        return this.listBySqlServerAsync(this.sqlServer);
    }

    @Override
    public List<SqlEncryptionProtector> listBySqlServer(String resourceGroupName, String sqlServerName) {
        List<SqlEncryptionProtector> encryptionProtectors = new ArrayList<>();
        PagedIterable<EncryptionProtectorInner> encryptionProtectorInners =
            this.sqlServerManager.inner().getEncryptionProtectors().listByServer(resourceGroupName, sqlServerName);
        for (EncryptionProtectorInner inner : encryptionProtectorInners) {
            encryptionProtectors
                .add(
                    new SqlEncryptionProtectorImpl(resourceGroupName, sqlServerName, inner, this.sqlServerManager));
        }
        return Collections.unmodifiableList(encryptionProtectors);
    }

    @Override
    public PagedFlux<SqlEncryptionProtector> listBySqlServerAsync(
        final String resourceGroupName, final String sqlServerName) {
        final SqlEncryptionProtectorOperationsImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getEncryptionProtectors()
            .listByServerAsync(resourceGroupName, sqlServerName)
            .mapPage(
                encryptionProtectorInner ->
                    new SqlEncryptionProtectorImpl(
                        resourceGroupName, sqlServerName, encryptionProtectorInner, self.sqlServerManager));
    }

    @Override
    public List<SqlEncryptionProtector> listBySqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        List<SqlEncryptionProtector> encryptionProtectors = new ArrayList<>();
        PagedIterable<EncryptionProtectorInner> encryptionProtectorInners =
            sqlServer
                .manager()
                .inner()
                .getEncryptionProtectors()
                .listByServer(sqlServer.resourceGroupName(), sqlServer.name());
        for (EncryptionProtectorInner inner : encryptionProtectorInners) {
            encryptionProtectors
                .add(new SqlEncryptionProtectorImpl((SqlServerImpl) sqlServer, inner, sqlServer.manager()));
        }
        return Collections.unmodifiableList(encryptionProtectors);
    }

    @Override
    public PagedFlux<SqlEncryptionProtector> listBySqlServerAsync(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        return sqlServer
            .manager()
            .inner()
            .getEncryptionProtectors()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .mapPage(
                encryptionProtectorInner ->
                    new SqlEncryptionProtectorImpl(
                        (SqlServerImpl) sqlServer, encryptionProtectorInner, sqlServer.manager()));
    }
}
