// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlChildrenOperations;
import com.azure.resourcemanager.sql.models.SqlServer;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;

/**
 * Implementation for SQL Server children operations.
 *
 * @param <FluentModelT> the fluent model type of the child resource
 */
public abstract class SqlChildrenOperationsImpl<FluentModelT>
    implements SqlChildrenOperations<FluentModelT>, SqlChildrenOperations.SqlChildrenActionsDefinition<FluentModelT> {

    protected SqlServerManager sqlServerManager;
    protected SqlServer sqlServer;

    SqlChildrenOperationsImpl(SqlServer parent, SqlServerManager sqlServerManager) {
        Objects.requireNonNull(sqlServerManager);
        this.sqlServer = parent;
        this.sqlServerManager = sqlServerManager;
    }

    @Override
    public FluentModelT get(String name) {
        if (this.sqlServer == null) {
            return null;
        }
        return this.getBySqlServer(this.sqlServer, name);
    }

    @Override
    public Mono<FluentModelT> getAsync(String name) {
        if (this.sqlServer == null) {
            return null;
        }
        return this.getBySqlServerAsync(this.sqlServer, name);
    }

    @Override
    public FluentModelT getById(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServer(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Mono<FluentModelT> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        return this
            .getBySqlServerAsync(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(ResourceUtils.parentRelativePathFromResourceId(id)),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String name) {
        if (this.sqlServer != null) {
            this.deleteBySqlServer(this.sqlServer.resourceGroupName(), this.sqlServer.name(), name);
        }
    }

    @Override
    public Mono<Void> deleteAsync(String name) {
        if (this.sqlServer == null) {
            return null;
        }
        return this.deleteBySqlServerAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name(), name);
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
    public List<FluentModelT> list() {
        if (this.sqlServer == null) {
            return null;
        }
        return this.listBySqlServer(this.sqlServer);
    }

    @Override
    public PagedFlux<FluentModelT> listAsync() {
        if (sqlServer == null) {
            return null;
        }
        return this.listBySqlServerAsync(this.sqlServer);
    }
}
