// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlSyncFullSchemaProperty;
import com.azure.resourcemanager.sql.models.SqlSyncGroup;
import com.azure.resourcemanager.sql.models.SqlSyncGroupLogProperty;
import com.azure.resourcemanager.sql.models.SqlSyncGroupOperations;
import com.azure.resourcemanager.sql.models.SqlSyncMemberOperations;
import com.azure.resourcemanager.sql.models.SyncConflictResolutionPolicy;
import com.azure.resourcemanager.sql.models.SyncGroupSchema;
import com.azure.resourcemanager.sql.models.SyncGroupState;
import com.azure.resourcemanager.sql.models.SyncGroupsType;
import com.azure.resourcemanager.sql.fluent.inner.SyncGroupInner;
import java.time.OffsetDateTime;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SqlSyncGroup. */
public class SqlSyncGroupImpl
    extends ExternalChildResourceImpl<SqlSyncGroup, SyncGroupInner, SqlDatabaseImpl, SqlDatabase>
    implements SqlSyncGroup, SqlSyncGroup.Update, SqlSyncGroupOperations.SqlSyncGroupOperationsDefinition {

    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;
    private String sqlDatabaseName;

    private SqlSyncMemberOperations.SqlSyncMemberActionsDefinition syncMemberOps;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses DNS alias operations
     */
    SqlSyncGroupImpl(
        String name, SqlDatabaseImpl parent, SyncGroupInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject);

        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.sqlServerName();
        this.sqlDatabaseName = parent.name();
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param resourceGroupName the resource group name
     * @param sqlServerName the parent SQL server name
     * @param sqlDatabaseName the parent SQL Database name
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses DNS alias operations
     */
    SqlSyncGroupImpl(
        String resourceGroupName,
        String sqlServerName,
        String sqlDatabaseName,
        String name,
        SyncGroupInner innerObject,
        SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlDatabaseName = sqlDatabaseName;
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses DNS alias operations
     */
    SqlSyncGroupImpl(String name, SyncGroupInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        if (innerObject != null && innerObject.id() != null) {
            try {
                ResourceId resourceId = ResourceId.fromString(innerObject.id());
                this.resourceGroupName = resourceId.resourceGroupName();
                this.sqlServerName = resourceId.parent().parent().name();
                this.sqlDatabaseName = resourceId.parent().name();
            } catch (NullPointerException e) {
            }
        }
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String sqlDatabaseName() {
        return this.sqlDatabaseName;
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.inner().id());
    }

    @Override
    public int interval() {
        return this.inner().interval();
    }

    @Override
    public OffsetDateTime lastSyncTime() {
        return this.inner().lastSyncTime();
    }

    @Override
    public SyncConflictResolutionPolicy conflictResolutionPolicy() {
        return this.inner().conflictResolutionPolicy();
    }

    @Override
    public String syncDatabaseId() {
        return this.inner().syncDatabaseId();
    }

    @Override
    public String databaseUserName() {
        return this.inner().hubDatabaseUsername();
    }

    @Override
    public SyncGroupState syncState() {
        return this.inner().syncState();
    }

    @Override
    public SyncGroupSchema schema() {
        return this.inner().schema();
    }

    @Override
    public void refreshHubSchema() {
        this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .refreshHubSchema(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name());
    }

    @Override
    public Mono<Void> refreshHubSchemaAsync() {
        return this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .refreshHubSchemaAsync(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name());
    }

    @Override
    public PagedIterable<SqlSyncFullSchemaProperty> listHubSchemas() {
        return this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .listHubSchemas(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name())
            .mapPage(inner -> new SqlSyncFullSchemaPropertyImpl(inner));
    }

    @Override
    public PagedFlux<SqlSyncFullSchemaProperty> listHubSchemasAsync() {
        return this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .listHubSchemasAsync(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name())
            .mapPage(syncFullSchemaPropertiesInner -> new SqlSyncFullSchemaPropertyImpl(syncFullSchemaPropertiesInner));
    }

    @Override
    public PagedIterable<SqlSyncGroupLogProperty> listLogs(String startTime, String endTime, String type) {
        return this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .listLogs(
                this.resourceGroupName,
                this.sqlServerName,
                this.sqlDatabaseName,
                this.name(),
                startTime,
                endTime,
                SyncGroupsType.fromString(type))
            .mapPage(inner -> new SqlSyncGroupLogPropertyImpl(inner));
    }

    @Override
    public PagedFlux<SqlSyncGroupLogProperty> listLogsAsync(String startTime, String endTime, String type) {
        return this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .listLogsAsync(
                this.resourceGroupName,
                this.sqlServerName,
                this.sqlDatabaseName,
                this.name(),
                startTime,
                endTime,
                SyncGroupsType.fromString(type))
            .mapPage(syncGroupLogPropertiesInner -> new SqlSyncGroupLogPropertyImpl(syncGroupLogPropertiesInner));
    }

    @Override
    public void triggerSynchronization() {
        this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .triggerSync(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name());
    }

    @Override
    public Mono<Void> triggerSynchronizationAsync() {
        return this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .triggerSyncAsync(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name());
    }

    @Override
    public void cancelSynchronization() {
        this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .cancelSync(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name());
    }

    @Override
    public Mono<Void> cancelSynchronizationAsync() {
        return this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .cancelSyncAsync(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name());
    }

    @Override
    public SqlSyncGroupImpl withExistingSqlServer(String resourceGroupName, String sqlServerName) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        return this;
    }

    @Override
    public SqlSyncGroupImpl withExistingSqlDatabase(SqlDatabase sqlDatabase) {
        this.resourceGroupName = sqlDatabase.resourceGroupName();
        this.sqlServerName = sqlDatabase.sqlServerName();
        this.sqlDatabaseName = sqlDatabase.name();
        return this;
    }

    @Override
    public SqlSyncGroupImpl withExistingDatabaseName(String databaseName) {
        this.sqlDatabaseName = databaseName;
        return this;
    }

    @Override
    public SqlSyncGroupImpl withSyncDatabaseId(String syncDatabaseId) {
        this.inner().withSyncDatabaseId(syncDatabaseId);
        return this;
    }

    @Override
    public SqlSyncGroupImpl withDatabaseUserName(String userName) {
        this.inner().withHubDatabaseUsername(userName);
        return this;
    }

    @Override
    public SqlSyncGroupImpl withDatabasePassword(String password) {
        this.inner().withHubDatabasePassword(password);
        return this;
    }

    @Override
    public SqlSyncGroupImpl withConflictResolutionPolicyHubWins() {
        this.inner().withConflictResolutionPolicy(SyncConflictResolutionPolicy.HUB_WIN);
        return this;
    }

    @Override
    public SqlSyncGroupImpl withConflictResolutionPolicyMemberWins() {
        this.inner().withConflictResolutionPolicy(SyncConflictResolutionPolicy.MEMBER_WIN);
        return this;
    }

    @Override
    public SqlSyncGroupImpl withInterval(int interval) {
        this.inner().withInterval(interval);
        return this;
    }

    @Override
    public SqlSyncGroupImpl withSchema(SyncGroupSchema schema) {
        this.inner().withSchema(schema);
        return this;
    }

    @Override
    public Update update() {
        this.setPendingOperation(PendingOperation.ToBeUpdated);
        return this;
    }

    @Override
    public Mono<SqlSyncGroup> createResourceAsync() {
        final SqlSyncGroupImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .createOrUpdateAsync(
                this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name(), this.inner())
            .map(
                syncGroupInner -> {
                    self.setInner(syncGroupInner);
                    return self;
                });
    }

    @Override
    public Mono<SqlSyncGroup> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .deleteAsync(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name());
    }

    @Override
    protected Mono<SyncGroupInner> getInnerAsync() {
        return this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name());
    }

    @Override
    public void delete() {
        this
            .sqlServerManager
            .inner()
            .getSyncGroups()
            .delete(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.name());
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.deleteResourceAsync();
    }

    @Override
    public SqlSyncMemberOperations.SqlSyncMemberActionsDefinition syncMembers() {
        if (this.syncMemberOps == null) {
            this.syncMemberOps = new SqlSyncMemberOperationsImpl(this, this.sqlServerManager);
        }

        return this.syncMemberOps;
    }
}
