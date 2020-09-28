// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.ServerKeyType;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerKey;
import com.azure.resourcemanager.sql.models.SqlServerKeyOperations;
import com.azure.resourcemanager.sql.fluent.models.ServerKeyInner;
import java.time.OffsetDateTime;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SQL Server Key interface. */
public class SqlServerKeyImpl extends ExternalChildResourceImpl<SqlServerKey, ServerKeyInner, SqlServerImpl, SqlServer>
    implements SqlServerKey, SqlServerKey.Update, SqlServerKeyOperations.SqlServerKeyOperationsDefinition {

    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;
    private String serverKeyName;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlServerKeyImpl(String name, SqlServerImpl parent, ServerKeyInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject);

        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.name();
        if (innerObject != null && innerObject.name() != null) {
            this.serverKeyName = innerObject.name();
        }
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param resourceGroupName the resource group name
     * @param sqlServerName the parent SQL server name
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlServerKeyImpl(
        String resourceGroupName,
        String sqlServerName,
        String name,
        ServerKeyInner innerObject,
        SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        if (innerObject != null && innerObject.name() != null) {
            this.serverKeyName = innerObject.name();
        }
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlServerKeyImpl(String name, ServerKeyInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        if (innerObject != null && innerObject.id() != null) {
            if (innerObject.name() != null) {
                this.serverKeyName = innerObject.name();
            }
            try {
                ResourceId resourceId = ResourceId.fromString(innerObject.id());
                this.resourceGroupName = resourceId.resourceGroupName();
                this.sqlServerName = resourceId.parent().name();
            } catch (NullPointerException e) {
            }
        }
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String name() {
        return this.serverKeyName;
    }

    @Override
    public SqlServerKeyImpl withExistingSqlServer(String resourceGroupName, String sqlServerName) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        return this;
    }

    @Override
    public SqlServerKeyImpl withExistingSqlServerId(String sqlServerId) {
        Objects.requireNonNull(sqlServerId);
        ResourceId resourceId = ResourceId.fromString(sqlServerId);
        this.resourceGroupName = resourceId.resourceGroupName();
        this.sqlServerName = resourceId.name();
        return this;
    }

    @Override
    public SqlServerKeyImpl withExistingSqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        this.resourceGroupName = sqlServer.resourceGroupName();
        this.sqlServerName = sqlServer.name();
        return this;
    }

    @Override
    public SqlServerKeyImpl withAzureKeyVaultKey(String uri) {
        this.innerModel().withServerKeyType(ServerKeyType.AZURE_KEY_VAULT);
        this.innerModel().withUri(uri);
        // If the key URI is "https://YourVaultName.vault.azure.net/keys/YourKeyName/01234567890123456789012345678901",
        // then the Server Key Name should be formatted as: "YourVaultName_YourKeyName_01234567890123456789012345678901"
        String[] items = uri.split("\\/");
        this.serverKeyName = String.format("%s_%s_%s", items[2].split("\\.")[0], items[4], items[5]);
        return this;
    }

    @Override
    public SqlServerKeyImpl withThumbprint(String thumbprint) {
        this.innerModel().withThumbprint(thumbprint);
        return this;
    }

    @Override
    public SqlServerKeyImpl withCreationDate(OffsetDateTime creationDate) {
        this.innerModel().withCreationDate(creationDate);
        return this;
    }

    @Override
    public Mono<SqlServerKey> createResourceAsync() {
        final SqlServerKeyImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getServerKeys()
            .createOrUpdateAsync(self.resourceGroupName, self.sqlServerName, self.serverKeyName, self.innerModel())
            .map(
                serverKeyInner -> {
                    self.setInner(serverKeyInner);
                    return self;
                });
    }

    @Override
    public Mono<SqlServerKey> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getServerKeys()
            .deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    protected Mono<ServerKeyInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getServerKeys()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.innerModel().id());
    }

    @Override
    public String kind() {
        return this.innerModel().kind();
    }

    @Override
    public Region region() {
        return Region.fromName(this.innerModel().location());
    }

    @Override
    public ServerKeyType serverKeyType() {
        return this.innerModel().serverKeyType();
    }

    @Override
    public String uri() {
        return this.innerModel().uri();
    }

    @Override
    public String thumbprint() {
        return this.innerModel().thumbprint();
    }

    @Override
    public OffsetDateTime creationDate() {
        return this.innerModel().creationDate();
    }

    @Override
    public void delete() {
        this
            .sqlServerManager
            .serviceClient()
            .getServerKeys()
            .delete(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.deleteResourceAsync();
    }

    @Override
    public Update update() {
        super.prepareUpdate();
        return this;
    }
}
