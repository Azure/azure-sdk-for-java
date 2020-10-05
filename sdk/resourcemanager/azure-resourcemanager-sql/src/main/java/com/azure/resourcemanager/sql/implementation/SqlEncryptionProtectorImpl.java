// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.EncryptionProtectorName;
import com.azure.resourcemanager.sql.models.ServerKeyType;
import com.azure.resourcemanager.sql.models.SqlEncryptionProtector;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.fluent.models.EncryptionProtectorInner;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SqlEncryptionProtector interface. */
public class SqlEncryptionProtectorImpl
    extends ExternalChildResourceImpl<SqlEncryptionProtector, EncryptionProtectorInner, SqlServerImpl, SqlServer>
    implements SqlEncryptionProtector, SqlEncryptionProtector.Update {

    private final ClientLogger logger = new ClientLogger(getClass());
    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;
    private String serverKeyName;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlEncryptionProtectorImpl(
        SqlServerImpl parent, EncryptionProtectorInner innerObject, SqlServerManager sqlServerManager) {
        super("", parent, innerObject);

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
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlEncryptionProtectorImpl(
        String resourceGroupName,
        String sqlServerName,
        EncryptionProtectorInner innerObject,
        SqlServerManager sqlServerManager) {
        super("", null, innerObject);
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
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlEncryptionProtectorImpl(EncryptionProtectorInner innerObject, SqlServerManager sqlServerManager) {
        super("", null, innerObject);
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
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String sqlServerName() {
        return this.serverKeyName;
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
    public String serverKeyName() {
        return this.innerModel().serverKeyName();
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
    public SqlEncryptionProtectorImpl withAzureKeyVaultServerKey(String serverKeyName) {
        this.innerModel().withServerKeyName(serverKeyName);
        this.innerModel().withServerKeyType(ServerKeyType.AZURE_KEY_VAULT);
        return this;
    }

    @Override
    public SqlEncryptionProtectorImpl withServiceManagedServerKey() {
        this.innerModel().withServerKeyName("ServiceManaged");
        this.innerModel().withServerKeyType(ServerKeyType.SERVICE_MANAGED);
        return this;
    }

    @Override
    public Mono<SqlEncryptionProtector> createResourceAsync() {
        final SqlEncryptionProtectorImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getEncryptionProtectors()
            .createOrUpdateAsync(
                this.resourceGroupName, this.sqlServerName, EncryptionProtectorName.CURRENT, this.innerModel())
            .map(
                encryptionProtectorInner -> {
                    self.setInner(encryptionProtectorInner);
                    return self;
                });
    }

    @Override
    public SqlEncryptionProtectorImpl update() {
        return this;
    }

    @Override
    public Mono<SqlEncryptionProtector> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("Delete operation not supported"));
    }

    @Override
    protected Mono<EncryptionProtectorInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getEncryptionProtectors()
            .getAsync(this.resourceGroupName, this.sqlServerName, EncryptionProtectorName.CURRENT);
    }
}
