// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlSyncMember;
import com.azure.resourcemanager.sql.models.SqlSyncMemberOperations;
import com.azure.resourcemanager.sql.fluent.inner.SyncMemberInner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SQL Sync Member operations. */
public class SqlSyncMemberOperationsImpl
    implements SqlSyncMemberOperations, SqlSyncMemberOperations.SqlSyncMemberActionsDefinition {

    protected SqlServerManager sqlServerManager;
    protected SqlSyncGroupImpl sqlSyncGroup;

    SqlSyncMemberOperationsImpl(SqlSyncGroupImpl parent, SqlServerManager sqlServerManager) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlSyncGroup = parent;
        this.sqlServerManager = sqlServerManager;
    }

    SqlSyncMemberOperationsImpl(SqlServerManager sqlServerManager) {
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
    }

    @Override
    public SqlSyncMember getBySqlServer(
        String resourceGroupName, String sqlServerName, String databaseName, String syncGroupName, String name) {
        SyncMemberInner syncMemberInner =
            this
                .sqlServerManager
                .inner()
                .getSyncMembers()
                .get(resourceGroupName, sqlServerName, databaseName, syncGroupName, name);
        return syncMemberInner != null
            ? new SqlSyncMemberImpl(
                resourceGroupName,
                sqlServerName,
                databaseName,
                syncGroupName,
                name,
                syncMemberInner,
                this.sqlServerManager)
            : null;
    }

    @Override
    public Mono<SqlSyncMember> getBySqlServerAsync(
        final String resourceGroupName,
        final String sqlServerName,
        final String databaseName,
        final String syncGroupName,
        final String name) {
        return this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .getAsync(resourceGroupName, sqlServerName, databaseName, syncGroupName, name)
            .map(
                syncMemberInner ->
                    new SqlSyncMemberImpl(
                        resourceGroupName,
                        sqlServerName,
                        databaseName,
                        syncGroupName,
                        name,
                        syncMemberInner,
                        sqlServerManager));
    }

    @Override
    public SqlSyncMember get(String name) {
        if (this.sqlSyncGroup == null) {
            return null;
        }
        return this
            .getBySqlServer(
                this.sqlSyncGroup.resourceGroupName(),
                this.sqlSyncGroup.sqlServerName(),
                this.sqlSyncGroup.sqlDatabaseName(),
                this.sqlSyncGroup.name(),
                name);
    }

    @Override
    public Mono<SqlSyncMember> getAsync(String name) {
        if (this.sqlSyncGroup == null) {
            return null;
        }
        return this
            .getBySqlServerAsync(
                this.sqlSyncGroup.resourceGroupName(),
                this.sqlSyncGroup.sqlServerName(),
                this.sqlSyncGroup.sqlDatabaseName(),
                this.sqlSyncGroup.name(),
                name);
    }

    @Override
    public SqlSyncMember getById(String id) {
        Objects.requireNonNull(id);
        try {
            ResourceId resourceId = ResourceId.fromString(id);
            return this
                .getBySqlServer(
                    resourceId.resourceGroupName(),
                    resourceId.parent().parent().parent().name(),
                    resourceId.parent().parent().name(),
                    resourceId.parent().name(),
                    resourceId.name());
        } catch (NullPointerException e) {
        }
        return null;
    }

    @Override
    public Mono<SqlSyncMember> getByIdAsync(String id) {
        Objects.requireNonNull(id);
        try {
            ResourceId resourceId = ResourceId.fromString(id);
            return this
                .getBySqlServerAsync(
                    resourceId.resourceGroupName(),
                    resourceId.parent().parent().parent().name(),
                    resourceId.parent().parent().name(),
                    resourceId.parent().name(),
                    resourceId.name());
        } catch (NullPointerException e) {
        }
        return null;
    }

    @Override
    public void delete(String name) {
        if (this.sqlSyncGroup == null) {
            return;
        }
        this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .delete(
                this.sqlSyncGroup.resourceGroupName(),
                this.sqlSyncGroup.sqlServerName(),
                this.sqlSyncGroup.sqlDatabaseName(),
                this.sqlSyncGroup.name(),
                name);
    }

    @Override
    public Mono<Void> deleteAsync(String name) {
        if (this.sqlSyncGroup == null) {
            return null;
        }
        return this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .deleteAsync(
                this.sqlSyncGroup.resourceGroupName(),
                this.sqlSyncGroup.sqlServerName(),
                this.sqlSyncGroup.sqlDatabaseName(),
                this.sqlSyncGroup.name(),
                name);
    }

    @Override
    public void deleteById(String id) {
        try {
            ResourceId resourceId = ResourceId.fromString(id);
            this
                .sqlServerManager
                .inner()
                .getSyncMembers()
                .delete(
                    resourceId.resourceGroupName(),
                    resourceId.parent().parent().parent().name(),
                    resourceId.parent().parent().name(),
                    resourceId.parent().name(),
                    resourceId.name());
        } catch (NullPointerException e) {
        }
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        try {
            ResourceId resourceId = ResourceId.fromString(id);
            return this
                .sqlServerManager
                .inner()
                .getSyncMembers()
                .deleteAsync(
                    resourceId.resourceGroupName(),
                    resourceId.parent().parent().parent().name(),
                    resourceId.parent().parent().name(),
                    resourceId.parent().name(),
                    resourceId.name());
        } catch (NullPointerException e) {
        }
        return null;
    }

    @Override
    public List<SqlSyncMember> list() {
        List<SqlSyncMember> sqlSyncMembers = new ArrayList<>();
        if (this.sqlSyncGroup != null) {
            PagedIterable<SyncMemberInner> syncMemberInners =
                this
                    .sqlServerManager
                    .inner()
                    .getSyncMembers()
                    .listBySyncGroup(
                        this.sqlSyncGroup.resourceGroupName(),
                        this.sqlSyncGroup.sqlServerName(),
                        this.sqlSyncGroup.sqlDatabaseName(),
                        this.sqlSyncGroup.name());
            for (SyncMemberInner syncMemberInner : syncMemberInners) {
                sqlSyncMembers
                    .add(
                        new SqlSyncMemberImpl(
                            syncMemberInner.name(), this.sqlSyncGroup, syncMemberInner, this.sqlServerManager));
            }
        }
        return Collections.unmodifiableList(sqlSyncMembers);
    }

    @Override
    public PagedFlux<SqlSyncMember> listAsync() {
        final SqlSyncMemberOperationsImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .listBySyncGroupAsync(
                this.sqlSyncGroup.resourceGroupName(),
                this.sqlSyncGroup.sqlServerName(),
                this.sqlSyncGroup.sqlDatabaseName(),
                this.sqlSyncGroup.name())
            .mapPage(
                syncMemberInner ->
                    new SqlSyncMemberImpl(
                        syncMemberInner.name(), self.sqlSyncGroup, syncMemberInner, self.sqlServerManager));
    }

    @Override
    public SqlSyncMemberImpl define(String syncMemberName) {
        SqlSyncMemberImpl result = new SqlSyncMemberImpl(syncMemberName, new SyncMemberInner(), this.sqlServerManager);
        result.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return (this.sqlSyncGroup != null) ? result.withExistingSyncGroup(this.sqlSyncGroup) : result;
    }
}
