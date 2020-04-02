/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.sql.SqlEncryptionProtector;
import com.azure.management.sql.SqlEncryptionProtectorOperations;
import com.azure.management.sql.SqlServer;
import com.azure.management.sql.models.EncryptionProtectorInner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for SQL Encryption Protector operations.
 */
public class SqlEncryptionProtectorOperationsImpl
    implements
        SqlEncryptionProtectorOperations,
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
        EncryptionProtectorInner encryptionProtectorInner = this.sqlServerManager.inner().encryptionProtectors()
            .get(resourceGroupName, sqlServerName);
        return encryptionProtectorInner != null ? new SqlEncryptionProtectorImpl(resourceGroupName, sqlServerName, encryptionProtectorInner, this.sqlServerManager) : null;
    }

    @Override
    public Mono<SqlEncryptionProtector> getBySqlServerAsync(final String resourceGroupName, final String sqlServerName) {
        final SqlEncryptionProtectorOperationsImpl self = this;
        return this.sqlServerManager.inner().encryptionProtectors()
            .getAsync(resourceGroupName, sqlServerName)
            .map(encryptionProtectorInner -> new SqlEncryptionProtectorImpl(resourceGroupName, sqlServerName, encryptionProtectorInner, self.sqlServerManager));
    }

    @Override
    public SqlEncryptionProtector getBySqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        EncryptionProtectorInner encryptionProtectorInner = sqlServer.manager().inner().encryptionProtectors()
            .get(sqlServer.resourceGroupName(), sqlServer.name());
        return encryptionProtectorInner != null ? new SqlEncryptionProtectorImpl((SqlServerImpl) sqlServer, encryptionProtectorInner, sqlServer.manager()) : null;
    }

    @Override
    public Mono<SqlEncryptionProtector> getBySqlServerAsync(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        return sqlServer.manager().inner().encryptionProtectors()
            .getAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .map(encryptionProtectorInner -> new SqlEncryptionProtectorImpl((SqlServerImpl) sqlServer, encryptionProtectorInner, sqlServer.manager()));
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
        return this.getBySqlServer(ResourceUtils.groupFromResourceId(id),
            ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)));
    }

    @Override
    public Mono<SqlEncryptionProtector> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        return this.getBySqlServerAsync(ResourceUtils.groupFromResourceId(id),
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
        PagedIterable<EncryptionProtectorInner> encryptionProtectorInners = this.sqlServerManager.inner().encryptionProtectors()
            .listByServer(resourceGroupName, sqlServerName);
        if (encryptionProtectorInners != null) {
            for (EncryptionProtectorInner inner : encryptionProtectorInners) {
                encryptionProtectors.add(new SqlEncryptionProtectorImpl(resourceGroupName, sqlServerName, inner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableList(encryptionProtectors);
    }

    @Override
    public PagedFlux<SqlEncryptionProtector> listBySqlServerAsync(final String resourceGroupName, final String sqlServerName) {
        final SqlEncryptionProtectorOperationsImpl self = this;
        return this.sqlServerManager.inner().encryptionProtectors()
            .listByServerAsync(resourceGroupName, sqlServerName)
            .mapPage(encryptionProtectorInner -> new SqlEncryptionProtectorImpl(resourceGroupName, sqlServerName, encryptionProtectorInner, self.sqlServerManager));
    }

    @Override
    public List<SqlEncryptionProtector> listBySqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        List<SqlEncryptionProtector> encryptionProtectors = new ArrayList<>();
        PagedIterable<EncryptionProtectorInner> encryptionProtectorInners = sqlServer.manager().inner().encryptionProtectors()
            .listByServer(sqlServer.resourceGroupName(), sqlServer.name());
        if (encryptionProtectorInners != null) {
            for (EncryptionProtectorInner inner : encryptionProtectorInners) {
                encryptionProtectors.add(new SqlEncryptionProtectorImpl((SqlServerImpl) sqlServer, inner, sqlServer.manager()));
            }
        }
        return Collections.unmodifiableList(encryptionProtectors);
    }

    @Override
    public PagedFlux<SqlEncryptionProtector> listBySqlServerAsync(final SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        return sqlServer.manager().inner().encryptionProtectors()
            .listByServerAsync(sqlServer.resourceGroupName(), sqlServer.name())
            .mapPage(encryptionProtectorInner -> new SqlEncryptionProtectorImpl((SqlServerImpl) sqlServer, encryptionProtectorInner, sqlServer.manager()));
    }
}
