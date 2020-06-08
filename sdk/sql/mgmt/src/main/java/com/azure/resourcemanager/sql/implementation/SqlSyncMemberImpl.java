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
import com.azure.resourcemanager.sql.models.SqlSyncMember;
import com.azure.resourcemanager.sql.models.SqlSyncMemberOperations;
import com.azure.resourcemanager.sql.models.SyncDirection;
import com.azure.resourcemanager.sql.models.SyncMemberDbType;
import com.azure.resourcemanager.sql.models.SyncMemberState;
import com.azure.resourcemanager.sql.fluent.inner.SyncMemberInner;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SqlSyncMember. */
public class SqlSyncMemberImpl
    extends ExternalChildResourceImpl<SqlSyncMember, SyncMemberInner, SqlSyncGroupImpl, SqlSyncGroup>
    implements SqlSyncMember, SqlSyncMember.Update, SqlSyncMemberOperations.SqlSyncMemberOperationsDefinition {

    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;
    private String sqlDatabaseName;
    private String sqlSyncGroupName;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses DNS alias operations
     */
    SqlSyncMemberImpl(
        String name, SqlSyncGroupImpl parent, SyncMemberInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject);

        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.sqlServerName();
        this.sqlDatabaseName = parent.sqlDatabaseName();
        this.sqlSyncGroupName = parent.name();
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param resourceGroupName the resource group name
     * @param sqlServerName the parent SQL server name
     * @param sqlDatabaseName the parent SQL Database name
     * @param sqlSyncGroupName the parent SQL Sync Group name
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses DNS alias operations
     */
    SqlSyncMemberImpl(
        String resourceGroupName,
        String sqlServerName,
        String sqlDatabaseName,
        String sqlSyncGroupName,
        String name,
        SyncMemberInner innerObject,
        SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlDatabaseName = sqlDatabaseName;
        this.sqlSyncGroupName = sqlSyncGroupName;
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses DNS alias operations
     */
    SqlSyncMemberImpl(String name, SyncMemberInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        if (innerObject != null && innerObject.id() != null) {
            try {
                ResourceId resourceId = ResourceId.fromString(innerObject.id());
                this.resourceGroupName = resourceId.resourceGroupName();
                this.sqlServerName = resourceId.parent().parent().parent().name();
                this.sqlDatabaseName = resourceId.parent().parent().name();
                this.sqlSyncGroupName = resourceId.parent().name();
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
    public String sqlSyncGroupName() {
        return this.sqlSyncGroupName;
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.inner().id());
    }

    @Override
    public SyncMemberDbType databaseType() {
        return this.inner().databaseType();
    }

    @Override
    public String syncAgentId() {
        return this.inner().syncAgentId();
    }

    @Override
    public String sqlServerDatabaseId() {
        return this.inner().sqlServerDatabaseId().toString();
    }

    @Override
    public String memberServerName() {
        return this.inner().serverName();
    }

    @Override
    public String memberDatabaseName() {
        return this.inner().databaseName();
    }

    @Override
    public String username() {
        return this.inner().username();
    }

    @Override
    public SyncDirection syncDirection() {
        return this.inner().syncDirection();
    }

    @Override
    public SyncMemberState syncState() {
        return this.inner().syncState();
    }

    @Override
    public Mono<SqlSyncMember> createResourceAsync() {
        final SqlSyncMemberImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .createOrUpdateAsync(
                this.resourceGroupName,
                this.sqlServerName,
                this.sqlDatabaseName,
                this.sqlSyncGroupName,
                this.name(),
                this.inner())
            .map(
                syncMemberInner -> {
                    self.setInner(syncMemberInner);
                    return self;
                });
    }

    @Override
    public Mono<SqlSyncMember> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .deleteAsync(
                this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.sqlSyncGroupName, this.name());
    }

    @Override
    protected Mono<SyncMemberInner> getInnerAsync() {
        return this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .getAsync(
                this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.sqlSyncGroupName, this.name());
    }

    @Override
    public void delete() {
        this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .delete(
                this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.sqlSyncGroupName, this.name());
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.deleteResourceAsync();
    }

    @Override
    public Update update() {
        this.setPendingOperation(PendingOperation.ToBeUpdated);
        return this;
    }

    @Override
    public PagedIterable<SqlSyncFullSchemaProperty> listMemberSchemas() {
        return this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .listMemberSchemas(
                this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.sqlSyncGroupName, this.name())
            .mapPage(inner -> new SqlSyncFullSchemaPropertyImpl(inner));
    }

    @Override
    public PagedFlux<SqlSyncFullSchemaProperty> listMemberSchemasAsync() {
        return this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .listMemberSchemasAsync(
                this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.sqlSyncGroupName, this.name())
            .mapPage(syncFullSchemaPropertiesInner -> new SqlSyncFullSchemaPropertyImpl(syncFullSchemaPropertiesInner));
    }

    @Override
    public void refreshMemberSchema() {
        this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .refreshMemberSchema(
                this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.sqlSyncGroupName, this.name());
    }

    @Override
    public Mono<Void> refreshMemberSchemaAsync() {
        return this
            .sqlServerManager
            .inner()
            .getSyncMembers()
            .refreshMemberSchemaAsync(
                this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.sqlSyncGroupName, this.name());
    }

    @Override
    public SqlSyncMemberImpl withExistingSqlServer(String resourceGroupName, String sqlServerName) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        return this;
    }

    @Override
    public SqlSyncMemberImpl withExistingSyncGroup(SqlSyncGroup sqlSyncGroup) {
        this.resourceGroupName = sqlSyncGroup.resourceGroupName();
        this.sqlServerName = sqlSyncGroup.sqlServerName();
        this.sqlDatabaseName = sqlSyncGroup.sqlDatabaseName();
        this.sqlSyncGroupName = sqlSyncGroup.name();
        return this;
    }

    @Override
    public SqlSyncMemberImpl withExistingDatabaseName(String databaseName) {
        this.sqlDatabaseName = databaseName;
        return this;
    }

    @Override
    public SqlSyncMemberImpl withMemberUserName(String userName) {
        this.inner().withUsername(userName);
        return this;
    }

    @Override
    public SqlSyncMemberImpl withExistingSyncGroupName(String syncGroupName) {
        this.sqlSyncGroupName = syncGroupName;
        return this;
    }

    @Override
    public SqlSyncMemberImpl withMemberPassword(String password) {
        this.inner().withPassword(password);
        return this;
    }

    @Override
    public SqlSyncMemberImpl withMemberSqlServerName(String sqlServerName) {
        this.inner().withServerName(sqlServerName);
        return this;
    }

    @Override
    public SqlSyncMemberImpl withMemberSqlDatabase(SqlDatabase sqlDatabase) {
        this.inner().withServerName(sqlDatabase.sqlServerName());
        this.inner().withDatabaseName(sqlDatabase.name());
        return this;
    }

    @Override
    public SqlSyncMemberImpl withMemberDatabaseType(SyncMemberDbType databaseType) {
        this.inner().withDatabaseType(databaseType);
        return this;
    }

    @Override
    public SqlSyncMemberImpl withDatabaseType(SyncDirection syncDirection) {
        this.inner().withSyncDirection(syncDirection);
        return this;
    }

    @Override
    public SqlSyncMemberImpl withMemberSqlDatabaseName(String sqlDatabaseName) {
        this.inner().withDatabaseName(sqlDatabaseName);
        return this;
    }
}
