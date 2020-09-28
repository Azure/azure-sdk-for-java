// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerDnsAlias;
import com.azure.resourcemanager.sql.models.SqlServerDnsAliasOperations;
import com.azure.resourcemanager.sql.fluent.models.ServerDnsAliasInner;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SqlServerDnsAlias. */
public class SqlServerDnsAliasImpl
    extends ExternalChildResourceImpl<SqlServerDnsAlias, ServerDnsAliasInner, SqlServerImpl, SqlServer>
    implements SqlServerDnsAlias, SqlServerDnsAliasOperations.SqlServerDnsAliasOperationsDefinition {

    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses DNS alias operations
     */
    SqlServerDnsAliasImpl(
        String name, SqlServerImpl parent, ServerDnsAliasInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject);

        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.name();
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param resourceGroupName the resource group name
     * @param sqlServerName the parent SQL server name
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses DNS alias operations
     */
    SqlServerDnsAliasImpl(
        String resourceGroupName,
        String sqlServerName,
        String name,
        ServerDnsAliasInner innerObject,
        SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses DNS alias operations
     */
    SqlServerDnsAliasImpl(String name, ServerDnsAliasInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        if (innerObject != null && innerObject.id() != null) {
            try {
                ResourceId resourceId = ResourceId.fromString(innerObject.id());
                this.resourceGroupName = resourceId.resourceGroupName();
                this.sqlServerName = resourceId.parent().name();
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
        return this.innerModel().id();
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String azureDnsRecord() {
        return this.innerModel().azureDnsRecord();
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.innerModel().id());
    }

    @Override
    public void delete() {
        this
            .sqlServerManager
            .serviceClient()
            .getServerDnsAliases()
            .delete(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.deleteResourceAsync();
    }

    @Override
    public SqlServerDnsAliasImpl withExistingSqlServer(String resourceGroupName, String sqlServerName) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        return this;
    }

    @Override
    public SqlServerDnsAliasImpl withExistingSqlServerId(String sqlServerId) {
        Objects.requireNonNull(sqlServerId);
        ResourceId resourceId = ResourceId.fromString(sqlServerId);
        this.resourceGroupName = resourceId.resourceGroupName();
        this.sqlServerName = resourceId.name();
        return this;
    }

    @Override
    public SqlServerDnsAliasImpl withExistingSqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        this.resourceGroupName = sqlServer.resourceGroupName();
        this.sqlServerName = sqlServer.name();
        return this;
    }

    @Override
    public Mono<SqlServerDnsAlias> createResourceAsync() {
        final SqlServerDnsAliasImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getServerDnsAliases()
            .createOrUpdateAsync(self.resourceGroupName, self.sqlServerName, self.name())
            .map(
                serverDnsAliasInner -> {
                    self.setInner(serverDnsAliasInner);
                    return self;
                });
    }

    @Override
    public Mono<SqlServerDnsAlias> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getServerDnsAliases()
            .deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    protected Mono<ServerDnsAliasInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getServerDnsAliases()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }
}
