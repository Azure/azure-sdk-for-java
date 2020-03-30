/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.management.sql.SqlSyncMember;
import com.azure.management.sql.SqlSyncMemberOperations;
import com.azure.management.sql.models.SyncMemberInner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for SQL Sync Member operations.
 */
public class SqlSyncMemberOperationsImpl
    implements
        SqlSyncMemberOperations,
    SqlSyncMemberOperations.SqlSyncMemberActionsDefinition {

    protected SqlServerManager sqlServerManager;
    protected String resourceGroupName;
    protected String sqlServerName;
    protected String sqlDatabaseName;
    protected String sqlSyncGroupName;
    protected SqlSyncGroupImpl sqlSyncGroup;

    SqlSyncMemberOperationsImpl(SqlSyncGroupImpl parent, SqlServerManager sqlServerManager) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlSyncGroup = parent;
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.sqlServerName();
        this.sqlDatabaseName = parent.sqlDatabaseName();
        this.sqlSyncGroupName = parent.name();
    }

    SqlSyncMemberOperationsImpl(SqlServerManager sqlServerManager) {
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
    }

    @Override
    public SqlSyncMember getBySqlServer(String resourceGroupName, String sqlServerName, String databaseName, String syncGroupName, String name) {
        SyncMemberInner syncMemberInner = this.sqlServerManager.inner().syncMembers()
            .get(resourceGroupName, sqlServerName, databaseName, syncGroupName, name);
        return syncMemberInner != null ? new SqlSyncMemberImpl(resourceGroupName, sqlServerName, databaseName, syncGroupName, name, syncMemberInner, this.sqlServerManager) : null;
    }

    @Override
    public Mono<SqlSyncMember> getBySqlServerAsync(final String resourceGroupName, final String sqlServerName, final String databaseName, final String syncGroupName, final String name) {
        return this.sqlServerManager.inner().syncMembers()
            .getAsync(resourceGroupName, sqlServerName, databaseName, syncGroupName, name)
            .map(syncMemberInner -> new SqlSyncMemberImpl(resourceGroupName, sqlServerName, databaseName, syncGroupName, name, syncMemberInner, sqlServerManager));
    }

    @Override
    public SqlSyncMember get(String name) {
        if (this.sqlSyncGroup == null) {
            return null;
        }
        return this.getBySqlServer(this.sqlSyncGroup.resourceGroupName(), this.sqlSyncGroup.sqlServerName(), this.sqlSyncGroup.sqlDatabaseName(), this.sqlSyncGroup.name(), name);
    }

    @Override
    public Mono<SqlSyncMember> getAsync(String name) {
        if (this.sqlSyncGroup == null) {
            return null;
        }
        return this.getBySqlServerAsync(this.sqlSyncGroup.resourceGroupName(), this.sqlSyncGroup.sqlServerName(), this.sqlSyncGroup.sqlDatabaseName(), this.sqlSyncGroup.name(), name);
    }

    @Override
    public SqlSyncMember getById(String id) {
        Objects.requireNonNull(id);
        try {
            ResourceId resourceId = ResourceId.fromString(id);
            return this.getBySqlServer(resourceId.resourceGroupName(),
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
            return this.getBySqlServerAsync(resourceId.resourceGroupName(),
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
        this.sqlServerManager.inner().syncMembers()
            .delete(this.sqlSyncGroup.resourceGroupName(), this.sqlSyncGroup.sqlServerName(), this.sqlSyncGroup.sqlDatabaseName(), this.sqlSyncGroup.name(), name);
    }

    @Override
    public Mono<Void> deleteAsync(String name) {
        if (this.sqlSyncGroup == null) {
            return null;
        }
        return this.sqlServerManager.inner().syncMembers()
            .deleteAsync(this.sqlSyncGroup.resourceGroupName(), this.sqlSyncGroup.sqlServerName(), this.sqlSyncGroup.sqlDatabaseName(), this.sqlSyncGroup.name(), name);
    }

    @Override
    public void deleteById(String id) {
        try {
            ResourceId resourceId = ResourceId.fromString(id);
            this.sqlServerManager.inner().syncMembers().delete(resourceId.resourceGroupName(),
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
            return this.sqlServerManager.inner().syncMembers().deleteAsync(resourceId.resourceGroupName(),
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
            PagedIterable<SyncMemberInner> syncMemberInners = this.sqlServerManager.inner().syncMembers()
                .listBySyncGroup(this.sqlSyncGroup.resourceGroupName(), this.sqlSyncGroup.sqlServerName(), this.sqlSyncGroup.sqlDatabaseName(), this.sqlSyncGroup.name());
            if (syncMemberInners != null) {
                for (SyncMemberInner syncMemberInner : syncMemberInners) {
                    sqlSyncMembers.add(new SqlSyncMemberImpl(syncMemberInner.getName(), this.sqlSyncGroup, syncMemberInner, this.sqlServerManager));
                }
            }

        }
        return Collections.unmodifiableList(sqlSyncMembers);
    }

    @Override
    public PagedFlux<SqlSyncMember> listAsync() {
        final SqlSyncMemberOperationsImpl self = this;
        return this.sqlServerManager.inner().syncMembers()
            .listBySyncGroupAsync(this.sqlSyncGroup.resourceGroupName(), this.sqlSyncGroup.sqlServerName(), this.sqlSyncGroup.sqlDatabaseName(), this.sqlSyncGroup.name())
            .mapPage(syncMemberInner -> new SqlSyncMemberImpl(syncMemberInner.getName(), self.sqlSyncGroup, syncMemberInner, self.sqlServerManager));
    }

    @Override
    public SqlSyncMemberImpl define(String syncMemberName) {
        SqlSyncMemberImpl result = new SqlSyncMemberImpl(syncMemberName, new SyncMemberInner(), this.sqlServerManager);
        result.setPendingOperation(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
        return (this.sqlSyncGroup != null) ? result.withExistingSyncGroup(this.sqlSyncGroup) : result;
    }
}
